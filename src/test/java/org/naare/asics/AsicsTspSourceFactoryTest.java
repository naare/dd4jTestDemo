package org.naare.asics;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.TimestampBuilder;
import org.junit.jupiter.api.Test;
import org.naare.utils.TestTSPSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.validationResultHasNoIssues;

class AsicsTspSourceFactoryTest {

    @Test
    void timestamp_withSignatureTspSourceFactoryNull_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Setting SignatureTspSourceFactory null has no effect on ASiC-S timestamping
        configuration.setSignatureTspSourceFactory(() -> null);

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getTimestampReports().size());
    }

    @Test
    void timestamp_withArchiveTspSourceFactoryNull_fail() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setArchiveTspSourceFactory(() -> null);

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Timestamp the container
        TimestampBuilder timestampBuilder = TimestampBuilder.aTimestamp(container);
        Exception exception = assertThrows(NullPointerException.class, timestampBuilder::invokeTimestamping);
        assertTrue(exception.getMessage().contains("TSP source cannot be null"));
    }

    @Test
    void timestamp_withCustomArchiveTspSourceFactoryAndNoTspServer_fail() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source factory
        TestTSPSource tspSource = new TestTSPSource();
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Timestamp the container
        TimestampBuilder timestampBuilder = TimestampBuilder.aTimestamp(container);
        assertThrows(NullPointerException.class, timestampBuilder::invokeTimestamping);
    }

    @Test
    void timestamp_withCustomArchiveTspSourceFactory() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source factory with TSP server
        TestTSPSource tspSource = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> tspSource);

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
    void timestamp_withCustomArchiveTspSourceFactoryAndChangingTspServer() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Set different TSP source server
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsaecc");

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(2, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
        assertTrue(container.getTimestamps().get(1).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }

    @Test
    void timestamp_withChangingArchiveTspSourceFactory() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Set different TSP source
        TestTSPSource tspSource2 = new TestTSPSource("http://tsa.demo.sk.ee/tsaecc");
        configuration.setArchiveTspSourceFactory(() -> tspSource2);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(2, result.getTimestampReports().size());
        assertTrue(container.getTimestamps().get(0).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
        assertTrue(container.getTimestamps().get(1).getCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }
}
