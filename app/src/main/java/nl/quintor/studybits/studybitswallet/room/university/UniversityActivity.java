package nl.quintor.studybits.studybitswallet.room.university;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.TextNode;

import org.hyperledger.indy.sdk.IndyException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.AgentClient;
import nl.quintor.studybits.studybitswallet.MainActivity;
import nl.quintor.studybits.studybitswallet.R;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class UniversityActivity extends AppCompatActivity {
    ArrayList<String> universities = new ArrayList<>();
    public static final String EXTRA_CONNECTION_REQUEST = "nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest";

    private IndyWallet studentWallet;
    private IndyPool indyPool;

    private RecyclerView universityRecyclerView;
    private UniversityRecyclerViewAdapter universityAdapter;
    private RecyclerView.LayoutManager universityLayoutManager;
    private UniversityListViewModel universityListViewModel;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            indyPool = new IndyPool("testPool");
            studentWallet = IndyWallet.open(indyPool, "student_wallet", MainActivity.STUDENT_DID);
        } catch (IndyException | ExecutionException | InterruptedException | JsonProcessingException e) {
            Log.e("STUDYBITS", "Exception on resume" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            studentWallet.close();
            indyPool.close();
        } catch (Exception e) {
            Log.e("STUDYBITS", "Exception on pause" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_university);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final UniversityActivity activity = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectUniversityDialogFragment dialogFragment = new ConnectUniversityDialogFragment();

                dialogFragment.setConnectDialogListener(() -> {
                    String endpoint = dialogFragment.getEndpoint().getText().toString();
                    String username = dialogFragment.getUsername().getText().toString();
                    ConnectionRequest connectionRequest = AgentClient.login(endpoint, username);
                    try {
                        AnoncryptedMessage anoncryptedConnectionResponse = studentWallet.acceptConnectionRequest(connectionRequest)
                                .thenCompose(AsyncUtil.wrapException(studentWallet::anonEncrypt)).get();

                        MessageEnvelope connectionResponseEnvelope = new MessageEnvelope(connectionRequest.getRequestNonce(), MessageEnvelope.MessageType.CONNECTION_RESPONSE,
                            new TextNode(new String(Base64.encode(anoncryptedConnectionResponse.getMessage(), Base64.NO_WRAP), Charset.forName("utf8"))));
                        MessageEnvelope connectionAcknowledgementEnvelope = AgentClient.postAndReturnMessage(endpoint, connectionResponseEnvelope);

                        String uniName = connectionAcknowledgementEnvelope.getMessage().toString();

                        University university = new University(uniName, endpoint, connectionRequest.getDid());

                        AppDatabase.getInstance(getApplicationContext()).universityDao().insertUniversities(university);

                    } catch (Exception e) {
                        Log.e("STUDYBITS", "Exception on accepting connection request" + e.getMessage());

                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "connect");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        universityRecyclerView = findViewById(R.id.university_recycler_view);

        universityLayoutManager = new LinearLayoutManager(this);
        universityRecyclerView.setLayoutManager(universityLayoutManager);

        universityAdapter = new UniversityRecyclerViewAdapter(this, new ArrayList<>());

        universityRecyclerView.setAdapter(universityAdapter);

        universityListViewModel = ViewModelProviders.of(this).get(UniversityListViewModel.class);

        universityListViewModel.getUniversityList().observe(UniversityActivity.this, universities -> {
            universityAdapter.setData(universities);
        });


    }


}
