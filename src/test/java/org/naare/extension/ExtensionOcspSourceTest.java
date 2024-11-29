package org.naare.extension;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import org.digidoc4j.*;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.digidoc4j.impl.asic.AsicSignature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class ExtensionOcspSourceTest {

    @Test
    void extendBToT_whenExtendingOcspSourceSetNull_Pass() {
        SignatureProfile fromProfile = SignatureProfile.B_BES;
        SignatureProfile toProfile = SignatureProfile.T;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setExtendingOcspSourceFactory(() -> null);

        // Create container with B profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        assertEquals(SignatureLevel.XAdES_BASELINE_T, container.validate().getSignatureReports().get(0).getSignatureFormat());
    }

    @Test
    void extendTToLt_whenExtendingOcspSourceSetNull_Fail() {
        SignatureProfile fromProfile = SignatureProfile.T;
        SignatureProfile toProfile = SignatureProfile.LT;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setExtendingOcspSourceFactory(() -> null);

        // Create container with T profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Extension failed, signature profile hasn't changed
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());
        assertEquals(SignatureLevel.XAdES_BASELINE_T, container.validate().getSignatureReports().get(0).getSignatureFormat());

        // No OCSP taken
        assertEquals(0, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLtToLta_whenExtendingOcspSourceSetNull_Pass() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setExtendingOcspSourceFactory(() -> null);

        // Create container with LT profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendTToLt_whenExtendingOcspSourceSet_passWithOcspTaken() {
        SignatureProfile fromProfile = SignatureProfile.T;
        SignatureProfile toProfile = SignatureProfile.LT;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setExtendingOcspSourceFactory(() -> source);

        // Create container with T profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());
        assertEquals(0, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LT, result.getSignatureReports().get(0).getSignatureFormat());

        // OCSP is taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLtToLta_whenOcspExpiredButInTslAndExtendingOcspSourceSetNull_passWithCurrentOcsp() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setExtendingOcspSourceFactory(() -> null);

        Container container = ContainerOpener
                .open("src\\test\\resources\\files\\asice_ocsp_cert_expired.asice", configuration);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLtToLta_whenOcspExpiredButInTslAndExtendingOcspSourceSet_passWithCurrentOcsp() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setExtendingOcspSourceFactory(() -> source);

        Container container = ContainerOpener
                .open("src\\test\\resources\\files\\asice_ocsp_cert_expired.asice", configuration);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLtToLta_whenOcspExpiredAndNotInTsl_passWithCurrentOcsp() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Default is null
        configuration.setExtendingOcspSourceFactory(() -> null);

        Container container = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        // Valid in DD4J (but not in DSS if DSS can't get new OCSP)
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // ------------------------------------------
        // Simulating DSS validation by setting constraint RevocationIssuerNotExpired=FAIL
        configuration.setValidationPolicy("conf/test_constraint_RevocationIssuerNotExpired_FAIL.xml");
        Container container2 = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        container2.extendSignatureProfile(toProfile);
        ContainerValidationResult result2 = container2.validate();
        assertAll(
                () -> assertFalse(result2.isValid()),
                () -> assertEquals(3, result2.getErrors().size()),
                () -> assertEquals(1, result2.getWarnings().size())
        );
    }

    @Test
        // TODO: should such action be allowed or result be valid in DD4J? - see DD4J-1158
    void extendLtToLta_whenOcspExpiredAndNotInTslAndExtendingOcspSourceSet_passWithNewOcspTaken() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setExtendingOcspSourceFactory(() -> source);

        Container container = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // Extend signature profile
        // Note, if aia.demo.sk.ee is not reachable, extending will fail
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        // Valid in DD4J (and in DSS)
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // Additional OCSP taken
        assertEquals(2, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // ------------------------------------------
        // Simulating DSS validation by setting constraint RevocationIssuerNotExpired=FAIL
        configuration.setValidationPolicy("conf/test_constraint_RevocationIssuerNotExpired_FAIL.xml");
        Container container2 = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        container2.extendSignatureProfile(toProfile);
        validationResultHasNoIssues(container2.validate());
    }
}
