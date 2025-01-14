package org.naare.asics;

import org.digidoc4j.Configuration;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.digidoc4j.exceptions.DuplicateTimestampException;
import org.digidoc4j.exceptions.IllegalContainerContentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsicsParsingTest {

    private Configuration configuration;

    // Create a configuration for test mode
    @BeforeEach
    void setup() {
        configuration = Configuration.of(Configuration.Mode.TEST);
    }

    @ParameterizedTest
    @CsvSource({
            "DataFileMissingAsics, Timestamped ASiC-S container must contain exactly one datafile",
            "AdditionalFolderInAsics, Timestamped ASiC-S container must contain exactly one datafile",
            "TwoDataFilesWithTst, Timestamped ASiC-S container must contain exactly one datafile",
            "TwoDataFilesWithoutSignatureOrTimestamp, ASiC-S container cannot contain more than one datafile",
            "evidencerecordXmlPresent, Unsupported evidence record entry: META-INF/evidencerecord.xml",
            "evidencerecordErsPresent, Unsupported evidence record entry: META-INF/evidencerecord.ers",
            "CadesMixedWithTst, Unsupported CAdES signature entry: META-INF/signature.p7s",
            "XadesMixedWithTst, ASiC-S container cannot contain signatures and timestamp tokens simultaneously"})
    void malformedAsics_throwsIllegalContainerContentError(String fileName, String errorMessage) {
        // Path to the malformed ASiC-S container
        String filepath = "src/test/resources/files/test/asics/" + fileName + ".asics";

        Exception exception = assertThrows(IllegalContainerContentException.class, () -> {
            ContainerOpener.open(filepath, configuration);
        });
        assertTrue(exception.getMessage().contains(errorMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "DuplicateTstTokens, Container contains duplicate timestamp token: META-INF/timestamp.tst",
            "DuplicateArchiveTstTokens, Container contains duplicate timestamp token: META-INF/timestamp002.tst",
            "DuplicateArchiveXml, Container contains duplicate timestamp manifest: META-INF/ASiCArchiveManifest.xml"})
    void asicsWithDuplicateTimestampFiles_throwsDuplicateTimestampError(String fileName, String errorMessage) {
        // Path to the malformed ASiC-S container
        String filepath = "src/test/resources/files/test/asics/" + fileName + ".asics";

        Exception exception = assertThrows(DuplicateTimestampException.class, () -> {
            ContainerOpener.open(filepath, configuration);
        });
        assertTrue(exception.getMessage().contains(errorMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "DuplicateManifestXml, Multiple manifest.xml files disallowed",
            "DuplicateMimetype, Multiple mimetype files disallowed"})
    void asicsWithDuplicateMediaFiles_throwsMultipleFilesError(String fileName, String errorMessage) {
        // Path to the malformed ASiC-S container
        String filepath = "src/test/resources/files/test/asics/" + fileName + ".asics";

        Exception exception = assertThrows(DigiDoc4JException.class, () -> {
            ContainerOpener.open(filepath, configuration);
        });
        assertTrue(exception.getMessage().contains(errorMessage));
    }
}
