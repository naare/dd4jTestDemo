package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OcspValidationFailTest {

    Configuration testConfiguration = Configuration.of(Configuration.Mode.TEST);

    @Test
    void estonianLtSignatureOcspRemovedFail() {
        testConfiguration.setLotlLocation("http://ib-repo-01.dev.riaint.ee/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(testConfiguration).
                fromExistingFile("src/test/resources/files/test/asic/EE_LT_sig_OCSP_response_removed.asice").
                build();
        ContainerValidationResult result = container.validate();

        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
        assertEquals("No revocation data found for the certificate!", result.getErrors().get(1).getMessage());
        assertEquals(1, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }

}
