package org.naare.extension;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import org.digidoc4j.*;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.digidoc4j.impl.asic.AsicSignature;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class ExtensionOcspSourceTest {

    @Test
    void extendBToTPass() {
        SignatureProfile fromProfile = SignatureProfile.B_BES;
        SignatureProfile toProfile = SignatureProfile.T;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Default is null
//        configuration.setExtendingOcspSourceFactory (()-> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, fromProfile);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
    }

    @Test
    void extendTToLTFail() {
        SignatureProfile fromProfile = SignatureProfile.T;
        SignatureProfile toProfile = SignatureProfile.LT;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Default is null
//        configuration.setExtendingOcspSourceFactory (()-> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, fromProfile);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Extension failed, signature profile hasn't changed
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());
        // No OCSP taken
        assertEquals(0, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLTToLTAPass() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Default is null
//        configuration.setExtendingOcspSourceFactory (()-> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, fromProfile);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendTToLTPass_setExtendingOcspSourceFactory() {
        SignatureProfile fromProfile = SignatureProfile.T;
        SignatureProfile toProfile = SignatureProfile.LT;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setExtendingOcspSourceFactory(() -> source);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, fromProfile);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());
        assertEquals(0, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        // OCSP is taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLTToLTAPass_ocspExpiredButInTsl() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Default is null
//        configuration.setExtendingOcspSourceFactory (()-> null);

        Container container = ContainerOpener
                .open("src\\test\\resources\\files\\asice_ocsp_cert_expired.asice", configuration);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        validationResultHasNoIssues(container.validate());
    }

    @Test
    void extendLTToLTAPass_ocspExpiredButInTsl_setExtendingOcspSourceFactory() {
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
        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        validationResultHasNoIssues(container.validate());
    }

    @Test
    void extendLTToLTAPass_ocspExpiredAndNotInTsl() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        // Default is null
//        configuration.setExtendingOcspSourceFactory (()-> null);

        Container container = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // Valid in DD4J (but not in DSS if DSS can't get new OCSP)
        validationResultHasNoIssues(container.validate());

        // ------------------------------------------
        // Simulating DSS validation by setting constraint RevocationIssuerNotExpired=FAIL
        configuration.setValidationPolicy("conf/test_constraint_RevocationIssuerNotExpired_FAIL.xml");
        Container container2 = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        container2.extendSignatureProfile(toProfile);
        ContainerValidationResult result = container2.validate();
        assertAll(
                () -> assertFalse(result.isValid()),
                () -> assertEquals(3, result.getErrors().size()),
                () -> assertEquals(1, result.getWarnings().size())
        );
    }

    @Test
        // TODO: should such action be allowed or result be valid in DD4J? - see DD4J-1158
    void extendLTToLTAPass_ocspExpiredAndNotInTsl_setExtendingOcspSourceFactory() {
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
        // Additional OCSP taken
        assertEquals(2, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());

        // Valid in DD4J (and in DSS)
        validationResultHasNoIssues(container.validate());

        // ------------------------------------------
        // Simulating DSS validation by setting constraint RevocationIssuerNotExpired=FAIL
        configuration.setValidationPolicy("conf/test_constraint_RevocationIssuerNotExpired_FAIL.xml");
        Container container2 = ContainerOpener
                .open("src\\test\\resources\\files\\asice_aia-ocsp_cert_expired.asice", configuration);
        container2.extendSignatureProfile(toProfile);
        validationResultHasNoIssues(container2.validate());
    }
}
