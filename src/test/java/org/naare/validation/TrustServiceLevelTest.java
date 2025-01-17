package org.naare.validation;

import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SubIndication;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import org.digidoc4j.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.naare.signing.Helpers.*;

class TrustServiceLevelTest {

    private static final String NON_QUALIFIED_TSP_SOURCE = "http://timestamp.entrust.net/TSS/RFC3161sha2TS";
    private Configuration configuration;
    private String defaultTspSource;

    @BeforeEach
    void setup() {
        configuration = Configuration.of(Configuration.Mode.TEST);
        defaultTspSource = configuration.getTspSource();
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "QTSA",
            "QTSA,TSA",
            "TSA",
            "TSA,QTSA",
    })
    void timestampAsicsAndValidate_timestampTrustServiceLevel(String levels) {
        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Timestamp container
        List<String> levelsList = Arrays.asList(levels.split(","));
        IntStream.range(0, levelsList.size()).forEach(index -> {
            String tspSource = levelsList.get(index).equals("TSA") ? NON_QUALIFIED_TSP_SOURCE : defaultTspSource;
            // First level applies to timestamp, the rest (if present) to archive timestamp
            if (index == 0) {
                configuration.setTspSource(tspSource);
            } else {
                configuration.setTspSourceForArchiveTimestamps(tspSource);
            }
            container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());
        });

        // Validate container
        ContainerValidationResult result = container.validate();
        // Valid, if at least one QTSA timestamp
        assertEquals(levelsList.contains("QTSA"), result.isValid());

        // Validation errors, if at least one TSA timestamp
        if (levelsList.contains("TSA")) {
            assertEquals(2, result.getErrors().size());
            assertEquals(0, result.getWarnings().size());
            assertEquals(0, result.getContainerErrors().size());
            assertEquals(0, result.getContainerWarnings().size());

            assertEquals("The trust service(s) related to the time-stamp does not have the expected type identifier!", result.getErrors().get(0).getMessage());
            assertEquals("The certificate is not related to a TSA/QTST!", result.getErrors().get(1).getMessage());
        } else {
            validationResultHasNoIssues(result);
        }

        // Check indications and timestamp levels
        IntStream.range(0, levelsList.size()).forEach(index -> {
            String timestampId = result.getTimestampIdList().get(index);
            if (levelsList.get(index).equals("QTSA")) {
                // DD4J
                assertEquals(Indication.PASSED, result.getTimestampReports().get(index).getIndication());
                assertEquals(TimestampQualification.QTSA, result.getTimestampReports().get(index).getTimestampLevel().getValue());
                assertEquals("Qualified timestamp", result.getTimestampReports().get(index).getTimestampLevel().getDescription());
                // DSS
                assertEquals(Indication.PASSED, result.getIndication(timestampId));
                assertNull(result.getSubIndication(timestampId));
            } else {
                // DD4J
                assertEquals(Indication.INDETERMINATE, result.getTimestampReports().get(index).getIndication());
                assertEquals(TimestampQualification.TSA, result.getTimestampReports().get(index).getTimestampLevel().getValue());
                assertEquals("Not qualified timestamp", result.getTimestampReports().get(index).getTimestampLevel().getDescription());

                // DSS
                assertEquals(Indication.INDETERMINATE, result.getIndication(timestampId));
                assertEquals(SubIndication.NO_CERTIFICATE_CHAIN_FOUND, result.getSubIndication(timestampId));
            }
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "QTSA",
            "TSA",
            "QTSA,QTSA",
            "QTSA,QTSA,TSA",
            "QTSA,TSA",
            "QTSA,TSA,QTSA",
            "TSA,QTSA",
            "TSA,TSA",
    })
    void signAsiceAndValidate_timestampTrustServiceLevel(String levels) {
        List<String> levelsList = Arrays.asList(levels.split(","));
        String sigTsLevel = levelsList.get(0);
        boolean sigTsValid = "QTSA".equals(sigTsLevel);
        List<String> archiveTsLevels = levelsList.size() >= 2 ? levelsList.subList(1, levelsList.size()) : Collections.emptyList();

        // Create signed ASiC-E container
        configuration.setTspSource(sigTsLevel.equals("TSA") ? NON_QUALIFIED_TSP_SOURCE : defaultTspSource);
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        // Add archive timestamp (by extending signature)
        archiveTsLevels.forEach(level -> {
            configuration.setTspSourceForArchiveTimestamps(level.equals("TSA") ? NON_QUALIFIED_TSP_SOURCE : defaultTspSource);
            container.extendSignatureProfile(SignatureProfile.LTA);
        });

        // Validate container
        ContainerValidationResult result = container.validate();
        // Valid, if no TSA timestamps (signature timestamp or archiveTimestamp)
        assertEquals(!levelsList.contains("TSA"), result.isValid());

        // Validation errors, if at least one TSA timestamp
        if (levelsList.contains("TSA")) {
            assertEquals(sigTsValid ? 2 : 3, result.getErrors().size());
            assertEquals(0, result.getWarnings().size());
            assertEquals(0, result.getContainerErrors().size());
            assertEquals(0, result.getContainerWarnings().size());

            assertEquals("The trust service(s) related to the time-stamp does not have the expected type identifier!", result.getErrors().get(0).getMessage());
            assertEquals("The certificate is not related to a TSA/QTST!", result.getErrors().get(1).getMessage());
            // Validation error, if signature timestamp TSA
            if (!sigTsValid) {
                assertEquals("Signature has an invalid timestamp", result.getErrors().get(2).getMessage());
            }

            // Check indications (DD4J)
            assertEquals(Indication.INDETERMINATE, result.getSignatureReports().get(0).getIndication());
        } else {
            // Check indications (DD4J)
            assertEquals(Indication.TOTAL_PASSED, result.getSignatureReports().get(0).getIndication());
        }

        // Check indications (DSS)
        String signatureId = result.getSignatureIdList().get(0);
        assertEquals(Indication.TOTAL_PASSED, result.getIndication(signatureId));
        assertNull(result.getSubIndication(signatureId));

        // Check signature level is not lowered
        if (archiveTsLevels.isEmpty()) {
            assertEquals(SignatureLevel.XAdES_BASELINE_LT, result.getSignatureReports().get(0).getSignatureFormat());
        } else {
            assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());
        }
    }
}
