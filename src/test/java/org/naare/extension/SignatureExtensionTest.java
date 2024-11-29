package org.naare.extension;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.impl.asic.AsicSignature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.*;

class SignatureExtensionTest {

    @Test
    void extendBToTPass() {
        SignatureProfile fromProfile = SignatureProfile.B_BES;
        SignatureProfile toProfile = SignatureProfile.T;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create container with B profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        assertEquals(SignatureLevel.XAdES_BASELINE_T, container.validate().getSignatureReports().get(0).getSignatureFormat());
    }

    @Test
    void extendTToLtFail() {
        SignatureProfile fromProfile = SignatureProfile.T;
        SignatureProfile toProfile = SignatureProfile.LT;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create container with T profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        // Check expected test container was prepared
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        // Extension failed, signature profile hasn't changed
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());
        assertEquals(SignatureLevel.XAdES_BASELINE_T, container.validate().getSignatureReports().get(0).getSignatureFormat());

        // No OCSP taken
        assertEquals(0, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendLtToLtaPass() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create container with LT profile signature
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }
}
