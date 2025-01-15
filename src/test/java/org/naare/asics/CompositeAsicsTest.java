package org.naare.asics;

import org.digidoc4j.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.naare.signing.Helpers.saveContainer;
import static org.naare.signing.Helpers.validationResultHasNoIssues;

class CompositeAsicsTest {

    @Test
    void createCompositeAsicsWithTimestampedAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        String filepath = "src/test/resources/files/test/asics/Test_ASICS.asics";
        Container nestedContainer = ContainerOpener.open(filepath, configuration);
        CompositeContainer container = CompositeContainerBuilder
                .fromContainer(nestedContainer, Paths.get(filepath).getFileName().toString())
                .buildTimestamped(timestampBuilder -> {});

        // Check nesting
        assertEquals("ASICS", container.getNestedContainerType());
        assertEquals(1, container.getNestingContainerDataFiles().size());
        assertEquals(0, container.getNestingContainerSignatures().size());
        assertEquals(1, container.getNestingContainerTimestamps().size());
        assertEquals(1, container.getNestedContainerDataFiles().size());
        assertEquals(0, container.getNestedContainerSignatures().size());
        assertEquals(1, container.getNestedContainerTimestamps().size());
        checkCompositeContainerNesting(container, nestedContainer);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(2, result.getTimestampReports().size());

        // Validate timestamps
        // Nested ASIC-S original timestamp
        String timestampId = nestedContainer.getTimestamps().get(0).getUniqueId();
        assertTrue((result.getValidationResult(timestampId).isValid()));
        assertEquals(timestampId, result.getTimestampReports().get(0).getUniqueId());
        assertEquals(timestampId, container.getNestedContainerTimestamps().get(0).getUniqueId());
        // Nesting ASIC-S added timestamp
        String timestampId2 = container.getTimestamps().get(0).getUniqueId();
        assertTrue((result.getValidationResult(timestampId2).isValid()));
        assertEquals(timestampId2, result.getTimestampReports().get(1).getUniqueId());
        assertEquals(timestampId2, container.getNestingContainerTimestamps().get(0).getUniqueId());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void createCompositeAsicsWithSignedAsicsAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        String filepath = "src/test/resources/files/test/asics/TEST_ESTEID2018_ASiC-S_XAdES_LT.scs";
        Container nestedContainer = ContainerOpener.open(filepath, configuration);
        CompositeContainer container = CompositeContainerBuilder
                .fromContainer(nestedContainer, Paths.get(filepath).getFileName().toString())
                .buildTimestamped(timestampBuilder -> {});

