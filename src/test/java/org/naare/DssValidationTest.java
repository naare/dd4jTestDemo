package org.naare;

import eu.europa.esig.dss.asic.cades.validation.ASiCContainerWithCAdESValidator;
import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import org.digidoc4j.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DssValidationTest {

    @Test
    public void validateContainerDss() {
        /* Set file to validate */
        DSSDocument container = new FileDocument("src/test/resources/containers/historical/1_ASICE_TEST.asice");
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);

        /* Set trusted TSL against what signature is validated */
        CommonCertificateVerifier verifier = new CommonCertificateVerifier();
        verifier.setTrustedCertSources(configuration.getTSL());

        /* Validate document and return validation reports */
        SignedDocumentValidator validator = ASiCContainerWithCAdESValidator.fromDocument(container);
        validator.setCertificateVerifier(verifier);

        Reports reports = validator.validateDocument();
        SimpleReport simpleReport = reports.getSimpleReport();
        DetailedReport detailedReport = reports.getDetailedReport();

        assertTrue(simpleReport.isValid(simpleReport.getFirstSignatureId()));
    }
}
