package org.naare.asics;

import org.digidoc4j.*;
import org.digidoc4j.impl.asic.asics.AsicSCompositeContainer;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
//                .withDataFile("src/test/resources/files/Test_ASICS.asics", "application/vnd.etsi.asic-s+zip")
                .build();

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(0, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        String outputFolderCreating = "src\\test\\resources\\output\\";
//        DateTime time = new DateTime();
//        container.saveAsFile(outputFolderCreating + "TEST" + "_ASICS_" + time.getMillis() + ".asics");
    }

    @Test
    void createCompositeAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        String filepath = "src\\test\\resources\\files\\Test_ASICS.asics";
        Container nestedContainer = ContainerOpener.open(filepath, configuration);
        Container container = new AsicSCompositeContainer(nestedContainer, Paths.get(filepath).getFileName().toString(), configuration);

        // Validate container
        ContainerValidationResult result = container.validate();
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getContainerErrors().size());
        assertEquals(0, result.getContainerWarnings().size());
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(1, result.getTimestampReports().size());
        assertEquals(nestedContainer.getTimestamps().get(0).getUniqueId(), result.getTimestampReports().get(0).getUniqueId());

        System.out.println(result.getReport());

        // Save container
//        String outputFolderCreating = "src\\test\\resources\\output\\";
//        DateTime time = new DateTime();
//        container.saveAsFile(outputFolderCreating + "TEST" + "_ASICS_" + time.getMillis() + ".asics");
    }
}
