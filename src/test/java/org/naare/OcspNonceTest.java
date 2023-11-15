package org.naare;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import org.digidoc4j.Configuration;
import org.digidoc4j.Constant;
import org.digidoc4j.impl.CommonOCSPSource;
import org.digidoc4j.impl.SkOCSPDataLoader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class OcspNonceTest {

    @Test
    public void getOcspForCert() {
        Configuration configuration = Configuration.of(Configuration.Mode.PROD);
        configuration.setPreferAiaOcsp(true);

        CommonOCSPSource ocspSource = new CommonOCSPSource(configuration);
        SkOCSPDataLoader ocspDataLoader = new SkOCSPDataLoader(configuration);
        ocspDataLoader.setUserAgent(Constant.USER_AGENT_STRING);
        ocspSource.setDataLoader(ocspDataLoader);

        CertificateToken signer = new CertificateToken(getSigningCertificate());
        CertificateToken issuer = configuration.getTSL().getCertificates().stream()
                .filter(signer::isSignedBy)
                .findFirst()
                .orElseThrow(NullPointerException::new);

        OCSPToken ocspToken = ocspSource.getRevocationToken(signer, issuer);
        System.out.println(ocspToken);
    }

    private static X509Certificate getSigningCertificate() {
        try (InputStream in = new ByteArrayInputStream(SIGNING_CERTIFICATE_PEM.getBytes(StandardCharsets.UTF_8))) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load certificate", e);
        }
    }

    private static final String SIGNING_CERTIFICATE_PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIIKsjCCCZqgAwIBAgIOAc7/+M67IPVHiYiDCAowDQYJKoZIhvcNAQELBQAwejEL" +
            "MAkGA1UEBhMCSFUxETAPBgNVBAcMCEJ1ZGFwZXN0MRYwFAYDVQQKDA1NaWNyb3Nl" +
            "YyBMdGQuMRcwFQYDVQRhDA5WQVRIVS0yMzU4NDQ5NzEnMCUGA1UEAwweUXVhbGlm" +
            "aWVkIGUtU3ppZ25vIFRMUyBDQSAyMDE4MB4XDTIxMTAxOTExNDc0NVoXDTIyMTAx" +
            "OTExNDc0NVowgbkxEzARBgsrBgEEAYI3PAIBAxMCTFQxHTAbBgNVBA8MFFByaXZh" +
            "dGUgT3JnYW5pemF0aW9uMRIwEAYDVQQFEwkzMDUyMDUxMjIxCzAJBgNVBAYTAkxU" +
            "MRAwDgYDVQQHDAdWaWxuaXVzMR0wGwYDVQQKDBRNb250b25pbyBGaW5hbmNlIFVB" +
            "QjEaMBgGA1UEYQwRUFNETFQtQkwtTEIwMDIwMDcxFTATBgNVBAMMDG1vbnRvbmlv" +
            "LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANGm0wcWXkWoxiLK" +
            "70/iyfZQkgz6dVX59XCMSXuaxCRdkL1DXqpr+y0pVClpRJ6oYeymcjCGYkB52hYb" +
            "cp2nuOjhoHsN7S9d3hR4riCGVv6GILbvnZQsSWcIT1FiXJtIcBrHljp+uB2082UT" +
            "6JJYwOrpPlnTzY2/Vq4vVoa2OPYMC5lo+3r1fc+OicPjjp/rFBvVbG5VUFmXcJO3" +
            "IuQKck5oWZVkICM9ogfTVlS3xMBeFI4FdzG33WQnkwh1+uZaPhtRYmGCcbVGonHR" +
            "7+IeL+12YYQm179loNJdQozv6dtHC8g8KLpo3kcHCdQvvDuPb5FhMCbgp3tEHVXX" +
            "5x6YIwMCAwEAAaOCBvQwggbwMA4GA1UdDwEB/wQEAwIFoDCCAX4GCisGAQQB1nkC" +
            "BAIEggFuBIIBagFoAHYAKXm+8J45OSHwVnOfY6V35b5XfZxgCvj5TV0mXCVdx4QA" +
            "AAF8mGEDbAAABAMARzBFAiEA3MdYNEU4NOuNhT/D9yPmbgx1GixID5qdUAn6CARt" +
            "XQkCIG4Is3DeMS3vEiW5xcdjpML0MlycEUZ4gVas9MbrxGR+AHYAVYHUwhaQNgFK" +
            "6gubVzxT8MDkOHhwJQgXL6OqHQcT0wwAAAF8mGEGUwAABAMARzBFAiARmN5ntEWS" +
            "ZQpHtJvrjgxfRooOOOr65ChZDnsMrFBUgwIhAJRlOtT3Zn6V/lwH6A+cX1xJjwQ8" +
            "9eaSfnjzenFBNM1CAHYAQcjKsd8iRkoQxqE6CUKHXk4xixsD6+tLx2jwkGKWBvYA" +
            "AAF8mGEIYwAABAMARzBFAiBUbvZPfZMh+UnIj+01kWJRucmgOenSPzcCVngOozTn" +
            "WAIhAMW7uVgd0ab8wSjaJcgrZ0Is7QAusIu+ilHJF3/eZDDSMB0GA1UdJQQWMBQG" +
            "CCsGAQUFBwMCBggrBgEFBQcDATCCAcEGA1UdIASCAbgwggG0MIIBnAYPKwYBBAGB" +
            "qBgCAQGBKgIWMIIBhzAmBggrBgEFBQcCARYaaHR0cDovL2NwLmUtc3ppZ25vLmh1" +
            "L3FjcHMweAYIKwYBBQUHAgIwbAxqUXVhbGlmaWVkIFBTRDIgY2VydGlmaWNhdGUg" +
            "Zm9yIHdlYnNpdGUgYXV0aGVudGljYXRpb24uIFRoZSBjZXJ0aWZpY2F0ZSBpcyBh" +
            "c3NvY2lhdGVkIHdpdGggYW4gb3JnYW5pemF0aW9uLjAzBggrBgEFBQcCAjAnDCVF" +
            "eHRlbmRlZCBWYWxpZGF0aW9uIChFVikgY2VydGlmaWNhdGUuMHEGCCsGAQUFBwIC" +
            "MGUMY01pbsWRc8OtdGV0dCBQU0QyIHdlYm9sZGFsLWhpdGVsZXPDrXTFkSB0YW7D" +
            "unPDrXR2w6FueS4gQSB0YW7DunPDrXR2w6FueSBzemVydmV6ZXRoZXoga2FwY3Nv" +
            "bMOzZGlrLjA7BggrBgEFBQcCAjAvDC1Gb2tvem90dGFuIGVsbGVuxZFyesO2dHQg" +
            "KEVWKSB0YW7DunPDrXR2w6FueS4wCQYHBACBmCcDATAHBgVngQwBATAdBgNVHQ4E" +
            "FgQUoEfYdfyykXBnT2tFumEwKrzAVxwwHwYDVR0jBBgwFoAUfYROwtRr6sHXIoxo" +
            "w+mg9OyYihwwFwYDVR0RBBAwDoIMbW9udG9uaW8uY29tMIG2BgNVHR8Ega4wgasw" +
            "N6A1oDOGMWh0dHA6Ly9xdGxzY2EyMDE4LWNybDEuZS1zemlnbm8uaHUvcXRsc2Nh" +
            "MjAxOC5jcmwwN6A1oDOGMWh0dHA6Ly9xdGxzY2EyMDE4LWNybDIuZS1zemlnbm8u" +
            "aHUvcXRsc2NhMjAxOC5jcmwwN6A1oDOGMWh0dHA6Ly9xdGxzY2EyMDE4LWNybDMu" +
            "ZS1zemlnbm8uaHUvcXRsc2NhMjAxOC5jcmwwggFfBggrBgEFBQcBAQSCAVEwggFN" +
            "MC8GCCsGAQUFBzABhiNodHRwOi8vcXRsc2NhMjAxOC1vY3NwMS5lLXN6aWduby5o" +
            "dTAvBggrBgEFBQcwAYYjaHR0cDovL3F0bHNjYTIwMTgtb2NzcDIuZS1zemlnbm8u" +
            "aHUwLwYIKwYBBQUHMAGGI2h0dHA6Ly9xdGxzY2EyMDE4LW9jc3AzLmUtc3ppZ25v" +
            "Lmh1MDwGCCsGAQUFBzAChjBodHRwOi8vcXRsc2NhMjAxOC1jYTEuZS1zemlnbm8u" +
            "aHUvcXRsc2NhMjAxOC5jcnQwPAYIKwYBBQUHMAKGMGh0dHA6Ly9xdGxzY2EyMDE4" +
            "LWNhMi5lLXN6aWduby5odS9xdGxzY2EyMDE4LmNydDA8BggrBgEFBQcwAoYwaHR0" +
            "cDovL3F0bHNjYTIwMTgtY2EzLmUtc3ppZ25vLmh1L3F0bHNjYTIwMTguY3J0MCEG" +
            "BWeBDAMBBBgwFhMDUFNEEwJMVAwLQkwtTEIwMDIwMDcwgd8GCCsGAQUFBwEDBIHS" +
            "MIHPMAgGBgQAjkYBATALBgYEAI5GAQMCAQowUwYGBACORgEFMEkwJBYeaHR0cHM6" +
            "Ly9jcC5lLXN6aWduby5odS9xY3BzX2VuEwJlbjAhFhtodHRwczovL2NwLmUtc3pp" +
            "Z25vLmh1L3FjcHMTAmh1MBMGBgQAjkYBBjAJBgcEAI5GAQYDMEwGBgQAgZgnAjBC" +
            "MCYwEQYHBACBmCcBAgwGUFNQX1BJMBEGBwQAgZgnAQMMBlBTUF9BSQwRQmFuayBv" +
            "ZiBMaXRodWFuaWEMBUxULUJMMA0GCSqGSIb3DQEBCwUAA4IBAQAl1Ou1Hvzwi3G4" +
            "9GMkFK1kCmtF0EAHoGfCqq2g+qb/CSlB0WDwbzt8kYST71IoVyzhE8ehLbJA/qQv" +
            "iXg4NpJlmYoNziqHQkXh4pKScTPXhN6FRvKxQOYydBEUiRTXxscz4GYI8Ew9lyOB" +
            "AXtz+85Dh8xAussfNaVJ73PV8RdrkTPGKu+CqYTctENHr5Czsqli/jMubUTI7Rth" +
            "H0LNkL3tGzTXAskqaYeq/3jbSMIru2BqgTx/HyPrfBzUazH/EcA+yIdjO5WZfg4i" +
            "BMZTdFm78ubXwOA27BK2lReEfYC5cVO6TNO4OHuCXK9osEiCP0p2lWF3Jbv78AxH" +
            "fp5yeQhg" +
            "\n-----END CERTIFICATE-----";

}
