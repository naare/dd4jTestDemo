package org.naare.extension;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.naare.utils.TestTSPSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class ExtensionTspSourceTest {

    @ParameterizedTest
    @CsvSource({
            "B_BES, T",
            "T, LT",    //    DSS XAdESService is not usable with null TSP source
    })
    void extend_withSignatureTspSourceNull_fail(SignatureProfile fromProfile, SignatureProfile toProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setSignatureTspSourceFactory(() -> null);

        Exception exception = assertThrows(NullPointerException.class, () -> container.extendSignatureProfile(toProfile));
        assertTrue(exception.getMessage().contains("The TSPSource cannot be null"));
    }

    @Test
    void extendLtToLta_withSignatureTspSourceNull_pass() {
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
    void extendBToT_withArchiveTspSourceNull_pass() {
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
    void extendTToLt_withArchiveTspSourceNull_pass() {
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
    void extendLTToLTA_withArchiveTspSourceNull_fail() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create container with B profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        configuration.setArchiveTspSourceFactory(() -> null);
        Exception exception = assertThrows(NullPointerException.class, () -> container.extendSignatureProfile(toProfile));
        assertTrue(exception.getMessage().contains("The TSPSource cannot be null"));
    }

    @Disabled("Requires manual validation")
    @Test
    void extendLtToLta_withCustomArchiveTspSource() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        String defaultTspSource = configuration.getTspSource();

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource(defaultTspSource);
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        // Set different TSP source server
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsarsa");
//        tspSource.setTspServer("http://tsa.demo.sk.ee/tsaecc");

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Set different TSP source server
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsaecc");
//        tspSource.setTspServer("http://tsa.demo.sk.ee/tsarsa");

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(toProfile, container.getSignatures().get(0).getProfile());

        // Save container and check archive timestamps through SiVa demo
        saveContainer(container);
    }
}
