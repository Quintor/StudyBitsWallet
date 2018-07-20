package nl.quintor.studybits.studybitswallet.credential;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.studybitswallet.R;

public class CredentialActivity extends AppCompatActivity  implements CredentialFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
;
    }

    @Override
    public void onListFragmentInteraction(CredentialOrOffer credentialOrOffer) {
    }
}
