package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerValidationResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OcspTimestampWarningsTest {

    Configuration testConfiguration = Configuration.of(Configuration.Mode.TEST);

    @ParameterizedTest
    @ValueSource(strings = {
            "latvian_LT_signature_with_7min_difference_between_TS_and_OCSP",
            "latvian_LT_signature_with_22h_difference_between_TS_and_OCSP",
            "latvian_LT_signature_with_44h_difference_between_TS_and_OCSP"})
    public void foreignSignatureOcspAfterTsPass(String fileName) {
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/test/asic/" + fileName + ".asice").
                build();
        ContainerValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }

    @Disabled
    @Test
    public void foreignTlevelSignatureGetOcsp24hAfterTsValidationFail() {
    }

    @Test
    public void estonianSignatureOcsp24hAfterTsFail() {
        String expectedOcspError = "The difference between the OCSP response time and the signature timestamp is too large";
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/live/asic/EE_SER-AEX-B-LT-V-20.asice").
                build();
        ContainerValidationResult result = container.validate();

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(expectedOcspError, result.getErrors().get(0).getMessage());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }

    @Disabled
    @Test
    public void estonianSignatureOcspMoreThen15mAndLessThan24hAfterTsPassWithWarning() {
    }

    @Test
    public void estonianSignatureOcspLessThen15mAfterTsPass() {
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/test/asic/estonian_LT_signature.asice").
                build();
        ContainerValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }

    @Disabled
    @Test
    public void estonianTLevelSignatureGetOcsp24hAfterTsFail() {
    }

    @Test
    public void estonianSignatureMissingOcspFail() {
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/live/asic/LIVE_LT_missing_OCSP.asice").
                build();
        ContainerValidationResult result = container.validate();

        assertFalse(result.isValid());
        assertEquals(3, result.getErrors().size());
        assertEquals("No revocation data found for the certificate!", result.getErrors().get(1).getMessage());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());

    }

    //DD4J-714 will apply the same checks as ASIC has, where 15m will give a warning and 24h an error
    @ParameterizedTest
    @ValueSource(strings = {
            "hellopades-lt-sha256-ocsp-15min1s",
            "hellopades-lt-sha256-ocsp-28h"
    })
    public void estonianPadesSignatureOcspAfterTsPass(String fileName) {
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/live/pdf/" + fileName + ".pdf").
                build();
        ContainerValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }
}