        // Check nesting
        assertEquals("ASICS", container.getNestedContainerType());
        assertEquals(1, container.getNestingContainerDataFiles().size());
        assertEquals(0, container.getNestingContainerSignatures().size());
        assertEquals(1, container.getNestingContainerTimestamps().size());
        assertEquals(1, container.getNestedContainerDataFiles().size());
        assertEquals(1, container.getNestedContainerSignatures().size());
        assertEquals(0, container.getNestedContainerTimestamps().size());
        checkCompositeContainerNesting(container, nestedContainer);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getSignatureReports().size());
        assertEquals(1, result.getTimestampReports().size());

        // Validate signature
        String signatureId = result.getSignatureReports().get(0).getUniqueId();
        assertTrue((result.getValidationResult(signatureId).isValid()));
        assertEquals(signatureId, nestedContainer.getSignatures().get(0).getUniqueId());
        assertEquals(signatureId, container.getNestedContainerSignatures().get(0).getUniqueId());

        // Validate nesting ASIC-S added timestamp
        String timestampId = container.getTimestamps().get(0).getUniqueId();
        assertTrue((result.getValidationResult(timestampId).isValid()));
        assertEquals(timestampId, result.getTimestampReports().get(0).getUniqueId());
        assertEquals(timestampId, container.getNestingContainerTimestamps().get(0).getUniqueId());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void createCompositeAsicsWithAsicsAndValidate_addedTimestampToNestedContainer() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        String filepath = "src/test/resources/files/test/asics/Test_ASICS.asics";
        Container nestedContainer = ContainerOpener.open(filepath, configuration);
        nestedContainer.addTimestamp(TimestampBuilder.aTimestamp(nestedContainer).invokeTimestamping());
        CompositeContainer container = CompositeContainerBuilder
                .fromContainer(nestedContainer, Paths.get(filepath).getFileName().toString())
                .buildTimestamped(timestampBuilder -> {});

        // Check nesting
        assertEquals("ASICS", container.getNestedContainerType());
        assertEquals(1, container.getNestingContainerDataFiles().size());
        assertEquals(0, container.getNestingContainerSignatures().size());
        assertEquals(1, container.getNestingContainerTimestamps().size());
        assertEquals(1, container.getNestedContainerDataFiles().size());
        assertEquals(0, container.getNestedContainerSignatures().size());
        assertEquals(2, container.getNestedContainerTimestamps().size());
        checkCompositeContainerNesting(container, nestedContainer);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(3, result.getTimestampReports().size());

        // Validate timestamps
        // Nested ASIC-S original timestamp
        String timestampId = nestedContainer.getTimestamps().get(0).getUniqueId();
        assertTrue((result.getValidationResult(timestampId).isValid()));
        assertEquals(timestampId, result.getTimestampReports().get(0).getUniqueId());
        assertEquals(timestampId, container.getNestedContainerTimestamps().get(0).getUniqueId());
        // Nested ASIC-S added timestamp
        String timestampId2 = nestedContainer.getTimestamps().get(1).getUniqueId();
        assertTrue((result.getValidationResult(timestampId2).isValid()));
        assertEquals(timestampId2, result.getTimestampReports().get(1).getUniqueId());
        assertEquals(timestampId2, container.getNestedContainerTimestamps().get(1).getUniqueId());
        // Nesting ASIC-S added timestamp
        String timestampId3 = container.getTimestamps().get(0).getUniqueId();
        assertTrue((result.getValidationResult(timestampId3).isValid()));
        assertEquals(timestampId3, result.getTimestampReports().get(2).getUniqueId());
        assertEquals(timestampId3, container.getNestingContainerTimestamps().get(0).getUniqueId());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void createCompositeAsicsWithAsiceAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        String filepath = "src/test/resources/files/test/asice/TEST_ESTEID2018_ASiC-E_XAdES_LT.sce";
        Container nestedContainer = ContainerOpener.open(filepath, configuration);
        CompositeContainer container = CompositeContainerBuilder
                .fromContainer(nestedContainer, Paths.get(filepath).getFileName().toString())
                .buildTimestamped(timestampBuilder -> {});

        // Check nesting
        assertEquals("ASICE", container.getNestedContainerType());
        assertEquals(1, container.getNestingContainerDataFiles().size());
        assertEquals(0, container.getNestingContainerSignatures().size());
        assertEquals(1, container.getNestingContainerTimestamps().size());
        assertEquals(1, container.getNestedContainerDataFiles().size());
        assertEquals(1, container.getNestedContainerSignatures().size());
        assertEquals(0, container.getNestedContainerTimestamps().size());
        checkCompositeContainerNesting(container, nestedContainer);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(1, result.getSignatureReports().size());
        assertEquals(1, result.getTimestampReports().size());

        // Validate timestamp
        String timestampId = result.getTimestampReports().get(0).getUniqueId();
        assertTrue((result.getValidationResult(timestampId).isValid()));
        assertEquals(timestampId, container.getTimestamps().get(0).getUniqueId());
        assertEquals(timestampId, container.getNestingContainerTimestamps().get(0).getUniqueId());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    @Test
    void createCompositeAsicsWithDdocAndValidate() {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        String filepath = "src/test/resources/files/DIGIDOC-XML1.3.ddoc";
        Container nestedContainer = ContainerOpener.open(filepath, configuration);
        CompositeContainer container = CompositeContainerBuilder
                .fromContainer(nestedContainer, Paths.get(filepath).getFileName().toString())
                .buildTimestamped(timestampBuilder -> {});

        // Check nesting
        assertEquals("DDOC", container.getNestedContainerType());
        assertEquals(1, container.getNestingContainerDataFiles().size());
        assertEquals(0, container.getNestingContainerSignatures().size());
        assertEquals(1, container.getNestingContainerTimestamps().size());
        assertEquals(1, container.getNestedContainerDataFiles().size());
        assertEquals(1, container.getNestedContainerSignatures().size());
        assertEquals(0, container.getNestedContainerTimestamps().size());
        checkCompositeContainerNesting(container, nestedContainer);

        // Validate container
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
        assertEquals(0, result.getSignatureReports().size());
        assertEquals(1, result.getTimestampReports().size());

        System.out.println(result.getReport());

        // Save container
//        saveContainer(container);
    }

    void checkCompositeContainerNesting(CompositeContainer container, Container nestedContainer) {
        assertAll(
                () -> assertEquals("ASICS", container.getType()),
                () -> assertEquals(container.getDataFiles().stream().map(DataFile::getId).collect(Collectors.toList()),
                        container.getNestingContainerDataFiles().stream().map(DataFile::getId).collect(Collectors.toList())),
                () -> assertEquals(container.getSignatures().stream().map(Signature::getId).collect(Collectors.toList()),
                        container.getNestingContainerSignatures().stream().map(Signature::getId).collect(Collectors.toList())),
                () -> assertEquals(container.getTimestamps().stream().map(Timestamp::getUniqueId).collect(Collectors.toList()),
                        container.getNestingContainerTimestamps().stream().map(Timestamp::getUniqueId).collect(Collectors.toList())),
                () -> assertEquals(nestedContainer.getDataFiles().stream().map(DataFile::getId).collect(Collectors.toList()),
                        container.getNestedContainerDataFiles().stream().map(DataFile::getId).collect(Collectors.toList())),
                () -> assertEquals(nestedContainer.getSignatures().stream().map(Signature::getId).collect(Collectors.toList()),
                        container.getNestedContainerSignatures().stream().map(Signature::getId).collect(Collectors.toList())),
                () -> assertEquals(nestedContainer.getTimestamps().stream().map(Timestamp::getUniqueId).collect(Collectors.toList()),
                        container.getNestedContainerTimestamps().stream().map(Timestamp::getUniqueId).collect(Collectors.toList()))
        );
    }
}
