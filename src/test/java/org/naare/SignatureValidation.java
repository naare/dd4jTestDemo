package org.naare;

import org.digidoc4j.*;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class SignatureValidation {

    @Test
    public void validateSignature() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = ContainerBuilder.
                aContainer().withConfiguration(configuration).
                        fromExistingFile("src\\test\\resources\\containers\\historical\\BDOC.bdoc").
                        build();

        ValidationResult result = container.validate();

        boolean isSignatureValid = result.isValid();

        Assert.assertTrue(isSignatureValid);
    }
}
