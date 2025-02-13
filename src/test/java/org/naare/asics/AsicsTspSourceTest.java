package org.naare.asics;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.TimestampBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
