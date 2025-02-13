package org.naare.extension;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.x509.tsp.TimestampToken;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.digidoc4j.impl.asic.AsicSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.naare.utils.TestTSPSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class ExtensionTspSourceFactoryTest {

    @ParameterizedTest
    @CsvSource({
            "B_BES, T",
            "T, LT",    //    DSS XAdESService is not usable with null TSP source
    })
    void extend_withSignatureTspSourceFactoryNull_fail(SignatureProfile fromProfile, SignatureProfile toProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setSignatureTspSourceFactory(() -> null);

        Exception exception = assertThrows(NullPointerException.class, () -> container.extendSignatureProfile(toProfile));
        assertTrue(exception.getMessage().contains("The TSPSource cannot be null"));
    }

    @Test
    void extendLtToLta_withSignatureTspSourceFactoryNull_pass() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setSignatureTspSourceFactory(() -> null);

        container.extendSignatureProfile(toProfile);
        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
    }

    @Test
    void extendBToT_withArchiveTspSourceFactoryNull_pass() {
        SignatureProfile fromProfile = SignatureProfile.B_BES;
        SignatureProfile toProfile = SignatureProfile.T;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setArchiveTspSourceFactory(() -> null);

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
    }

    @Test
    void extendTToLt_withArchiveTspSourceFactoryNull_pass() {
        SignatureProfile fromProfile = SignatureProfile.T;
        SignatureProfile toProfile = SignatureProfile.LT;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setArchiveTspSourceFactory(() -> null);

        // Can't extend to LT without OCSP source
        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setExtendingOcspSourceFactory(() -> source);

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
    }

    @Test
    void extendLTToLTA_withArchiveTspSourceFactoryNull_fail() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setArchiveTspSourceFactory(() -> null);
        Exception exception = assertThrows(NullPointerException.class, () -> container.extendSignatureProfile(toProfile));
        assertTrue(exception.getMessage().contains("The TSPSource cannot be null"));
    }

    @Test
    void extendToLta_withCustomArchiveTspSourceFactoryAndChangingTspServer() {
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Set different TSP server
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsaecc");

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);

        AsicSignature signature = (AsicSignature) container.getSignatures().get(0);
        List<TimestampToken> archiveTimestamps = signature.getOrigin().getDssSignature().getArchiveTimestamps();
        // Check archive timestamp
        assertTrue(archiveTimestamps.get(0).getTimeStamp().getTimeStampInfo().getTsa().getName().toString()
                .contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
        assertTrue(archiveTimestamps.get(1).getTimeStamp().getTimeStampInfo().getTsa().getName().toString()
                .contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }

    @Test
    void extendToLta_withChangingCustomArchiveTspSourceFactory() {
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource tspSource1 = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> tspSource1);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Set different custom TSP source
        TestTSPSource tspSource2 = new TestTSPSource("http://tsa.demo.sk.ee/tsaecc");
        configuration.setArchiveTspSourceFactory(() -> tspSource2);

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);

        AsicSignature signature = (AsicSignature) container.getSignatures().get(0);
        List<TimestampToken> archiveTimestamps = signature.getOrigin().getDssSignature().getArchiveTimestamps();
        // Check archive timestamp
        assertTrue(archiveTimestamps.get(0).getTimeStamp().getTimeStampInfo().getTsa().getName().toString()
                .contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
        assertTrue(archiveTimestamps.get(1).getTimeStamp().getTimeStampInfo().getTsa().getName().toString()
                .contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }
}
