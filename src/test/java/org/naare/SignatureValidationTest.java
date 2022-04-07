package org.naare;

import org.digidoc4j.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureValidationTest {

    @Test
    public void validateSignature() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = ContainerBuilder.
                aContainer().withConfiguration(configuration).
                        fromExistingFile("src\\test\\resources\\containers\\historical\\BDOC.bdoc").
                        build();

        ValidationResult result = container.validate();

        boolean isSignatureValid = result.isValid();

        assertTrue(isSignatureValid);
    }
}
