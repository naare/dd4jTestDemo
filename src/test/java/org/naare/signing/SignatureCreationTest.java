package org.naare.signing;

import org.digidoc4j.*;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.getDataToSign;


public class SignatureCreationTest {

    String outputFolder = "src\\test\\resources\\output\\signatureCreation\\";

    @Test
    public void signWithDataToSign() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Set AIA source as a default for OCSP */
        //configuration.setPreferAiaOcsp(true);

        /* Reading a file to a stream */
        //InputStream inputStream = FileUtils.openInputStream(new File("C:\\Users\\heiti\\REPO\\big_big_dummy.txt"));

        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.ASICE)
                .withConfiguration(configuration)
                //.withDataFile(inputStream,"big_big_dummy.txt", "application/octet-stream")
                .withDataFile("src/test/resources/files/test.txt", "application/octet-stream")
                .build();

        /* Sign with ID card using IDEMIA driver */
        PKCS11SignatureToken signatureToken = new PKCS11SignatureToken("C:\\Program Files\\IDEMIA\\AWP\\DLLs\\OcsCryptoki.dll", "12345".toCharArray(), 1);

        DataToSign dataToSign = SignatureBuilder.aSignature(container).
                withSigningCertificate(signatureToken.getCertificate()).withSignatureProfile(SignatureProfile.LT).buildDataToSign();

        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);

        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_SignedWithData_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signWithKeystore() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        /* Sign with keystore */
        PKCS12SignatureToken signatureToken = new PKCS12SignatureToken("src\\test\\resources\\keystores\\sign_keystore.p12", "1234".toCharArray());

        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_SignedWithKeystore_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signUsingProxy() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Proxy settings */
        //configuration.setHttpsProxyUser("user_name");
        //configuration.getHttpsProxyUser();

        //configuration.setHttpsProxyUserFor(ExternalConnectionType.TSP, "TSP_user_name");
        //configuration.getHttpsProxyUserFor(ExternalConnectionType);

        //configuration.setHttpsProxyPassword("password");
        //configuration.getHttpsProxyPassword();

        //configuration.setHttpsProxyPasswordFor(ExternalConnectionType.TSP, "TSP_password");
        //configuration.getHttpsProxyPasswordFor(ExternalConnectionType);


        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = new PKCS11SignatureToken("C:\\Program Files\\IDEMIA\\AWP\\DLLs\\OcsCryptoki.dll", "12345".toCharArray(), 1);
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_SignedWithProxy_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signWithCustomTSL() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Set custom TSL */
        configuration.setSslTruststorePathFor(ExternalConnectionType.TSL, "path/to/custom/ssl/truststore.p12");
        configuration.setSslTruststorePasswordFor(ExternalConnectionType.TSL, "custom-ssl-truststore-password");
        configuration.setSslTruststoreTypeFor(ExternalConnectionType.TSL,"PKCS12");

        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = new PKCS11SignatureToken("C:\\Program Files\\IDEMIA\\AWP\\DLLs\\OcsCryptoki.dll", pin.toCharArray(), 1);
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_SignedWithCustomTSL_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }
}
