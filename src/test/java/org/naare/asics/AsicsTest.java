package org.naare.asics;

import org.digidoc4j.*;
import org.junit.jupiter.api.Test;

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
}
