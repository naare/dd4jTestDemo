package org.naare.timestamping;

import eu.europa.esig.dss.enumerations.SignatureScopeType;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.simplereport.jaxb.XmlSignatureScope;
import org.digidoc4j.*;
import org.digidoc4j.impl.asic.report.TimestampValidationReport;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.naare.signing.Helpers.buildContainer;

class TimestampTest {

    @Test
    void timestamp() {

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
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals(1, result.getTimestampReports().size());

        // Check timestamp
        TimestampValidationReport timestampValidationReport = result.getTimestampReports().get(0);
        assertEquals(TimestampQualification.QTSA, timestampValidationReport.getTimestampLevel().getValue());
        assertEquals("Qualified timestamp", timestampValidationReport.getTimestampLevel().getDescription());
        assertEquals("DEMO SK TIMESTAMPING AUTHORITY 2023E", timestampValidationReport.getProducedBy());
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
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals(2, result.getTimestampReports().size());

        // Check first timestamp
        TimestampValidationReport timestampValidationReport = result.getTimestampReports().get(0);
        assertEquals(TimestampQualification.QTSA, timestampValidationReport.getTimestampLevel().getValue());
        assertEquals("Qualified timestamp", timestampValidationReport.getTimestampLevel().getDescription());
        assertEquals("DEMO SK TIMESTAMPING AUTHORITY 2023E", timestampValidationReport.getProducedBy());
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
        assertEquals("DEMO SK TIMESTAMPING AUTHORITY 2023E", timestampValidationReport2.getProducedBy());
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
}
