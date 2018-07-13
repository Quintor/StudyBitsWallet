package nl.quintor.studybits.studybitswallet;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;

public class AgentClient {
    public static ConnectionRequest login(String endpoint, String username) {
        try {
            Log.d("STUDYBITS", "Logging in");
            URL url = new URL(endpoint + "/agent/login/" + username);
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

    public static MessageEnvelope postAndReturnMessage(String endpoint, MessageEnvelope message) throws IOException {
        URL url = new URL(endpoint + "/agent/message");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        OutputStream out = urlConnection.getOutputStream();
        out.write(message.toJSON().getBytes(Charset.forName("utf8")));
        out.close();

        Log.d("STUDYBITS", "Response code: " + urlConnection.getResponseCode());
        return JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), MessageEnvelope.class);
    }
}
