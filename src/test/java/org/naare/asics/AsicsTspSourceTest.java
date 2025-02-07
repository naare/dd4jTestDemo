package org.naare.asics;

import org.digidoc4j.*;
import org.junit.jupiter.api.Test;
import org.naare.utils.TestTSPSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.validationResultHasNoIssues;

class AsicsTspSourceTest {

    @Test
    void timestamp_withSignatureTspSourceNull_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
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
    void timestamp_withArchiveTspSourceNull_fail() {
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
    void timestamp_withCustomArchiveTspSource() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        String defaultTspSource = configuration.getTspSource();

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource(defaultTspSource);
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        // Create datafile ASiC-S container
        Container container = buildContainer(Container.DocumentType.ASICS, configuration);

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Set different TSP source server
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsarsa");

        // Add timestamp
        container.addTimestamp(TimestampBuilder.aTimestamp(container).invokeTimestamping());

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(2, result.getTimestampReports().size());

        assertEquals("TEST of SK TSA CA 2023E",
                container.getTimestamps().get(0).getCertificate().issuerName(X509Cert.Issuer.CN));
        assertEquals("TEST of SK TSA CA 2023R",
                container.getTimestamps().get(1).getCertificate().issuerName(X509Cert.Issuer.CN));
    }
}
