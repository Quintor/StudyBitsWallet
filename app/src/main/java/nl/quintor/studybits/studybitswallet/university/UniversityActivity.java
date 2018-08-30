package nl.quintor.studybits.studybitswallet.university;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.io.IOException;
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
import nl.quintor.studybits.studybitswallet.IndyClient;
import nl.quintor.studybits.studybitswallet.MainActivity;
import nl.quintor.studybits.studybitswallet.R;
import nl.quintor.studybits.studybitswallet.WalletActivity;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class UniversityActivity extends WalletActivity {
    ArrayList<String> universities = new ArrayList<>();
    public static final String EXTRA_CONNECTION_REQUEST = "nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest";

    private RecyclerView universityRecyclerView;
    private UniversityRecyclerViewAdapter universityAdapter;
    private RecyclerView.LayoutManager universityLayoutManager;
    private UniversityListViewModel universityListViewModel;

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
                    String endpoint = dialogFragment.getEndpointText();
                    String username = dialogFragment.getUsernameText();
                    Log.d("STUDYBITS", "Logging in with endpoint " + endpoint + " and username " + username);
                    AgentClient agentClient = new AgentClient(endpoint);
                    ConnectionRequest connectionRequest = agentClient.login(username);
                    try {
                        IndyClient indyClient = new IndyClient(studentWallet, AppDatabase.getInstance(getApplicationContext()));
                        University university = indyClient.connect(endpoint, agentClient, connectionRequest);

                        Snackbar.make(view, "Connected to " + university.getName() + "!", Snackbar.LENGTH_SHORT).show();
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