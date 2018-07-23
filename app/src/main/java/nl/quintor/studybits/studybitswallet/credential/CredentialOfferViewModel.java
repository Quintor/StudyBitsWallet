package nl.quintor.studybits.studybitswallet.credential;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.Credential;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class CredentialOfferViewModel extends AndroidViewModel {
    private final MutableLiveData<List<CredentialOrOffer>> credentialOffers = new MutableLiveData<>();
    private final LiveData<List<Credential>> credentials;

    public CredentialOfferViewModel(@NonNull Application application) {
        super(application);
        credentials = AppDatabase.getInstance(this.getApplication()).credentialDao().get();
    }

    public void initCredentialOffers(List<University> universities, IndyWallet indyWallet) {
        Log.d("STUDYBITS", "Initializing credential offers");
        try {
            List<CredentialOrOffer> credentialOrOffers = new ArrayList<>();
            for (University university : universities) {
                Log.d("STUDYBITS", "Initializing credential offers for university " + university);
                URL url = new URL(university.getEndpoint() + "/agent/credential_offer");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                List<MessageEnvelope> offersForUni =
                        JSONUtil.mapper.readValue(new BufferedInputStream(urlConnection.getInputStream()), new TypeReference<List<MessageEnvelope>>() {});

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

    public LiveData<List<Credential>> getCredentials() {
        return credentials;
    }
}
