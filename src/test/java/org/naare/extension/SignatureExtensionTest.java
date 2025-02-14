package org.naare.extension;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import org.digidoc4j.*;
import org.digidoc4j.impl.asic.AsicSignature;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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

        // Extend signature profile
        container.extendSignatureProfile(toProfile);

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());

        // No additional OCSP taken
        assertEquals(1, ((AsicSignature) container.getSignatures().get(0)).getOrigin().getOcspResponses().size());
    }

    @Test
    void extendSomeOfMany_ltToLta_successful() {
        SignatureProfile fromProfile = SignatureProfile.LT;
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create container with 3 LT profile signatures
        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);
        SignPkcs12(container, fromProfile);
        SignPkcs12(container, fromProfile);
        assertEquals(fromProfile, container.getSignatures().get(0).getProfile());

        // Extend first and third signature profile
        container.extendSignatureProfile(toProfile,
                Arrays.asList(container.getSignatures().get(0), container.getSignatures().get(2)));

        assertEquals(toProfile, container.getSignatures().get(0).getProfile());
        assertEquals(fromProfile, container.getSignatures().get(1).getProfile());
        assertEquals(toProfile, container.getSignatures().get(2).getProfile());

        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(0).getSignatureFormat());
        assertEquals(SignatureLevel.XAdES_BASELINE_LT, result.getSignatureReports().get(1).getSignatureFormat());
        assertEquals(SignatureLevel.XAdES_BASELINE_LTA, result.getSignatureReports().get(2).getSignatureFormat());
    }

    @Test
    void extendSignatureInAsicsPass() {
        SignatureProfile toProfile = SignatureProfile.LTA;

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-S container
        String filepath = "src/test/resources/files/test/asics/TEST_ESTEID2018_ASiC-S_XAdES_LT.scs";
        Container container = ContainerOpener.open(filepath, configuration);

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
