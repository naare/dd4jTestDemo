package org.naare.signing;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import org.digidoc4j.*;
import org.digidoc4j.exceptions.OCSPRequestFailedException;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class SignatureOcspSourceTest {

    String outputFolderCreating = "src\\test\\resources\\output\\signatureCreation\\";

    @Disabled
    @Test
    void setOcspSourceNullAndSigningFails() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSigningOcspSourceFactory(() -> null);

//        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
//        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
//        source.setDataLoader(loader);
//        configuration.setExtendingOcspSourceFactory(() -> source); //ainult laiendamisel (LT -> LTA, LTA -> LTA) Kas B ja T taseme laiendamise korral rakendub see? Kui ei määra, siis fallback on null
//        testConfiguration.setSigningOcspSourceFactory(); //ainult signeerimisel, kui ei määra, siis fallback CommonOCSPSource peale
//        testConfiguration.setOcspSource(); //kui tegemist ei ole AIA-ga, siis saab määrata OCSP URL-i
//        testConfiguration.setSignOCSPRequests();
//        testConfiguration.setSigningOcspSourceFactory(()-> null); määramaks väärtuseks null

        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.ASICE)
                .withConfiguration(configuration)
                .withDataFile("src/test/resources/files/test.txt", "application/octet-stream")
                .build();

        /* Sign with ID card using IDEMIA driver */
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");

        DataToSign dataToSign = SignatureBuilder
                .aSignature(container)
                .withSigningCertificate(signatureToken.getCertificate())
                .withSignatureProfile(SignatureProfile.LT)
                .buildDataToSign();

        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

//        ContainerValidationResult result = container.validate();
//        String validationReport = result.getReport();
//
//        container.saveAsFile(outputFolderCreating + "TEST_PKCS11_Signature_" + "LT" + "_" + getDateTime() + ".asice");
//
//        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    void signTWithSigningOcspSourceNullPasses() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSigningOcspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.T);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        assertEquals(SignatureProfile.T, container.getSignatures().get(0).getProfile());
    }

    @Test
    void signLTWithSigningOcspSourceNullFails() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSigningOcspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        Exception exception = assertThrows(OCSPRequestFailedException.class, () -> {
            dataToSign.finalize(signatureValue);
        });
        assertTrue(exception.getMessage().contains("OCSP request failed"));
    }

    @Test
    void signLTWithSigningOcspSourceDefaultPasses() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        assertEquals(SignatureProfile.LT, container.getSignatures().get(0).getProfile());
        validationResultHasNoIssues(container.validate());
    }

    @Test
    void signLTWithSigningOcspSourceCustomPasses() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setSigningOcspSourceFactory(() -> source);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        assertEquals(SignatureProfile.LT, container.getSignatures().get(0).getProfile());

        validationResultHasNoIssues(container.validate());
    }
}
