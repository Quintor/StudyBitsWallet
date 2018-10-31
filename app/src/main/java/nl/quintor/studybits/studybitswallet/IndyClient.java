package nl.quintor.studybits.studybitswallet;

import android.os.Message;
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
import nl.quintor.studybits.indy.wrapper.dto.AuthcryptableString;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
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
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.indy.wrapper.util.AsyncUtil;
import nl.quintor.studybits.studybitswallet.exchangeposition.AuthcryptableExchangePositions;
import nl.quintor.studybits.studybitswallet.exchangeposition.ExchangePosition;
import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class IndyClient {
    private final IndyWallet studentWallet;
    private final MessageEnvelopeCodec studentCodec;
    private final AppDatabase appDatabase;

    public IndyClient(IndyWallet indyWallet, AppDatabase appDatabase) {
        this.studentWallet = indyWallet;
        this.studentCodec = new MessageEnvelopeCodec(indyWallet);
        this.appDatabase = appDatabase;
    }

    public void acceptCredentialOffer(CredentialOffer credentialOffer) {
        try {
            Log.d("STUDYBITS", "Accepting credential offer");
            Prover studentProver = new Prover(studentWallet, TestConfiguration.STUDENT_SECRET_NAME);

            CredentialRequest credentialRequest = studentProver.createCredentialRequest(credentialOffer).get();
            MessageEnvelope credentialRequestEnvelope = studentCodec.encryptMessage(credentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST).get();

            University university = appDatabase.universityDao().getByDid(credentialOffer.getTheirDid());

            MessageEnvelope<CredentialWithRequest> credentialEnvelope = new AgentClient(university.getEndpoint()).postAndReturnMessage(credentialRequestEnvelope, IndyMessageTypes.CREDENTIAL);

            CredentialWithRequest credentialWithRequest = studentCodec.decryptMessage(credentialEnvelope).get();

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

    public MessageEnvelope fulfillExchangePosition(ExchangePosition exchangePosition) throws IndyException, IOException, ExecutionException, InterruptedException {
        ProofRequest proofRequest = exchangePosition.getProofRequest();

        Prover prover = new Prover(studentWallet, TestConfiguration.STUDENT_SECRET_NAME);
        Map<String, String> values = new HashMap<>();

        Proof proof = prover.fulfillProofRequest(proofRequest, values).get();

        return studentCodec.encryptMessage(proof, IndyMessageTypes.PROOF).get();
    }

    @NonNull
    public University connect(String endpoint, AgentClient agentClient, ConnectionRequest connectionRequest) throws InterruptedException, ExecutionException, IndyException, IOException {

        ConnectionResponse connectionResponse = studentWallet.acceptConnectionRequest(connectionRequest).get();

        MessageEnvelope<ConnectionResponse> connectionResponseEnvelope = studentCodec.encryptMessage(connectionResponse, IndyMessageTypes.CONNECTION_RESPONSE).get();

        MessageEnvelope<AuthcryptableString> connectionAcknowledgementEnvelope = agentClient.postAndReturnMessage(connectionResponseEnvelope, IndyMessageTypes.CONNECTION_ACKNOWLEDGEMENT);

        AuthcryptableString uniNameC = studentCodec.decryptMessage(connectionAcknowledgementEnvelope).get();
        String uniName = uniNameC.getPayload();
        University university = new University(uniName, endpoint, connectionRequest.getDid());

        Log.d("STUDYBITS", "Inserting university: " + university);
        appDatabase.universityDao().insertUniversities(university);
        return university;
    }
}
