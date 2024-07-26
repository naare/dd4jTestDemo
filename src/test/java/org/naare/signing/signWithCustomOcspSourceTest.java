package org.naare.signing;

import eu.europa.esig.dss.spi.client.http.DataLoader;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerValidationResult;
import org.digidoc4j.DataToSign;
import org.digidoc4j.SignatureBuilder;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.OcspDataLoaderFactory;
import org.digidoc4j.impl.SKOnlineOCSPSource;
import org.digidoc4j.impl.asic.report.ContainerValidationReport;
import org.digidoc4j.signers.PKCS11SignatureToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.naare.signing.Helpers.getDateTime;
import static org.naare.signing.Helpers.getDefaultPkcs11SignatureToken;

public class signWithCustomOcspSourceTest {

    String outputFolderCreating = "src\\test\\resources\\output\\signatureCreation\\";

    @Test
    public void setOcspSourceNullAndSigningFails() {
        Configuration configuration = Configuration.of(Configuration.Mode.TEST);
        configuration.setSigningOcspSourceFactory(()-> null);

//        SKOnlineOCSPSource source = new CommonOCSPSource(configuration);
//        DataLoader loader = new OcspDataLoaderFactory(configuration).create();
//        source.setDataLoader(loader);
//        configuration.setExtendingOcspSourceFactory(() -> source); //ainult laiendamisel (LT -> LTA, LTA -> LTA) Kas B ja T taseme laiendamise korral rakendub see? Kui ei määra, siis fallback on null
//        testConfiguration.setSigningOcspSourceFactory(); //ainult signeerimisel, kui ei määra, siis fallback CommonOCSPSource peale
//        testConfiguration.setOcspSource(); //kui tegemist ei ole AIA-ga, siis saab määrata OCSP URL-i
//        testConfiguration.setSignOCSPRequests();
//        testConfiguration.setSigningOcspSourceFactory(()-> null); määramaks väärtuseks null

        Container container = ContainerBuilder
                .aContainer(Container.DocumentType.ASICE)
                .withConfiguration(configuration)
                .withDataFile("src/test/resources/files/test.txt", "application/octet-stream")
                .build();

        /* Sign with ID card using IDEMIA driver */
        PKCS11SignatureToken signatureToken = getDefaultPkcs11SignatureToken("12345");

        DataToSign dataToSign = SignatureBuilder
                .aSignature(container)
                .withSigningCertificate(signatureToken.getCertificate())
                .withSignatureProfile(SignatureProfile.LT)
                .buildDataToSign();

        byte[] signatureValue = signatureToken.sign(dataToSign.getDigestAlgorithm(), dataToSign.getDataToSign());
        org.digidoc4j.Signature signature = dataToSign.finalize(signatureValue);
        container.addSignature(signature);

//        ContainerValidationResult result = container.validate();
//        String validationReport = result.getReport();
//
//        container.saveAsFile(outputFolderCreating + "TEST_PKCS11_Signature_" + "LT" + "_" + getDateTime() + ".asice");
//
//        assertEquals(container.getSignatures().size(), 1);
    }
}
