package nl.quintor.studybits.studybitswallet;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.hyperledger.indy.sdk.IndyException;

import java.util.concurrent.ExecutionException;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;

public abstract class WalletActivity extends AppCompatActivity {
    protected static IndyPool indyPool;
    protected static IndyWallet studentWallet;

    public static final String STUDENT_DID = "Xepuw1Y1k9DpvoSvZaoVJr";
    public static final String STUDENT_SECRET_NAME = "student_secret_name";

    @Override
    protected void onResume() {
        super.onResume();
        try {
            indyPool = new IndyPool("testPool");
            studentWallet = IndyWallet.open(indyPool, "student_wallet", STUDENT_DID);
        } catch (IndyException | ExecutionException | InterruptedException | JsonProcessingException e) {
            Log.e("STUDYBITS", "Exception on resume " + e.getMessage());
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
}
