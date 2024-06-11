package org.naare.signing;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.DataToSign;
import org.digidoc4j.SignatureBuilder;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.SignatureToken;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.joda.time.DateTime;

public class Helpers {

    public static Container buildContainer(Container.DocumentType type, Configuration config, String filePath, String mimeType) {
        return ContainerBuilder
                .aContainer(type)
                .withConfiguration(config)
                .withDataFile(filePath, mimeType)
                .build();
    }

    public static Container buildContainer(Container.DocumentType type, Configuration config) {
        return ContainerBuilder
                .aContainer(type)
                .withConfiguration(config)
                .withDataFile("src/test/resources/files/test.txt", "application/octet-stream")
                .build();
    }

    public static PKCS11SignatureToken getDefaultPkcs11SignatureToken(String PinCode) {
        return new PKCS11SignatureToken("C:\\Program Files\\IDEMIA\\AWP\\DLLs\\OcsCryptoki.dll", PinCode.toCharArray(), 1);
    }

    public static PKCS12SignatureToken getDefaultPkcs12SignatureToken(String PinCode) {
        return new PKCS12SignatureToken("src\\test\\resources\\keystores\\sign_ECC_from_TEST_of_ESTEID2018.p12", PinCode.toCharArray());
    }

    public static DataToSign getDataToSign(Container container, SignatureToken token, SignatureProfile profile) {
        return SignatureBuilder
                .aSignature(container)
                .withSigningCertificate(token.getCertificate())
                .withSignatureProfile(profile)
                .buildDataToSign();
    }

    public static String getDateTime() {
        DateTime time = new DateTime();
        return time.toString("dMMy_") + time.getMillisOfDay();
    }

}
