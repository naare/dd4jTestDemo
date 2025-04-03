package org.naare.validation;

import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.ContainerValidationResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.naare.signing.Helpers.validationResultHasNoIssues;

public class ValidationCustomConfTest {

    @Test
    public void validationWithTslV6Succeeds() throws IOException {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        /* TEST V6 TSL*/
        configuration.setLotlLocation("TEST-LOTL-with-EE_T-v6-TEST-TSL");
        /* Invalidate DD4J TSL cache and force reload */
        configuration.getTSL().invalidateCache();
        configuration.getTSL().refresh();

        /* Open existing ASiC-E container, which was signed using V5 TSL */
        String filepath = "src/test/resources/containers/historical/1_ASICE_TEST.asice";
        Container container = ContainerOpener.open(filepath, configuration);

        /* Container validation is successful */
        ContainerValidationResult result = container.validate();
        validationResultHasNoIssues(result);
    }
}
