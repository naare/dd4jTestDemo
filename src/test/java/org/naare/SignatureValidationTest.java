package org.naare;

import org.apache.commons.io.FileUtils;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.ValidationResult;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureValidationTest {

    @Test
    public void validateSignatureTest() throws IOException {

        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Invalidate local TSL cache and force refresh it */
//        configuration.getTSL().invalidateCache();
//        configuration.getTSL().refresh();

        /* Set dummy TSL refresh callback */
        configuration.setTslRefreshCallback(summary -> true);

        /* Set custom LOTL Truststore*/
//        configuration.setLotlTruststorePath("src\\test\\resources\\conf\\PROD-lotl-truststore.p12");
//        configuration.setLotlTruststorePassword("digidoc4j-password");
//        configuration.setLotlTruststoreType("PKCS12");

        /* Set LOTL URL */
//        configuration.setLotlLocation("<INSERT LOTL URL HERE>");
//        configuration.setTslLocation("<INSERT LOTL URL HERE>"); //4.3.0 ja older versions

        /* Load custom DD4J conf */
//        configuration.loadConfiguration("src\\test\\resources\\conf\\digidoc4j-test.yaml");

        /* Filtering trusted territories in Java */
//        configuration.setTrustedTerritories("suvaline", "EE");

        /* Set required terrotories which TSL loading must succeed */
//        configuration.setRequiredTerritories("suvaline");

        /* Set TSL cache validity time */
//        long i = 3000;
//        configuration.setTslCacheExpirationTime(i);

        Container container = ContainerBuilder.
                aContainer().withConfiguration(configuration).
                fromExistingFile("src/test/resources/containers/historical/TEST_Plus_SignedWithData_10062024_72713780.asice").
                build();

        /* Reading a file to a stream */
//        InputStream inputStream = FileUtils.openInputStream(new File("src/test/resources/..."));

        /* Open container from a stream */
//        Container container = ContainerOpener.open(inputStream, true); // With big files support enabled

        ValidationResult result = container.validate();

        boolean isSignatureValid = result.isValid();

        List<DigiDoc4JException> validationErrors = result.getErrors();
        List<DigiDoc4JException> validationWarnings = result.getWarnings();
        List<DigiDoc4JException> containerErrors = ((ContainerValidationResult) result).getContainerErrors();
        List<DigiDoc4JException> containerWarnings = ((ContainerValidationResult) result).getContainerWarnings();

        /* See the validation report in XML (for debugging only - DO NOT USE YOUR APPLICATION LOGIC ON IT) */
        String validationReport = ((ContainerValidationResult) result).getReport();

        assertTrue(isSignatureValid);
    }
}
