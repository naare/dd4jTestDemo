package org.naare.signing;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.exceptions.OCSPRequestFailedException;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class SignatureOcspSourceTest {

    @Test
    void signT_withSigningOcspSourceNull_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSigningOcspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.T);

        assertEquals(SignatureProfile.T, container.getSignatures().get(0).getProfile());
    }

    @Test
    void signLT_withSigningOcspSourceNull_fail() {
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
    void signLt_withSigningOcspSourceCustom_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
        source.setDataLoader(loader);
        configuration.setSigningOcspSourceFactory(() -> source);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        assertEquals(SignatureProfile.LT, container.getSignatures().get(0).getProfile());
        validationResultHasNoIssues(container.validate());
    }
}
