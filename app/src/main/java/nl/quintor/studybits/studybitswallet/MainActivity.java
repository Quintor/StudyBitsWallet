package nl.quintor.studybits.studybitswallet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.security.NetworkSecurityPolicy;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jna.Native;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import nl.quintor.studybits.studybitswallet.credential.CredentialActivity;
import nl.quintor.studybits.studybitswallet.exchangeposition.ExchangePositionActivity;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.university.UniversityActivity;


public class MainActivity extends AppCompatActivity {
    static String ENDPOINT_RUG = "http://10.31.200.120:8080";

    static {
        Log.d("STUDYBITS", "Attempting to load indy");
        System.loadLibrary("indy");
        Log.d("STUDYBITS", "Loaded indy");
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        Log.d("STUDYBITS", "Network allowed" + NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 112;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("STUDYBITS", "Test");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final MainActivity activity = this;



        ImageButton universityButton = (ImageButton) findViewById(R.id.button_university);

        universityButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, UniversityActivity.class);
            startActivity(intent);
        });

        ImageButton credentialButton = (ImageButton) findViewById(R.id.button_credential);

        credentialButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, CredentialActivity.class);
            startActivity(intent);
        });

        ImageButton exchangePositionButton = (ImageButton) findViewById(R.id.button_exchange_position);

        exchangePositionButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ExchangePositionActivity.class);
            startActivity(intent);
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            try {
                int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }

                File indyClientDir = new File(Environment.getExternalStorageDirectory().getPath() + "/.indy_client/");

                for (File file : FileUtils.listFilesAndDirs(indyClientDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                    Log.d("STUDYBITS", "File " + file);
                }

                FileUtils.deleteDirectory(indyClientDir);

                for (String abi : Build.SUPPORTED_ABIS) {
                    Log.d("STUDYBITS", "Supported ABI: " + abi);
                }

                Log.d("STUDYBITS", "Loading other indy");
                LibIndy.API api = (LibIndy.API) Native.loadLibrary("indy", LibIndy.API.class);
                Log.d("STUDYBITS", "Indy api object: " + api);
                String poolName = PoolUtils.createPoolLedgerConfig("10.31.200.120", "testPool");

                IndyPool indyPool = new IndyPool(poolName);
                IndyWallet tempWallet = IndyWallet.create(indyPool, "student_wallet", "000000000000000000000000Student1");

                Prover prover = new Prover(tempWallet, WalletActivity.STUDENT_SECRET_NAME);
                prover.init();
                tempWallet.close();
                Log.d("STUDYBITS", "Closing tempWallet");
                indyPool.close();

                AppDatabase.getInstance(this).universityDao().delete();
                AppDatabase.getInstance(this).credentialDao().deleteAll();

                URL url = new URL(ENDPOINT_RUG + "/bootstrap/reset");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(false);
                urlConnection.setDoInput(true);

                Log.d("STUDYBITS", "Response code: " + urlConnection.getResponseCode());
                Snackbar.make(view, "Successfully reset", Snackbar.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("STUDYBITS", "Exception during reset" + e.getMessage());
                e.printStackTrace();
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    MessageEnvelope connectionRequestEnvelope = login();
//
//                    ConnectionRequest connectionRequest = JSONUtil.mapper.treeToValue(connectionRequestEnvelope.getMessage(), ConnectionRequest.class);
//
//                    Log.d("STUDYBITS", "Received connection request " + connectionRequest);
//
//                    AnoncryptedMessage anoncryptedConnectionResponse = studentWallet.acceptConnectionRequest(connectionRequest)
//                            .thenCompose(AsyncUtil.wrapException(studentWallet::anonEncrypt)).get();
//
//                    MessageEnvelope connectionResponseEnvelope = new MessageEnvelope(connectionRequest.getRequestNonce(), MessageEnvelope.MessageType.CONNECTION_RESPONSE,
//                            new TextNode(new String(Base64.encode(anoncryptedConnectionResponse.getMessage(), Base64.NO_WRAP), Charset.forName("utf8"))));
//
//                    Log.d("STUDYBITS", "Encoded base64 string: " + new String(Base64.encode(anoncryptedConnectionResponse.getMessage(), Base64.NO_WRAP), Charset.forName("utf8")));
//
//                    MessageEnvelope connectionAcknowledgementEnvelope = postAndReturnMessage(connectionResponseEnvelope);
//
//                    Log.d("STUDYBITS", "Obtained message of type " + connectionAcknowledgementEnvelope.getType());
//
//                    MessageEnvelope[] credentialOfferEnvelopes = getCredentialOffers();
//
//                    Log.d("STUDYBITS", "Obtained " + credentialOfferEnvelopes.length + " credential offers");
//
//                    AuthcryptedMessage authcryptedCredentialOffer = new AuthcryptedMessage(Base64.decode(credentialOfferEnvelopes[0].getMessage().asText(), Base64.NO_WRAP), credentialOfferEnvelopes[0].getId());
//
//                    CredentialOffer credentialOffer = studentWallet.authDecrypt(authcryptedCredentialOffer, CredentialOffer.class).get();
//
//
//                    Prover prover = new Prover(studentWallet, "master_secret_name");
//                    prover.init();
//
//                    AuthcryptedMessage authcryptedCredentialRequest = prover.createCredentialRequest(credentialOffer)
//                            .thenCompose(AsyncUtil.wrapException(prover::authEncrypt)).get();
//
//                    MessageEnvelope authcryptedCredentialRequestEnvelope = new MessageEnvelope(authcryptedCredentialRequest.getDid(), MessageEnvelope.MessageType.CREDENTIAL_REQUEST,
//                            new TextNode(new String(Base64.encode(authcryptedCredentialRequest.getMessage(), Base64.NO_WRAP), Charset.forName("utf8"))));
//
//                    MessageEnvelope credentialEnvelope = postAndReturnMessage(authcryptedCredentialRequestEnvelope);
//
//                    Log.d("STUDYBITS ", "Got message of type " + credentialEnvelope.getType());
//
//                    AuthcryptedMessage authcryptedCredential = new AuthcryptedMessage(Base64.decode(credentialEnvelope.getMessage().asText(), Base64.NO_WRAP), credentialEnvelope.getId());
//
//                    Credential credential = prover.authDecrypt(authcryptedCredential, CredentialWithRequest.class).get().getCredential();
//
//                    Log.d("STUDYBITS", "Got credential with values " + credential.getValues().toString());
//
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                    Log.d("Studybits", "Exception: " + e);
//                    Log.d("STUDYBITS", "LocalizedMessage " + e.getLocalizedMessage());
//                    Log.d("STUDYBITS", "Message" + e.getMessage());
//                    return;
//                }
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    public static String createPoolLedgerConfig(String testPoolIP, String poolName) throws IOException, InterruptedException, ExecutionException, IndyException {
        File genesisTxnFile = createGenesisTxnFile("temp.txn", testPoolIP);
        PoolJSONParameters.CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter = new PoolJSONParameters.CreatePoolLedgerConfigJSONParameter(genesisTxnFile.getAbsolutePath());
        Pool.createPoolLedgerConfig(poolName, createPoolLedgerConfigJSONParameter.toJson()).get();
        return poolName;
    }

    public static String createPoolLedgerConfig(String testPoolIP) throws IOException, InterruptedException, ExecutionException, IndyException {
        return createPoolLedgerConfig(testPoolIP, "default_pool");
    }

    private static File createGenesisTxnFile(String filename, String testPoolIp) throws IOException {
        String path = getTmpPath(filename);
        Log.d("STUDYBITS", "Writing genesis to file " + path);
        if (testPoolIp == null) {
            testPoolIp = getTestPoolIP();
        }

        String testClientIp = testPoolIp;

        String[] defaultTxns = new String[]{String.format("{\"data\":{\"alias\":\"Node1\",\"blskey\":\"4N8aUNHSgjQVgkpm8nhNEfDf6txHznoYREg9kirmJrkivgL4oSEimFF6nsQ6M41QvhM2Z33nves5vfSn9n1UwNFJBYtWVnHYMATn76vLuL3zU88KyeAYcHfsih3He6UHcXDxcaecHVz6jhCYz1P2UZn2bDVruL5wXpehgBfBaLKm3Ba\",\"client_ip\":\"%s\",\"client_port\":9702,\"node_ip\":\"%s\",\"node_port\":9701,\"services\":[\"VALIDATOR\"]},\"dest\":\"Gw6pDLhcBcoQesN72qfotTgFa7cbuqZpkX3Xo6pLhPhv\",\"identifier\":\"Th7MpTaRZVRYnPiabds81Y\",\"txnId\":\"fea82e10e894419fe2bea7d96296a6d46f50f93f9eeda954ec461b2ed2950b62\",\"type\":\"0\"}", testClientIp, testPoolIp), String.format("{\"data\":{\"alias\":\"Node2\",\"blskey\":\"37rAPpXVoxzKhz7d9gkUe52XuXryuLXoM6P6LbWDB7LSbG62Lsb33sfG7zqS8TK1MXwuCHj1FKNzVpsnafmqLG1vXN88rt38mNFs9TENzm4QHdBzsvCuoBnPH7rpYYDo9DZNJePaDvRvqJKByCabubJz3XXKbEeshzpz4Ma5QYpJqjk\",\"client_ip\":\"%s\",\"client_port\":9704,\"node_ip\":\"%s\",\"node_port\":9703,\"services\":[\"VALIDATOR\"]},\"dest\":\"8ECVSk179mjsjKRLWiQtssMLgp6EPhWXtaYyStWPSGAb\",\"identifier\":\"EbP4aYNeTHL6q385GuVpRV\",\"txnId\":\"1ac8aece2a18ced660fef8694b61aac3af08ba875ce3026a160acbc3a3af35fc\",\"type\":\"0\"}", testClientIp, testPoolIp), String.format("{\"data\":{\"alias\":\"Node3\",\"blskey\":\"3WFpdbg7C5cnLYZwFZevJqhubkFALBfCBBok15GdrKMUhUjGsk3jV6QKj6MZgEubF7oqCafxNdkm7eswgA4sdKTRc82tLGzZBd6vNqU8dupzup6uYUf32KTHTPQbuUM8Yk4QFXjEf2Usu2TJcNkdgpyeUSX42u5LqdDDpNSWUK5deC5\",\"client_ip\":\"%s\",\"client_port\":9706,\"node_ip\":\"%s\",\"node_port\":9705,\"services\":[\"VALIDATOR\"]},\"dest\":\"DKVxG2fXXTU8yT5N7hGEbXB3dfdAnYv1JczDUHpmDxya\",\"identifier\":\"4cU41vWW82ArfxJxHkzXPG\",\"txnId\":\"7e9f355dffa78ed24668f0e0e369fd8c224076571c51e2ea8be5f26479edebe4\",\"type\":\"0\"}", testClientIp, testPoolIp), String.format("{\"data\":{\"alias\":\"Node4\",\"blskey\":\"2zN3bHM1m4rLz54MJHYSwvqzPchYp8jkHswveCLAEJVcX6Mm1wHQD1SkPYMzUDTZvWvhuE6VNAkK3KxVeEmsanSmvjVkReDeBEMxeDaayjcZjFGPydyey1qxBHmTvAnBKoPydvuTAqx5f7YNNRAdeLmUi99gERUU7TD8KfAa6MpQ9bw\",\"client_ip\":\"%s\",\"client_port\":9708,\"node_ip\":\"%s\",\"node_port\":9707,\"services\":[\"VALIDATOR\"]},\"dest\":\"4PS3EDQ3dW1tci1Bp6543CfuuebjFrg36kLAUcskGfaA\",\"identifier\":\"TWwCRQRZ2ZHMJFn9TzLp7W\",\"txnId\":\"aa5e817d7cc626170eca175822029339a444eb0ee8f0bd20d3b0b76e566fb008\",\"type\":\"0\"}", testClientIp, testPoolIp)};
        File file = new File(path);
        FileUtils.forceMkdirParent(file);
        FileWriter fw = new FileWriter(file);
        String[] var6 = defaultTxns;
        int var7 = defaultTxns.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            String defaultTxn = var6[var8];
            fw.write(defaultTxn);
            fw.write("\n");
        }

        fw.close();
        return file;
    }

    static MessageEnvelope postAndReturnMessage(MessageEnvelope message) throws IOException {
        URL url = new URL(ENDPOINT_RUG + "/agent/message");
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

    static MessageEnvelope login() throws IOException {
        URL url = new URL(ENDPOINT_RUG + "/agent/login/12345678");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestMethod("POST");

        return JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), MessageEnvelope.class);
    }

    static MessageEnvelope[] getCredentialOffers() throws IOException {
        URL url = new URL(ENDPOINT_RUG + "/agent/credential_offer");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        return JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), MessageEnvelope[].class);
    }

    static String getTestPoolIP() {
        String testPoolIp = System.getenv("TEST_POOL_IP");
        return testPoolIp != null ? testPoolIp : "127.0.0.1";
    }

    static String getTmpPath() {
        return FileUtils.getTempDirectoryPath() + "/indy/";
    }

    static String getTmpPath(String filename) {
        return getTmpPath() + filename;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
