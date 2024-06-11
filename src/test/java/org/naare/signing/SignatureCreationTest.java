package org.naare.signing;

import org.digidoc4j.*;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.getDataToSign;
import static org.naare.signing.Helpers.getDateTime;
import static org.naare.signing.Helpers.getDefaultPkcs11SignatureToken;
import static org.naare.signing.Helpers.getDefaultPkcs12SignatureToken;


public class SignatureCreationTest {

    String outputFolderCreating = "src\\test\\resources\\output\\signatureCreation\\";
    String outputFolderExisting = "src\\test\\resources\\output\\\\ExistingContainers\\";

    @Test
    public void signPkcs11WithDataToSign() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Set dummy TSL refresh callback */
//        configuration.setTslRefreshCallback(summary -> true);

        /* Use AIA OCSP source (default true) */
//        configuration.setPreferAiaOcsp(false);

        /* Reading a file to a stream */
//        InputStream inputStream = FileUtils.openInputStream(new File("src/test/resources/files/test.txt"));

        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.ASICE)
//                .aContainer(Container.DocumentType.BDOC)
//                .aContainer(Container.DocumentType.ASICS)
//                .aContainer(Container.DocumentType.DDOC)
//                .aContainer(Container.DocumentType.PADES)
                .withConfiguration(configuration)
                /* Reading a file to a stream */
//                .withDataFile(inputStream,"big_big_dummy.txt", "application/octet-stream")
                /* Use datafile */
                .withDataFile("src/test/resources/files/test.txt", "application/octet-stream")
                .build();

        /* Sign with ID card using IDEMIA driver */
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");

        DataToSign dataToSign = SignatureBuilder
                .aSignature(container)
                .withCity("San Pedro")
                .withStateOrProvince("Puerto Vallarta")
                .withPostalCode("13456")
                .withCountry("Val Verde")
                .withRoles("Manager", "Suspicious Fisherman")
                .withSigningCertificate(signatureToken.getCertificate())
//                .withSignatureProfile(SignatureProfile.B_EPES)
//                .withSignatureProfile(SignatureProfile.B_BES)
                .withSignatureProfile(SignatureProfile.LT)
//                .withSignatureProfile(SignatureProfile.LTA)
//                .withSignatureProfile(SignatureProfile.LT_TM)
                .buildDataToSign();

        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);

        container.addSignature(signature);

        container.saveAsFile(outputFolderCreating + "TEST_PKCS11_Signature_" + "LT" + "_" + getDateTime() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signPkcs11WithDataToSignExistingContainer() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Use AIA OCSP source (default true) */
//        configuration.setPreferAiaOcsp(true);

        Container container = ContainerOpener
                .open(outputFolderExisting + "\\1_ASICE_TEST.asice", configuration);

        /* Sign container n times */
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");

        for (int i = 0; i < 10; i++) {
            DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);

            byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

            org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);

            container.addSignature(signature);
        }

        container.saveAsFile(outputFolderExisting + "TEST_PKCS11_Multiple_Signatures_" + "LT" + "_" + getDateTime() + ".asice");

        assertEquals(11, container.getSignatures().size());
    }

    @Test
    public void signPkcs12WithDataToSign() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        /* Sign with keystore */
        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");

        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);

        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);

        container.addSignature(signature);

        container.saveAsFile(outputFolderCreating + "TEST_PKCS12_Signature" + "_LT_" + getDateTime() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signPkcs12WithDataToSignExistingContainer() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Use AIA OCSP source (default true) */
//        configuration.setPreferAiaOcsp(true);

        Container container = ContainerOpener
                .open("src\\test\\resources\\containers\\historical\\1_ASICE_TEST.asice", configuration);

        /* Sign with keystore */
        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");

        for (int i = 0; i < 10; i++) {
            DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);

            byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

            org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);

            container.addSignature(signature);
        }

        container.saveAsFile(outputFolderExisting + "TEST_PKCS12_Multiple_Signatures_" + "LT" + "_" + getDateTime() + ".asice");

        assertEquals(11, container.getSignatures().size());
    }

    @Disabled("Disabled by default as need Proxy configuration")
    @Test
    public void signUsingProxy() {
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
        container.saveAsFile(outputFolderCreating + "TEST" + "_SignedWithProxy_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Disabled("Disabled by default: needs LIVE ID card, access to TS service (nortal VPN)")
    @Test
    public void signWithCustomTSL() {
        Configuration configuration = Configuration.of(Configuration.Mode.PROD);

        /* Set custom TSL */
        configuration.setSslTruststorePathFor(ExternalConnectionType.TSL, "src\\test\\resources\\conf\\PROD-lotl-truststore.p12");
        configuration.setSslTruststorePasswordFor(ExternalConnectionType.TSL, "digidoc4j-password");
        configuration.setSslTruststoreTypeFor(ExternalConnectionType.TSL, "PKCS12");

        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("<ENTER ID card PIN>");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolderCreating + "TEST_PKCS11_SignedWithCustomTSL_" + "LT" + "_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Disabled("Disabled by default as uses PROD mode, can be used with LIVE ID card and it´s PIN code")
    @Test
    public void signWithCustomLotlTruststore() {
        Configuration configuration = Configuration.of(Configuration.Mode.PROD);

        /* Set custom LOTL Truststore*/
        configuration.setLotlTruststorePath("src\\test\\resources\\conf\\PROD-lotl-truststore.p12");
        configuration.setLotlTruststorePassword("digidoc4j-password");
        configuration.setLotlTruststoreType("PKCS12");

        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("<ENTER ID card PIN>");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolderCreating + "TEST_PKCS11_SignedWithCustomLOTL_" + "LT" + "_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }
}
