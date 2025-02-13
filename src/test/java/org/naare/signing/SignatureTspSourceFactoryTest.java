package org.naare.signing;

import eu.europa.esig.dss.spi.x509.tsp.TimestampToken;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.impl.asic.AsicSignature;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.naare.utils.TestTSPSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.naare.signing.Helpers.*;

class SignatureTspSourceFactoryTest {

    @Test
    void signB_withTspSourceFactoriesNull_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSignatureTspSourceFactory(() -> null);
        configuration.setArchiveTspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.B_BES);
    }

    @ParameterizedTest
    @EnumSource(value = SignatureProfile.class, names = {"T", "LT", "LTA"})
    void sign_withSignatureTspSourceFactoryNull_fail(SignatureProfile signatureProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSignatureTspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, signatureProfile);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        Exception exception = assertThrows(NullPointerException.class, () -> dataToSign.finalize(signatureValue));
        assertTrue(exception.getMessage().contains("The TSPSource cannot be null"));
    }

    @ParameterizedTest
    @EnumSource(value = SignatureProfile.class, names = {"T", "LT"})
    void sign_withArchiveTspSourceFactoryNull_pass(SignatureProfile signatureProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setArchiveTspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        SignPkcs12(container, signatureProfile);
    }

    @Test
    void signLta_withArchiveTspSourceFactoryNull_fail() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSignatureTspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LTA);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        Exception exception = assertThrows(NullPointerException.class, () -> dataToSign.finalize(signatureValue));
        assertTrue(exception.getMessage().contains("The TSPSource cannot be null"));
    }

    @Test
    void sign_withCustomSignatureTspSourceFactoryAndNoTspServer() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source factory
        TestTSPSource tspSource = new TestTSPSource();
        configuration.setSignatureTspSourceFactory(() -> tspSource);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LTA);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        assertThrows(NullPointerException.class, () -> dataToSign.finalize(signatureValue));
    }

    @Test
    void sign_withCustomArchiveTspSourceFactoryAndNoTspServer() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source factory
        TestTSPSource tspSource = new TestTSPSource();
        configuration.setArchiveTspSourceFactory(() -> tspSource);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        PKCS12SignatureToken signatureToken = getDefaultPkcs12SignatureToken("1234");
        DataToSign dataToSign = getDataToSign(container, signatureToken, SignatureProfile.LTA);
        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());

        assertThrows(NullPointerException.class, () -> dataToSign.finalize(signatureValue));
    }

    @Test
    void sign_withCustomSignatureTspSourceFactory() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source factory with TSP server
        TestTSPSource signatureTspSource = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setSignatureTspSourceFactory(() -> signatureTspSource);

        // Create signed container
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LTA);

        AsicSignature signature = (AsicSignature) container.getSignatures().get(0);
        // Check signature timestamp
        assertTrue(signature.getTimeStampTokenCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));

        List<TimestampToken> archiveTimestamps = signature.getOrigin().getDssSignature().getArchiveTimestamps();
        // Check archive timestamp
        assertTrue(archiveTimestamps.get(0).getTimeStamp().getTimeStampInfo().getTsa().getName().toString()
                .contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));
    }

    @Test
    void sign_withCustomArchiveTspSourceFactory() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource archiveTspSource = new TestTSPSource("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> archiveTspSource);

        // Create signed container
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LTA);

        AsicSignature signature = (AsicSignature) container.getSignatures().get(0);
        // Check signature timestamp
        assertTrue(signature.getTimeStampTokenCertificate().getSubjectName().contains("CN=DEMO SK TIMESTAMPING UNIT 2025E"));

        List<TimestampToken> archiveTimestamps = signature.getOrigin().getDssSignature().getArchiveTimestamps();
        // Check archive timestamp
        assertTrue(archiveTimestamps.get(0).getTimeStamp().getTimeStampInfo().getTsa().getName().toString()
                .contains("CN=DEMO SK TIMESTAMPING UNIT 2025R"));
    }
}
