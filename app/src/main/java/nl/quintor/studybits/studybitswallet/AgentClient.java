package nl.quintor.studybits.studybitswallet;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.studybitswallet.exchangeposition.ExchangePosition;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class AgentClient {
    public static Map<String, CookieManager> cookieManagers= new HashMap<>();

    private String endpoint;

    public AgentClient(String endpoint) {
        this.endpoint = endpoint;
    }
    public ConnectionRequest login(String username) {
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

            MessageEnvelope connectionRequestEnvelope = JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), MessageEnvelope.class);

            return JSONUtil.mapper.treeToValue(connectionRequestEnvelope.getMessage(), ConnectionRequest.class);
        }
        catch (IOException e) {
            Log.e("STUDYBITS", "Exception when logging in" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<MessageEnvelope> getCredentialOffers() throws IOException {
        HttpURLConnection urlConnection = getConnection("/agent/credential_offer");

        return JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), new TypeReference<List<MessageEnvelope>>() {});
    }

    public List<ExchangePosition> getExchangePositions(University university) throws IOException {
        HttpURLConnection urlConnection = getConnection("/agent/exchange_position");


        List<ExchangePosition> exchangePositions = JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), new TypeReference<List<ExchangePosition>>() {});

        exchangePositions.forEach(exchangePosition -> exchangePosition.setUniversity(university));

        return exchangePositions;
    }


    public MessageEnvelope postAndReturnMessage(MessageEnvelope message) throws IOException {
        HttpURLConnection urlConnection = getConnection("/agent/message");

        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        OutputStream out = urlConnection.getOutputStream();
        out.write(message.toJSON().getBytes(Charset.forName("utf8")));
        out.close();

        Log.d("STUDYBITS", "Response code: " + urlConnection.getResponseCode());
        return JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), MessageEnvelope.class);
    }

    public HttpURLConnection getConnection(String path) throws IOException {
        CookieManager cookieManager = cookieManagers.computeIfAbsent(endpoint, s -> new CookieManager());
        CookieHandler.setDefault(cookieManager);
        URL url = new URL(endpoint + path);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        return urlConnection;
    }
}
