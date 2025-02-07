package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OcspTimestampDifferenceTest {

    Configuration testConfiguration = Configuration.of(Configuration.Mode.TEST);

    @ParameterizedTest
    @ValueSource(strings = {
            "LV_LT_sig_OCSP_7m_after_TS",
            "LV_LT_sig_OCSP_22h_after_TS",
            "LV_LT_sig_OCSP_44h_after_TSP"})
    void foreignSignatureOcspAfterTsPass(String fileName) {
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

    @Test
    void estonianSignatureOcsp24hAfterTsFail() {
        String expectedOcspError = "The difference between the OCSP response time and the signature timestamp is too large";
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/live/asic/EE_LT_sig_OCSP_25h_after_TS.asice").
                build();
        ContainerValidationResult result = container.validate();

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(expectedOcspError, result.getErrors().get(0).getMessage());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EE_LT_sig_OCSP_15m6s_after_TS",
            "EE_LT_sig_OCSP_37m_after_TS"
    })
    void estonianSignatureOcspMoreThen15mAndLessThan24hAfterTsPassWithWarning(String fileName) {
        String expectedOcspError = "The time difference between the signature timestamp and the OCSP response exceeds "
                + testConfiguration.getAllowedTimestampAndOCSPResponseDeltaInMinutes()
                + " minutes, rendering the OCSP response not 'fresh'.";
        testConfiguration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/test/asic/" + fileName + ".asice").
                build();
        ContainerValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(expectedOcspError, result.getWarnings().get(0).getMessage());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "EE_LT_sig_valid",
            "EE_LT_sig_OCSP_1m_after_TS",
            "EE_LT_sig_OCSP_8m_after_TS"
    })
    void estonianSignatureOcspLessThen15mAfterTsPass(String fileName) {
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

    @Test
    void estonianSignatureMissingOcspFail() {
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
    void estonianPadesSignatureOcspAfterTsPass(String fileName) {
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
