package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OcspSourceTest {

    @Test
    public void validatingWithEmptyOcspSourcePass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setPreferAiaOcsp(false);
        configuration.setSigningOcspSourceFactory(() -> null);
        configuration.setExtendingOcspSourceFactory(() -> null);
        configuration.setOcspSource(null);
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(configuration).
                fromExistingFile("src/test/resources/files/test/asic/EE_LT_sig_valid.asice").
                build();
        ContainerValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
    }
}
