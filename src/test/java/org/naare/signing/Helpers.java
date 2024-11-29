package org.naare.signing;

import org.digidoc4j.*;
import org.digidoc4j.impl.asic.asics.AsicSCompositeContainer;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        return new PKCS11SignatureToken("C:/Program Files/IDEMIA/AWP/DLLs/OcsCryptoki.dll", PinCode.toCharArray(), 1);
    }

    public static PKCS12SignatureToken getDefaultPkcs12SignatureToken(String PinCode) {
        return new PKCS12SignatureToken("src/test/resources/keystores/sign_ECC_from_TEST_of_ESTEID2018.p12", PinCode.toCharArray());
    }

    public static DataToSign getDataToSign(Container container, SignatureToken token, SignatureProfile profile) {
        return SignatureBuilder
                .aSignature(container)
                .withSigningCertificate(token.getCertificate())
                .withSignatureProfile(profile)
                .buildDataToSign();
    }

    public static void SignPkcs12(Container container, SignatureProfile signatureProfile) {
        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, signatureProfile);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);
    }

    public static String getDateTime() {
        DateTime time = new DateTime();
        return time.toString("dMMy_") + time.getMillisOfDay();
    }

    public static void saveContainer(Container container) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callerMethodName = stackTrace[2].getMethodName();

        // Validation to check that the container save was initiated form JUnit test method for appropriate filename
//        try {
//            if (!Class.forName(stackTrace[2].getClassName()).getDeclaredMethod(callerMethodName).isAnnotationPresent(Test.class)) {
//                throw new Exception("Must be called from JUnit Test method");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String filePath = String.format("src/test/resources/output/%s_%d.%s",
                callerMethodName,
                new DateTime().getMillis(),
                container.getType().toLowerCase());
        container.saveAsFile(filePath);
    }

    public static void saveContainer(Container container, String filename) {
        String filePath = String.format("src/test/resources/output/%s_%d.%s",
                filename,
                new DateTime().getMillis(),
                container.getType().toLowerCase());
        container.saveAsFile(filePath);
    }

    public static void validationResultHasNoIssues(ContainerValidationResult result) {
        assertAll(
                () -> assertTrue(result.isValid()),
                () -> assertEquals(0, result.getErrors().size()),
                () -> assertEquals(0, result.getWarnings().size()),
                () -> assertEquals(0, result.getContainerErrors().size()),
                () -> assertEquals(0, result.getContainerWarnings().size())
        );
    }

}
