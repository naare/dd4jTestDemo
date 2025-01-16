package org.naare.validation;

import org.digidoc4j.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.*;

class TrustServiceLevelTest {

    String nonQualifiedTspSource = "http://timestamp.entrust.net/TSS/RFC3161sha2TS";

    @ParameterizedTest
    @ValueSource(strings = {
            "QTSA",
            "QTSA,TSA",
            "TSA",
            "TSA,QTSA",
    })
    void timestampAsicsAndValidate_timestampTrustServiceLevel(String levels) {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        String defaultTspSource = configuration.getTspSource();
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Timestamp container
        List<String> levelsList = Arrays.asList(levels.split(","));
        IntStream.range(0, levelsList.size()).forEach(index -> {
            String tspSource = Objects.equals(levelsList.get(index), "TSA") ? nonQualifiedTspSource : defaultTspSource;
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

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        String defaultTspSource = configuration.getTspSource();
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        List<String> levelsList = Arrays.asList(levels.split(","));
        String sigTsLevel = levelsList.get(0);
        boolean sigTsValid = "QTSA".equals(sigTsLevel);
        List<String> archiveTsLevels = levelsList.size() >= 2 ? levelsList.subList(1, levelsList.size()) : List.of();

        // Create signed ASiC-E container
        configuration.setTspSource(Objects.equals(sigTsLevel, "TSA") ? nonQualifiedTspSource : defaultTspSource);
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        // Add archive timestamp (by extending signature)
        archiveTsLevels.forEach(level -> {
            configuration.setTspSourceForArchiveTimestamps(Objects.equals(level, "TSA") ? nonQualifiedTspSource : defaultTspSource);
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
        }
    }
}
