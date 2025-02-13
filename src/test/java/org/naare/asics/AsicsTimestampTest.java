package org.naare.asics;

import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SignatureScopeType;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.simplereport.jaxb.XmlSignatureScope;
import org.digidoc4j.*;
import org.digidoc4j.impl.asic.report.TimestampValidationReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.validationResultHasNoIssues;

class AsicsTimestampTest {

    @Test
    void timestampCreatedDatafileAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create datafile ASiC-S container
        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.ASICS)
                .withConfiguration(configuration)
                // Set datafile and its mimetype
                .withDataFile("src/test/resources/files/test.txt", "text/plain")
//                .withDataFile("src/test/resources/files/test/asics/Test_ASICS.asics", "application/vnd.etsi.asic-s+zip")
                .build();

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(1, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void timestampCreatedCompositeAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create timestamped composite ASiC-S container
        CompositeContainer container = CompositeContainerBuilder
                .fromContainerFile("src/test/resources/files/test/asics/Test_ASICS.asics")
                .withConfiguration(configuration)
                .buildTimestamped(timestampBuilder -> {
                });

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(2, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void timestampOpenedDatafileAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing datafile ASiC-S container
        String filepath = "src/test/resources/files/test/asics/Test_ASICS.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(2, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void timestampOpenedCompositeAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing composite ASiC-S container
        String filepath = "src/test/resources/files/test/asics/TEST_composite_ASICS.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(3, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ValidDDOCinsideAsics",
            "asicsWithTsExpiredAndWithdrawn"})
    void timestampAsics_withTsExpiredAndWithdrawnInTsl_validatesWithWarning(String fileName) {

        Configuration configuration = Configuration.of(Configuration.Mode.PROD);

        // Open ASiC-S container with expired timestamp which service is withdrawn in TSL
        String filepath = "src/test/resources/files/live/asics/" + fileName + ".asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(1, result.getContainerWarnings().size());
        // Check container validation returns warnings
        assertTrue(result.getWarnings().get(0).getMessage().contains("The certificate is not related to a granted status at time-stamp lowest POE time!"));
        assertTrue(result.getContainerWarnings().get(0).getMessage().contains("Found a timestamp token not related to granted status. If not yet covered with a fresh timestamp token, this container might become invalid in the future."));
        // Check that TimestampLevel has been lowered
        assertEquals(TimestampQualification.TSA, result.getTimestampReports().get(0).getTimestampLevel().getValue());

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(1, result.getContainerWarnings().size());
        assertTrue(result.getWarnings().get(0).getMessage().contains("The certificate is not related to a granted status at time-stamp lowest POE time!"));
        assertTrue(result.getContainerWarnings().get(0).getMessage().contains("Found a timestamp token not related to granted status. If not yet covered with a fresh timestamp token, this container might become invalid in the future."));
        assertEquals(TimestampQualification.TSA, result.getTimestampReports().get(0).getTimestampLevel().getValue());
        assertEquals(TimestampQualification.QTSA, result.getTimestampReports().get(1).getTimestampLevel().getValue());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void timestampAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Check container validity before timestamping
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getTimestampReports().size());

        // Timestamp the container
        TimestampBuilder timestampBuilder = TimestampBuilder.aTimestamp(container);
        Timestamp timestamp = timestampBuilder.invokeTimestamping();
        container.addTimestamp(timestamp);

        // Check container validity
        result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(1, result.getTimestampReports().size());

        // Check timestamp
        TimestampValidationReport timestampValidationReport = result.getTimestampReports().get(0);
        assertEquals(TimestampQualification.QTSA, timestampValidationReport.getTimestampLevel().getValue());
        assertEquals("Qualified timestamp", timestampValidationReport.getTimestampLevel().getDescription());
        assertEquals("DEMO SK TIMESTAMPING UNIT 2025E", timestampValidationReport.getProducedBy());
        assertTrue(timestampValidationReport.getProductionTime().getTime() - new Date().getTime() <= 1000);
        assertEquals(1, timestampValidationReport.getTimestampScope().size());

        // Check timestamp scope
        XmlSignatureScope timestampScope = timestampValidationReport.getTimestampScope().get(0);
        assertEquals("test.txt", timestampScope.getName());
        assertEquals(SignatureScopeType.FULL, timestampScope.getScope());
        assertEquals("Full document", timestampScope.getValue());
    }

    @Test
    void timestampExistingTimestamp() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Check container validity before timestamping
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        // Check there is one timestamp present
        assertEquals(1, result.getTimestampReports().size());

        // Timestamp the container again
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Check container validity
        result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(2, result.getTimestampReports().size());

        // Check first timestamp
        TimestampValidationReport timestampValidationReport = result.getTimestampReports().get(0);
        assertEquals(TimestampQualification.QTSA, timestampValidationReport.getTimestampLevel().getValue());
        assertEquals("Qualified timestamp", timestampValidationReport.getTimestampLevel().getDescription());
        assertEquals("DEMO SK TIMESTAMPING UNIT 2025E", timestampValidationReport.getProducedBy());
        assertTrue(timestampValidationReport.getProductionTime().getTime() - new Date().getTime() <= 5000);
        assertEquals(1, timestampValidationReport.getTimestampScope().size());

        // Check first timestamp scope
        XmlSignatureScope timestampScope = timestampValidationReport.getTimestampScope().get(0);
        assertEquals("test.txt", timestampScope.getName());
        assertEquals(SignatureScopeType.FULL, timestampScope.getScope());
        assertEquals("Full document", timestampScope.getValue());

        // Check added timestamp
        TimestampValidationReport timestampValidationReport2 = result.getTimestampReports().get(1);
        assertEquals(TimestampQualification.QTSA, timestampValidationReport2.getTimestampLevel().getValue());
        assertEquals("Qualified timestamp", timestampValidationReport2.getTimestampLevel().getDescription());
        assertEquals("DEMO SK TIMESTAMPING UNIT 2025E", timestampValidationReport2.getProducedBy());
        assertTrue(timestampValidationReport.getProductionTime().getTime() - new Date().getTime() <= 5000);
        assertEquals(3, timestampValidationReport2.getTimestampScope().size());

        // Check first timestamp scopes
        XmlSignatureScope timestampScope1 = timestampValidationReport2.getTimestampScope().get(0);
        assertEquals("META-INF/ASiCArchiveManifest.xml", timestampScope1.getName());
        assertEquals(SignatureScopeType.FULL, timestampScope1.getScope());
        assertEquals("Manifest document", timestampScope1.getValue());

        XmlSignatureScope timestampScope2 = timestampValidationReport2.getTimestampScope().get(1);
        assertEquals("META-INF/timestamp.tst", timestampScope2.getName());
        assertEquals(SignatureScopeType.FULL, timestampScope2.getScope());
        assertEquals("Full document", timestampScope2.getValue());

        XmlSignatureScope timestampScope3 = timestampValidationReport2.getTimestampScope().get(2);
        assertEquals("test.txt", timestampScope3.getName());
        assertEquals(SignatureScopeType.FULL, timestampScope3.getScope());
        assertEquals("Full document", timestampScope3.getValue());
    }

    @Test
    void timestampAsics_withTsExpiredButGrantedInTsl_validates() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open ASiC-S container with expired timestamp which service is granted in TSL
        String filepath = "src/test/resources/files/test/asics/asicsWithTsExpiredButGranted.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
        // Check that TimestampLevel is not lowered
        assertEquals(TimestampQualification.QTSA, result.getTimestampReports().get(0).getTimestampLevel().getValue());

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(2, result.getTimestampReports().size());

    }

    @Test
    void timestampAsics_withBrokenTs_failsWithException() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open ASiC-S container with broken timestamp
        String filepath = "src/test/resources/files/test/asics/1xTST-valid-bdoc-data-file-hash-failure-in-tst.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        String timestampId = container.getTimestamps().get(0).getUniqueId();

        // Add timestamp
        Exception exception = assertThrows(eu.europa.esig.dss.alert.exception.AlertException.class, () -> {
            container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());
        });
        assertTrue(exception.getMessage().contains("Broken timestamp(s) detected. [" + timestampId + ": Signature is not intact!]"));
    }

    @Test
    void timestampAsics_withOneValidAndOneInvalidTs_validates() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open ASiC-S container with one valid and one invalid timestamp
        String filepath = "src/test/resources/files/test/asics/2xTST-text-data-file-hash-failure-since-2nd-tst.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        // Check timestamp addition was successful
        assertEquals(3, result.getTimestampReports().size());
        assertEquals(Indication.PASSED, result.getTimestampReports().get(2).getIndication());
    }

    @Test
    void validateTimestampAsics_firstTsInvalid_SecondTsValidButDatafileNotCovered_validatesWithWarning() {
        Configuration configuration = Configuration.of(Configuration.Mode.PROD);

        // Open ASiC-S container with first as invalid TS and second valid, but datafile not covered
        String filepath = "src/test/resources/files/live/asics/2xTST-1st-invalid-2nd-does-not-cover-datafile.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        // Check timestamp indications are correct
        assertEquals(2, result.getTimestampReports().size());
        assertEquals(Indication.FAILED, result.getTimestampReports().get(0).getIndication());
        assertEquals(Indication.PASSED, result.getTimestampReports().get(1).getIndication());
        assertEquals("The time-stamp token does not cover container datafile!", result.getWarnings().get(0).getMessage());
        assertEquals("The time-stamp token does not cover container datafile!", result.getTimestampReports().get(1).getWarnings().get(0));
    }

    @Test
    void validateCompositeAsics_firstTsInvalid_SecondTsValidButDatafileNotCovered_validatesWithWarning() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open ASiC-S container with first as invalid TS and second valid, but datafile not covered
        String filepath = "src/test/resources/files/test/asics/2xTST-valid-bdoc-data-file-1st-tst-invalid-2nd-tst-no-coverage.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        // Check timestamp indications are correct
        assertEquals(2, result.getTimestampReports().size());
        assertEquals(Indication.FAILED, result.getTimestampReports().get(0).getIndication());
        assertEquals(Indication.PASSED, result.getTimestampReports().get(1).getIndication());
        assertEquals("The time-stamp token does not cover container datafile!", result.getWarnings().get(0).getMessage());
        assertEquals("The time-stamp token does not cover container datafile!", result.getTimestampReports().get(1).getWarnings().get(0));
    }

    @Test
    void validateCompositeAsics_validTimestamps_LastTsDatafileNotCovered_validatesWithWarning() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open ASiC-S container with valid timestamps, but last does not cover datafile
        String filepath = "src/test/resources/files/test/asics/2xTST-both-valid-2nd-tst-not-covering-nested-container.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        // Check timestamp indications are correct
        assertEquals(3, result.getTimestampReports().size());
        assertEquals(Indication.PASSED, result.getTimestampReports().get(0).getIndication());
        assertEquals(Indication.PASSED, result.getTimestampReports().get(1).getIndication());
        assertEquals(Indication.PASSED, result.getTimestampReports().get(2).getIndication());
        assertEquals("The time-stamp token does not cover container datafile!", result.getWarnings().get(0).getMessage());
        assertEquals("The time-stamp token does not cover container datafile!", result.getTimestampReports().get(2).getWarnings().get(0));
    }

    @Test
    void timestampAsics_noCoverageWarningIfTimestampScopeFull_validates() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open ASiC-S container with full scope timestamps
        String filepath = "src/test/resources/files/test/asics/5xTST_validTimestamps_scopeFull.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(5, result.getTimestampReports().size());
    }
}
