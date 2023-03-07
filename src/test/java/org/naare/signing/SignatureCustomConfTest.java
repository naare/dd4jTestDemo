package org.naare.signing;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.x509.aia.AIASource;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.buildContainer;
import static org.naare.signing.Helpers.getDataToSign;
import static org.naare.signing.Helpers.getDefaultPkcs11SignatureToken;

public class SignatureCustomConfTest {

    String outputFolder = "src\\test\\resources\\output\\customConf\\";

    @Test
    public void signWithCustomAiaSource() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Assign custom DataLoader (deprecated from 5.1.0 version) */
        DataLoader dataLoader = null;
        configuration.setAiaDataLoaderFactory(() -> dataLoader);

        /* Assign custom AiaSourceFactory for testing */
        //AIASource aiaSource = null;
        AIASource aiaSource = certificateToken -> {
            System.out.println("Just for logging");
            return Collections.emptySet();
        };
        configuration.setAiaSourceFactory(() -> aiaSource);

        /* Invalidate DD4J TSL cache */
        configuration.getTSL().invalidateCache();
        /* For OCSP prefer AIA source */
        //configuration.setPreferAiaOcsp(true);

        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_Custom_AIA_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signWithCustomLOTL() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* TEST TSL-i URL (setTslLocation deprecated from 5.0.0) */
        configuration.setLotlLocation("URL_to_custom_LOTL");
        //configuration.setTslLocation("URL_to_custom_LOTL");

        /* Invalidate DD4J TSL cache */
        configuration.getTSL().invalidateCache();

        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_Custom_AIA_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }

    @Test
    public void signWithCustomRequiredTerritories() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Required countries which TSL update must be successful */
        configuration.setRequiredTerritories("EE");

        /* Invalidate DD4J TSL cache and force reload */
        configuration.getTSL().invalidateCache();
        configuration.getTSL().refresh();


        /* Build a container and sign it */
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LT);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

        DateTime time = new DateTime();
        container.saveAsFile(outputFolder + "TEST" + "_Custom_AIA_" + time.getMillis() + ".asice");

        assertEquals(container.getSignatures().size(), 1);
    }
}
