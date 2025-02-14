package org.naare.asics;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.TimestampBuilder;
import org.digidoc4j.exceptions.NetworkException;
import org.digidoc4j.exceptions.ServiceUnreachableException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.validationResultHasNoIssues;

class AsicsTspSourceTest {

    @Test
    void timestamp_withDefaultTspSource_test() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSource());
        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSourceForArchiveTimestamps());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }

    @Disabled("Execute based on need")
    @Test
    void timestamp_withDefaultTspSource_prod() {
        Configuration configuration = Configuration.of(Configuration.Mode.PROD);

        assertEquals("http://tsa.sk.ee", configuration.getTspSource());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=SK TIMESTAMPING UNIT 2024E"));
    }

    @Test
    void timestamp_withTspSourcesNull_usesDefaults() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        configuration.setTspSource(null);
        configuration.setTspSourceForArchiveTimestamps(null);
        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSource());
        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSourceForArchiveTimestamps());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }

    @Test
    void timestamp_withCustomTspSource() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        configuration.setTspSource("http://tsa.demo.sk.ee/tsarsa");
        assertEquals("http://tsa.demo.sk.ee/tsarsa", configuration.getTspSource());
        assertEquals("http://tsa.demo.sk.ee/tsarsa", configuration.getTspSourceForArchiveTimestamps());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
    }

    @Test
    void timestamp_withCustomTspSourceForArchiveTimestamps() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        configuration.setTspSourceForArchiveTimestamps("http://tsa.demo.sk.ee/tsarsa");
        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSource());
        assertEquals("http://tsa.demo.sk.ee/tsarsa", configuration.getTspSourceForArchiveTimestamps());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
    }

    @Test
    void timestamp_withInvalidTspSource_unknownHost() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        configuration.setTspSourceForArchiveTimestamps("http://invalid.url");
        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSource());
        assertEquals("http://invalid.url", configuration.getTspSourceForArchiveTimestamps());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        TimestampBuilder timestampBuilder = TimestampBuilder.aTimestamp(container);
        Exception exception = assertThrows(ServiceUnreachableException.class, timestampBuilder::invokeTimestamping);
        assertEquals("Failed to connect to TSP service <http://invalid.url>. Service is down or URL is invalid.",
                exception.getMessage());
    }

    @Test
    void timestamp_withInvalidTspSource_badRequest() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        configuration.setTspSourceForArchiveTimestamps("http://tsa.demo.sk.ee/invalid");
        assertEquals("http://tsa.demo.sk.ee/tsa", configuration.getTspSource());
        assertEquals("http://tsa.demo.sk.ee/invalid", configuration.getTspSourceForArchiveTimestamps());

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        TimestampBuilder timestampBuilder = TimestampBuilder.aTimestamp(container);
        Exception exception = assertThrows(NetworkException.class, timestampBuilder::invokeTimestamping);
        assertEquals("Unable to process <TSP> POST call for service <http://tsa.demo.sk.ee/invalid>",
                exception.getMessage());
    }
}
