package nl.quintor.studybits.studybitswallet.credential;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.hyperledger.indy.sdk.IndyException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.CredentialInfo;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.AgentClient;
import nl.quintor.studybits.studybitswallet.MainActivity;
import nl.quintor.studybits.studybitswallet.WalletActivity;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class CredentialOfferViewModel extends AndroidViewModel {
    private final MutableLiveData<List<CredentialOrOffer>> credentialOffers = new MutableLiveData<>();
    private final MutableLiveData<List<CredentialInfo>> credentials = new MutableLiveData<>();

    public CredentialOfferViewModel(@NonNull Application application) {
        super(application);
    }

    public void initCredentials(IndyWallet indyWallet) {
        Prover prover = new Prover(indyWallet, WalletActivity.STUDENT_SECRET_NAME);

        try {
            credentials.setValue(prover.findAllCredentials().get());
        } catch (InterruptedException | ExecutionException | IndyException e) {
            Log.e("STUDYBITS", "Error while refreshing credentials");
            e.printStackTrace();
        }
    }

    public void initCredentialOffers(List<University> universities, IndyWallet indyWallet) {
        Log.d("STUDYBITS", "Initializing credential offers");
        try {
            List<CredentialOrOffer> credentialOrOffers = new ArrayList<>();
            for (University university : universities) {
                Log.d("STUDYBITS", "Initializing credential offers for university " + university);
                List<MessageEnvelope> offersForUni = new AgentClient(university.getEndpoint()).getCredentialOffers();

                Log.d("STUDYBITS", "Got " + offersForUni.size() + " message envelopes with offers");

                List<CredentialOrOffer> credentialOrOffersForUni = offersForUni.stream()
                        .map(envelope -> new AuthcryptedMessage(Base64.decode(envelope.getMessage().asText(), Base64.NO_WRAP), envelope.getId()))
                        .map(AsyncUtil.wrapException(message -> indyWallet.authDecrypt(message, CredentialOffer.class).get()))
                        .map(credentialOffer -> CredentialOrOffer.fromCredentialOffer(university.getName(), credentialOffer))
                        .collect(Collectors.toList());

                credentialOrOffers.addAll(credentialOrOffersForUni);
            }


            credentialOffers.setValue(credentialOrOffers);
        }
        catch (Exception e) {
            Log.e("STUDYBITS", "Exception while getting credential offers" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public LiveData<List<CredentialOrOffer>> getCredentialOffers() {
        return credentialOffers;
    }

    public LiveData<List<CredentialInfo>> getCredentials() {
        return credentials;
    }
}
