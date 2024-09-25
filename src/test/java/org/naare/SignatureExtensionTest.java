package org.naare;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        // Extend signature profile to LTA
        container.extendSignatureProfile(SignatureProfile.LT);

        assertEquals(SignatureProfile.LT, container.getSignatures().get(0).getProfile());
    }
}
