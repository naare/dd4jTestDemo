package org.naare.asics;

import org.digidoc4j.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.saveContainer;
import static org.naare.signing.Helpers.validationResultHasNoIssues;

class AsicsTest {

    @Test
    void createDatafileAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Create ASiC-S container
        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.ASICS)
                .withConfiguration(configuration)
                // Set datafile and its mimetype
                .withDataFile("src/test/resources/files/test.txt", "text/plain")
//                .withDataFile("src/test/resources/files/test/asics/Test_ASICS.asics", "application/vnd.etsi.asic-s+zip")
                .build();

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(0, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Test_ASICS.asics", // Timestamped, with manifest
            "1xTST_asics_no_manifest.asics", // Timestamped, no manifest
            "0xSIG_0xTST_asics.asics", // No timestamp or signature, with manifest
            "0xSIG_0xTST_asics_no_manifest.asics", // No timestamp or signature, no manifest
            "TEST_ESTEID2018_ASiC-S_XAdES_LT.scs", // Signed, no manifest
            "1xSIG_with_manifest.asics" // Signed, with manifest
    })
    void openAsicsAndValidate(String fileName) {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        // Open existing ASiC-S container
        String filepath = "src/test/resources/files/test/asics/" + fileName;
        Container container = ContainerOpener.open(filepath, configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
    }
}
