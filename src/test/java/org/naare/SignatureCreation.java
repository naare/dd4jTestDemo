package org.naare;

import org.digidoc4j.*;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class SignatureCreation {

    @Test
    public void signWithDataToSign() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.BDOC)
                .withConfiguration(configuration)
                .withDataFile("src\\test\\resources\\files\\test.txt", "application/octet-stream")
                .build();

//       PKCS11SignatureToken signatureToken = new PKCS11SignatureToken("pathToDriver\\IDPrimePKCS1164.dll", "12345".toCharArray(), 14);
        PKCS12SignatureToken signatureToken = new PKCS12SignatureToken("src\\test\\resources\\keystores\\sign_keystore.p12", "1234".toCharArray());

        DataToSign dataToSign = SignatureBuilder.aSignature(container).
                withSigningCertificate(signatureToken.getCertificate()).withSignatureProfile(SignatureProfile.LT_TM).buildDataToSign();

        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);

        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile("src\\test\\resources\\containers\\new\\test"+time.getMillis()+".bdoc");

        Assert.assertEquals(container.getSignatures().size(),1);
    }
}
