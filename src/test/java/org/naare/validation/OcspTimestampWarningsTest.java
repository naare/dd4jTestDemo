package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OcspTimestampWarningsTest {

    @Test
    public void latvianSignatureOcspAlmost24hAfterTsSuccess() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");

        Container container = ContainerBuilder.
                aContainer().withConfiguration(configuration).
                fromExistingFile("src/test/resources/files/asic/TEST_latvian_LT_signature_with_22h_difference_between_TS_and_OCSP.asice").
                build();
        ValidationResult result = container.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, ((ContainerValidationResult) result).getContainerErrors().size());
        assertEquals(0, ((ContainerValidationResult) result).getContainerWarnings().size());
    }
}
