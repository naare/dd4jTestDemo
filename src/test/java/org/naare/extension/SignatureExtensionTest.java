package org.naare.extension;

import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSException;
import org.digidoc4j.*;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.digidoc4j.impl.asic.AsicContainer;
import org.digidoc4j.impl.asic.AsicSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
    void extensionValidation_withValidContainer_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-E container
        String filepath = "src/test/resources/files/test/asice/TEST_ESTEID2018_ASiC-E_XAdES_LT.sce";
        Container container = ContainerOpener.open(filepath, configuration);

        Map<String, DigiDoc4JException> validationErrors = ((AsicContainer) container)
                .getExtensionValidationErrors(SignatureProfile.LTA);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void extensionValidation_withValidSignature_pass() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-E container
        String filepath = "src/test/resources/files/test/asice/TEST_ESTEID2018_ASiC-E_XAdES_LT.sce";
        Container container = ContainerOpener.open(filepath, configuration);

        Map<String, DigiDoc4JException> validationErrors = ((AsicContainer) container).getExtensionValidationErrors(
                SignatureProfile.LTA,
                Collections.singletonList(container.getSignatures().get(0))
        );
        assertTrue(validationErrors.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "B_BES, T",
            "B_BES, LT",
            "B_BES, LTA",
            "T, LT",
            "T, LTA",
            "LT, LTA",
            "LTA, LTA",
    })
    void extensionValidation_allowedExtensions(SignatureProfile fromProfile, SignatureProfile toProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        Map<String, DigiDoc4JException> validationErrors = ((AsicContainer) container)
                .getExtensionValidationErrors(toProfile);
        assertTrue(validationErrors.isEmpty());
    }

    @Test
    void extensionValidation_withExpiredSignature_alertException() {
        String expectedError = "Expired signature found. [S-D56EB97126A7FAEE1D3BBEB2BC63D077B557D1BB1D203B5121E5F7904B3109C9: The signing certificate has expired and there is no POE during its validity range : [2016-04-13T11:20:28Z - 2021-04-12T20:59:59Z]!]";
        SignatureProfile toProfile = SignatureProfile.LTA;
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-E container with expired signature
        String filepath = "src/test/resources/files/test/asice/asice_single_signature_with_expired_signer_and_ts_and_ocsp_certificates.asice";
        AsicContainer container = (AsicContainer) ContainerOpener.open(filepath, configuration);

        Map<String, DigiDoc4JException> validationErrors = container.getExtensionValidationErrors(toProfile);
        assertEquals(1, validationErrors.size());

        DigiDoc4JException validationError = validationErrors.get(container.getSignatures().get(0).getUniqueId());
        assertEquals("Validating the signature with DSS failed", validationError.getMessage());
        assertEquals(AlertException.class, validationError.getCause().getClass());
        assertEquals(expectedError, validationError.getCause().getMessage());

        // Try extending signature profile
        Exception exception = assertThrows(AlertException.class, () -> container.extendSignatureProfile(toProfile));
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void extensionValidation_withSignatureNotCoveringDatafile_dssException() {
        String expectedError = "Cryptographic signature verification has failed / Signature verification failed against the best candidate.";
        SignatureProfile toProfile = SignatureProfile.LTA;
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-E container with signature not covering datafile
        String filepath = "src/test/resources/files/test/asice/signature_does_not_cover_datafile.asice";
        AsicContainer container = (AsicContainer) ContainerOpener.open(filepath, configuration);

        Map<String, DigiDoc4JException> validationErrors = container.getExtensionValidationErrors(toProfile);
        assertEquals(1, validationErrors.size());

        DigiDoc4JException validationError = validationErrors.get(container.getSignatures().get(0).getUniqueId());
        assertEquals("Validating the signature with DSS failed", validationError.getMessage());
        assertEquals(DSSException.class, validationError.getCause().getClass());
        assertEquals(expectedError, validationError.getCause().getMessage());

        // Try extending signature profile
        Exception exception = assertThrows(DSSException.class, () -> container.extendSignatureProfile(toProfile));
        assertEquals(expectedError, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "LTA, LT",
            "LTA, T",
            "LTA, B_BES",
            "LTA, LT_TM",
            "LTA, B_EPES",
            "LT, LT",
            "LT, T",
            "LT, B_BES",
            "LT, LT_TM",
            "LT, B_EPES",
            "T, T",
            "T, B_BES",
            "T, LT_TM",
            "T, B_EPES",
            "B_BES, B_BES",
            "B_BES, LT_TM",
            "B_BES, B_EPES",
    })
    void extensionValidation_notAllowedExtensions(SignatureProfile fromProfile, SignatureProfile toProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        Container container = buildContainer(Container.DocumentType.ASICE, configuration);
        SignPkcs12(container, fromProfile);

        Map<String, DigiDoc4JException> validationErrors = ((AsicContainer) container).getExtensionValidationErrors(toProfile);
        assertEquals(1, validationErrors.size());

        String expectedError = "Not supported: It is not possible to extend " + fromProfile.toString()
                + " signature to " + toProfile.toString() + ".";
        DigiDoc4JException validationError = validationErrors.get(container.getSignatures().get(0).getUniqueId());
        assertEquals(expectedError, validationError.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = SignatureProfile.class, names = {"B_BES", "T", "LT", "LTA", "LT_TM", "B_EPES"})
    void extensionValidation_validTmSignature_notExtendable(SignatureProfile signatureProfile) {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing BDOC container
        String filepath = "src/test/resources/files/singleValidSignatureTM.bdoc";
        Container container = ContainerOpener.open(filepath, configuration);

        Map<String, DigiDoc4JException> validationErrors = ((AsicContainer) container)
                .getExtensionValidationErrors(signatureProfile, container.getSignatures());
        assertEquals(1, validationErrors.size());
        DigiDoc4JException validationError = validationErrors.get(container.getSignatures().get(0).getUniqueId());
        assertEquals("Not supported: It is not possible to extend LT_TM signature to "
                + signatureProfile + ".", validationError.getMessage());
    }
}
