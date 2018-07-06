package studybits.quintor.nl.studybitswallet;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import nl.quintor.studybits.indy.wrapper.LookupRepository;


public class ProxyLedger implements LookupRepository {
    private String endpoint;

    public ProxyLedger(String endpoint) {
        this.endpoint = endpoint;
    }


    @Override
    public CompletableFuture<String> getKeyForDid(String did, Wallet wallet) throws IndyException {
        try {
            URL url = new URL(endpoint + "/proxy/key?did=" + did);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            return CompletableFuture.completedFuture(IOUtils.toString(urlConnection.getInputStream(), Charset.forName("utf8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public CompletableFuture<String> submitRequest(String request) throws IndyException {
        try {
            URL url = new URL(endpoint + "/proxy/request");
            Log.d("STUDYBITS", "Request we are sending: " + request);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setDoOutput(true);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(request.getBytes(Charset.forName("utf8")));
            out.close();

            return CompletableFuture.completedFuture(IOUtils.toString(urlConnection.getInputStream(), Charset.forName("utf8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<String> signAndSubmitRequest(String request, String did, Wallet wallet) throws IndyException {
        throw new NotImplementedException("signAndSubmitRequest");
    }
}

