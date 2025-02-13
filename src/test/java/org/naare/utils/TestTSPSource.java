package org.naare.utils;

import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.utils.Utils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.tsp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class TestTSPSource implements TSPSource {

    private static final Logger LOG = LoggerFactory.getLogger(TestTSPSource.class);

    /**
     * The URL of the TSP server
     */
    private String tspServer;

    /**
     * The data loader used to retrieve the TSP response.
     */
    private DataLoader dataLoader;

    /**
     * Default constructor for TestTSPSource.
     */
    public TestTSPSource() {
        this(null);
    }

    /**
     * Constructs a TestTSPSource that will query the specified URL with a default {@code TimestampDataLoader}.
     *
     * @param tspServer the TSP URL
     */
    public TestTSPSource(final String tspServer) {
        this.tspServer = tspServer;
        this.dataLoader = new TimestampDataLoader();
        LOG.info("TestTSPSource initialized with the default data loader.");
    }

    /**
     * Sets the URL of the TSA.
     *
     * @param tspServer the TSA URL
     */
    public void setTspServer(final String tspServer) {
        this.tspServer = tspServer;
    }

    @Override
    public TimestampBinary getTimeStampResponse(final eu.europa.esig.dss.enumerations.DigestAlgorithm digestAlgorithm, final byte[] digest) throws DSSException {
        Objects.requireNonNull(dataLoader, "DataLoader is not provided!");
        LOG.info("Timestamp digest algorithm: {}", digestAlgorithm.getName());
        LOG.info("Timestamp digest value: {}", Utils.toHex(digest));

        try {
            // Set up the time stamp request
            TimeStampRequestGenerator tsqGenerator = new TimeStampRequestGenerator();
            tsqGenerator.setCertReq(true);

            ASN1ObjectIdentifier asn1ObjectIdentifier = new ASN1ObjectIdentifier(digestAlgorithm.getOid());
            TimeStampRequest timeStampRequest = tsqGenerator.generate(asn1ObjectIdentifier, digest);

            byte[] requestBytes = timeStampRequest.getEncoded();

            // Call the communications layer
            LOG.info("Making post to: {}", tspServer);
            byte[] respBytes = dataLoader.post(tspServer, requestBytes);

            // Handle the TSA response
            TimeStampResponse timeStampResponse = new TimeStampResponse(respBytes);

            // Validate token, nonce, policy id, message digest, etc.
            timeStampResponse.validate(timeStampRequest);

            TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
            return new TimestampBinary(DSSASN1Utils.getDEREncoded(timeStampToken));

        } catch (TSPException e) {
            throw new RuntimeException(String.format("Invalid TSP response: %s", e.getMessage()), e);
        } catch (IOException e) {
            throw new RuntimeException(String.format("An error occurred during timestamp request: %s", e.getMessage()), e);
        }
    }
}

