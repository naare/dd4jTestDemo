package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.ContainerValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManifestTest {

    @Test
    void signedAsics_withInvalidManifest_validationError() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-S container
        String filepath = "src/test/resources/files/test/asics/1xSIG_with_invalid_manifest.asics";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(1, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals("Manifest file has an entry for file <test.txt> with mimetype <application/octet-stream> but the signature file for signature id-826a2a48c5c35283b450ea2fa6396b7b indicates the mimetype is <text/plain>", result.getContainerErrors().get(0).getMessage());
    }

    @Test
    void signedAsice_withInvalidManifest_validationError() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-E container
        String filepath = "src/test/resources/files/test/asice/TEST_ESTEID2018_ASiC-E_XAdES_LT_invalid_manifest.sce";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(1, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals("Manifest file has an entry for file <test.txt> with mimetype <application/octet-stream> but the signature file for signature id-7b66710af77da83532ae5dfa1d5ad109 indicates the mimetype is <text/plain>", result.getContainerErrors().get(0).getMessage());
    }

    @Test
    void signedAsice_withoutManifest_validationError() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-E container
        String filepath = "src/test/resources/files/test/asice/TEST_ESTEID2018_ASiC-E_XAdES_LT_missing_manifest.sce";
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(1, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals("Unsupported format: Container does not contain a manifest file", result.getContainerErrors().get(0).getMessage());
    }
}
