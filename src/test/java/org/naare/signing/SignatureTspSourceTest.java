package org.naare.signing;

import eu.europa.esig.dss.spi.x509.tsp.TimestampToken;
import org.digidoc4j.*;
import org.digidoc4j.impl.asic.AsicSignature;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.naare.utils.TestTSPSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.*;

class SignatureTspSourceTest {

    @Test
    void signB_withTspSourcesNull_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSignatureTspSourceFactory(() -> null);
        configuration.setArchiveTspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.B_BES);
    }

    @ParameterizedTest
    @EnumSource(value = SignatureProfile.class, names = {"T", "LT", "LTA"})
    void sign_withSignatureTspSourceNull_fail(SignatureProfile signatureProfile) {
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
    void sign_withArchiveTspSourceNull_pass(SignatureProfile signatureProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setArchiveTspSourceFactory(() -> null);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);

        SignPkcs12(container, signatureProfile);
    }

    @Test
    void signLta_withArchiveTspSourceNull_fail() {
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
    void sign_withCustomSignatureTspSource() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource();
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsarsa");
        configuration.setSignatureTspSourceFactory(() -> tspSource);

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
    void sign_withCustomArchiveTspSource() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource();
        tspSource.setTspServer("http://tsa.demo.sk.ee/tsarsa");
        configuration.setArchiveTspSourceFactory(() -> tspSource);

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

    @Test
    void sign_withBaltstampSignatureTspSource() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        // Set custom TSP source
        TestTSPSource tspSource = new TestTSPSource();
        tspSource.setTspServer("http://tsa.baltstamp.lt");
        configuration.setSignatureTspSourceFactory(() -> tspSource);

        // Create signed container
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        AsicSignature signature = (AsicSignature) container.getSignatures().get(0);
        // Check signature timestamp
        assertTrue(signature.getTimeStampTokenCertificate().getSubjectName().contains("CN=BalTstamp QTSA TSU1"));

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());

        // Baltstamp specific issue?
        assertTrue(result.getWarnings().get(0).getMessage().contains("The signed attribute: 'signing-certificate' is present more than once!"));
    }
}
