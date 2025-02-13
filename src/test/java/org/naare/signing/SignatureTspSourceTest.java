package org.naare.signing;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.SignatureProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.naare.signing.Helpers.SignPkcs12;
import static org.naare.signing.Helpers.buildContainer;

class SignatureTspSourceTest {

    @Test
    void sign_withBaltstampSignatureTspSource() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setLotlLocation("http://repo.ria/tsl/trusted-test-mp.xml");
        configuration.setTspSource("http://tsa.baltstamp.lt");

        // Create signed container
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, SignatureProfile.LT);

        // Check signature timestamp
        assertTrue(container.getSignatures().get(0).getTimeStampTokenCertificate().getSubjectName().contains("CN=BalTstamp QTSA TSU1"));

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
