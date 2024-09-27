package org.naare;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.digidoc4j.*;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.naare.signing.Helpers.*;

class SignatureExtensionTest {

    @Test
    void extendLTSignatureToLTA() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        // Check expected test container was prepared
        assertEquals(SignatureProfile.LT, container.getSignatures().get(0).getProfile());

        // Extend signature profile to LTA
        container.extendSignatureProfile(SignatureProfile.LTA);

        assertEquals(SignatureProfile.LTA, container.getSignatures().get(0).getProfile());

        ContainerValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());
    }

    @Disabled("As of now, doesn't seem to be supported")
    @Test
    void extendTSignatureToLT() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.T);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        // Check expected test container was prepared
        assertEquals(SignatureProfile.T, container.getSignatures().get(0).getProfile());

        // Extend signature profile to LT
        container.extendSignatureProfile(SignatureProfile.LT);

        assertEquals(SignatureProfile.LT, container.getSignatures().get(0).getProfile());
    }
}
