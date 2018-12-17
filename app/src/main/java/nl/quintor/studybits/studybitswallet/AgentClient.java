package nl.quintor.studybits.studybitswallet;

import android.os.Message;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import org.apache.commons.io.IOUtils;
import org.hyperledger.indy.sdk.IndyException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOfferList;
import nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.indy.wrapper.message.MessageType;
import nl.quintor.studybits.indy.wrapper.message.MessageTypes;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.studybitswallet.exchangeposition.AuthcryptableExchangePositions;
import nl.quintor.studybits.studybitswallet.exchangeposition.ExchangePosition;
import nl.quintor.studybits.studybitswallet.room.entity.University;

import static nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes.*;
import static nl.quintor.studybits.studybitswallet.exchangeposition.StudyBitsMessageTypes.EXCHANGE_POSITIONS;

public class AgentClient {
    public static Map<String, CookieManager> cookieManagers= new HashMap<>();

    private University university;
    private MessageEnvelopeCodec codec;

    public AgentClient(University university, MessageEnvelopeCodec codec) {
        this.university = university;
        this.codec = codec;
    }
    public static MessageEnvelope<ConnectionResponse> login(String endpoint, String username, MessageEnvelope<ConnectionRequest> envelope) {
        try {
            Log.d("STUDYBITS", "Logging in");
            URL url;
            if (username == null || "".equals(username)) {
                url = new URL(endpoint + "/agent/login");
            }
            else {
                url = new URL(endpoint + "/agent/login?student_id=" + username);
            }
            CookieManager cookieManager = cookieManagers.computeIfAbsent(endpoint, s -> new CookieManager());
            CookieHandler.setDefault(cookieManager);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);



            OutputStream out = urlConnection.getOutputStream();
            out.write(envelope.toJSON().getBytes(Charset.forName("utf8")));
            out.close();

            return MessageEnvelope.parseFromString(IOUtils.toString(urlConnection.getInputStream(), Charset.forName("utf8")), IndyMessageTypes.CONNECTION_RESPONSE);
        }
        catch (IOException e) {
            Log.e("STUDYBITS", "Exception when logging in" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<CredentialOffer> getCredentialOffers() throws IOException, IndyException, ExecutionException, InterruptedException {

        MessageEnvelope<CredentialOfferList> credentialOfferListEnvelope = this.postAndReturnMessage(getRequestEnvelope(CREDENTIAL_OFFERS), CREDENTIAL_OFFERS);

        CredentialOfferList offersList = codec.decryptMessage(credentialOfferListEnvelope).get();

        List<CredentialOffer> credentialOffers = offersList.getCredentialOffers();

        return credentialOffers;
    }

    public List<ExchangePosition> getExchangePositions() throws IOException, IndyException, ExecutionException, InterruptedException {

        MessageEnvelope<AuthcryptableExchangePositions> exchangePositionsMessageEnvelope = this.postAndReturnMessage(getRequestEnvelope(EXCHANGE_POSITIONS), EXCHANGE_POSITIONS);

        AuthcryptableExchangePositions exchangePositionsList = codec.decryptMessage(exchangePositionsMessageEnvelope).get();

        List<ExchangePosition> exchangePositions = exchangePositionsList.getExchangePositions();

        exchangePositions.forEach(exchangePosition -> {
            exchangePosition.setUniversity(university);
        });

        return exchangePositions;
    }

    public void postMessage(MessageEnvelope message) throws IOException {
        HttpURLConnection urlConnection = getConnection("/agent/message");

        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(false);
        urlConnection.setDoInput(true);

        OutputStream out = urlConnection.getOutputStream();
        out.write(message.toJSON().getBytes(Charset.forName("utf8")));
        out.close();

        Log.d("STUDYBITS", "Response code: " + urlConnection.getResponseCode());
    }

    public MessageEnvelope postAndReturnMessage(MessageEnvelope message, MessageType returnType) throws IOException {
        HttpURLConnection urlConnection = getConnection("/agent/message");

        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        OutputStream out = urlConnection.getOutputStream();
        out.write(message.toJSON().getBytes(Charset.forName("utf8")));
        out.close();

        Log.d("STUDYBITS", "Response code: " + urlConnection.getResponseCode());
        if(returnType != null ) {
            return MessageEnvelope.parseFromString(IOUtils.toString(urlConnection.getInputStream(), Charset.forName("utf8")), returnType);
        } else {
            return null;
        }

    }

    public MessageEnvelope<String> getRequestEnvelope(MessageType expectedReturn) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        return codec.encryptMessage(expectedReturn.getURN(), GET_REQUEST, university.getTheirDid()).get();
    }

    public HttpURLConnection getConnection(String path) throws IOException {
        CookieManager cookieManager = cookieManagers.computeIfAbsent(university.getEndpoint(), s -> new CookieManager());
        CookieHandler.setDefault(cookieManager);
        URL url = new URL(university.getEndpoint() + path);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        return urlConnection;
    }
}
