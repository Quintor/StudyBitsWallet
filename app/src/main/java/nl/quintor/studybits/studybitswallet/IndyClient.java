package nl.quintor.studybits.studybitswallet;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.node.TextNode;

import org.hyperledger.indy.sdk.IndyException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionResponse;
import nl.quintor.studybits.indy.wrapper.dto.Credential;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.dto.CredentialRequest;
import nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest;
import nl.quintor.studybits.indy.wrapper.dto.Proof;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.exchangeposition.ExchangePosition;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class IndyClient {
    private final IndyWallet studentWallet;
    private final AppDatabase appDatabase;

    public IndyClient(IndyWallet indyWallet, AppDatabase appDatabase) {
        this.studentWallet = indyWallet;
        this.appDatabase = appDatabase;
    }

    public void acceptCredentialOffer(CredentialOffer credentialOffer) {
        try {
            Log.d("STUDYBITS", "Accepting credential offer");
            Prover studentProver = new Prover(studentWallet, TestConfiguration.STUDENT_SECRET_NAME);

            CredentialRequest credentialRequest = studentProver.createCredentialRequest(credentialOffer).get();
            MessageEnvelope credentialRequestEnvelope = MessageEnvelope.fromAuthcryptable(credentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, studentWallet);

            University university = appDatabase.universityDao().getByDid(credentialOffer.getTheirDid());

            MessageEnvelope<CredentialWithRequest> credentialEnvelope = new AgentClient(university.getEndpoint()).postAndReturnMessage(credentialRequestEnvelope, studentWallet);

            CredentialWithRequest credentialWithRequest = credentialEnvelope.getMessage();

            studentProver.storeCredential(credentialWithRequest).get();

            Credential credential = credentialWithRequest.getCredential();

            university.setCredDefId(credential.getCredDefId());

            appDatabase.universityDao().insertUniversities(university);

            Log.d("STUDYBITS", "Accepted credential offer");


        }
        catch (Exception e) {
            Log.e("STUDYBITS", "Exception when accepting credential offer" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public ProofRequest extractProofRequest(ExchangePosition exchangePosition) throws IndyException, ExecutionException, InterruptedException {
        return studentWallet.authDecrypt(exchangePosition.getAuthcryptedProofRequest(), ProofRequest.class).get();
    }

    public MessageEnvelope fulfillExchangePosition(ExchangePosition exchangePosition) throws IndyException, IOException, ExecutionException, InterruptedException {
        ProofRequest proofRequest = extractProofRequest(exchangePosition);

        Prover prover = new Prover(studentWallet, TestConfiguration.STUDENT_SECRET_NAME);
        Map<String, String> values = new HashMap<>();

        Proof proof = prover.fulfillProofRequest(proofRequest, values).get();
        return MessageEnvelope.fromAuthcryptable(proof, IndyMessageTypes.PROOF, studentWallet);
    }

    @NonNull
    public University connect(String endpoint, AgentClient agentClient, ConnectionRequest connectionRequest) throws InterruptedException, ExecutionException, IndyException, IOException {
        ConnectionResponse connectionResponse = studentWallet.acceptConnectionRequest(connectionRequest).get();

        MessageEnvelope connectionResponseEnvelope = MessageEnvelope.fromAnoncryptable(connectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, studentWallet);
        MessageEnvelope<String> connectionAcknowledgementEnvelope = agentClient.postAndReturnMessage(connectionResponseEnvelope, studentWallet);

        String uniName = connectionAcknowledgementEnvelope.getMessage();

        University university = new University(uniName, endpoint, connectionRequest.getDid());

        Log.d("STUDYBITS", "Inserting university: " + university);
        appDatabase.universityDao().insertUniversities(university);
        return university;
    }
}
