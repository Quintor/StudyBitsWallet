package nl.quintor.studybits.studybitswallet;

import android.arch.lifecycle.LifecycleOwner;
import android.os.AsyncTask;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
import nl.quintor.studybits.studybitswallet.credential.CredentialOrOffer;
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

    public void acceptCredentialOffer(LifecycleOwner lifecycleOwner, CredentialOrOffer credentialOrOffer, CompletableFuture<Void> returnValue) {
        try {
            Log.d("STUDYBITS", "Accepting credential offer");
            Prover studentProver = new Prover(studentWallet, TestConfiguration.STUDENT_SECRET_NAME);

            CredentialRequest credentialRequest = studentProver.createCredentialRequest(credentialOrOffer.getTheirDid(), credentialOrOffer.getCredentialOffer()).get();
            MessageEnvelope credentialRequestEnvelope = studentCodec.encryptMessage(credentialRequest, IndyMessageTypes.CREDENTIAL_REQUEST, credentialOrOffer.getTheirDid()).get();

            University university = credentialOrOffer.getUniversity();

            if (returnValue.isDone()) {
                return;
            }
            try {
                MessageEnvelope<CredentialWithRequest> credentialEnvelope = new AgentClient(university, studentCodec).postAndReturnMessage(credentialRequestEnvelope, IndyMessageTypes.CREDENTIAL);

                CredentialWithRequest credentialWithRequest = studentCodec.decryptMessage(credentialEnvelope).get();

                studentProver.storeCredential(credentialWithRequest).get();

                Credential credential = credentialWithRequest.getCredential();

                university.setCredDefId(credential.getCredDefId());

                new AppDatabase.AsyncDatabaseTask(() -> appDatabase.universityDao().insertUniversities(university),
                        new AtomicInteger(1), () -> {
                    Log.d("STUDYBITS", "Accepted credential offer");
                    returnValue.complete(null);
                }).execute();


            }
            catch (Exception e) {
                Log.e("STUDYBITS", "Error while accepting credential offer " + e.getMessage());
                returnValue.completeExceptionally(e);
            }
        }
        catch (Exception e) {
            Log.e("STUDYBITS", "Exception when accepting credential offer" + e.getMessage());
            e.printStackTrace();
            returnValue.completeExceptionally(e);
        }
    }

    public MessageEnvelope fulfillExchangePosition(ExchangePosition exchangePosition) throws IndyException, IOException, ExecutionException, InterruptedException {
        ProofRequest proofRequest = exchangePosition.getProofRequest();

        Prover prover = new Prover(studentWallet, TestConfiguration.STUDENT_SECRET_NAME);
        Map<String, String> values = new HashMap<>();

        Proof proof = prover.fulfillProofRequest(proofRequest, values).get();

        return studentCodec.encryptMessage(proof, IndyMessageTypes.PROOF, exchangePosition.getTheirDid()).get();
    }

    @NonNull
    public University connect(String endpoint, String uniName, String username, String uniVerinymDid) throws InterruptedException, ExecutionException, IndyException, IOException {
        ConnectionRequest connectionRequest = studentWallet.createConnectionRequest().get();

        MessageEnvelope<ConnectionRequest> connectionResponseEnvelope = studentCodec.encryptMessage(connectionRequest, IndyMessageTypes.CONNECTION_REQUEST, uniVerinymDid).get();

        MessageEnvelope<ConnectionResponse> connectionResponseMessageEnvelope = AgentClient.login(endpoint, username, connectionResponseEnvelope);

        ConnectionResponse connectionResponse = studentCodec.decryptMessage(connectionResponseMessageEnvelope).get();

        studentWallet.acceptConnectionResponse(connectionResponse, connectionRequest.getDid());

        University university = new University(uniName, endpoint, connectionResponse.getDid());

        Log.d("STUDYBITS", "Inserting university: " + university);
        new AppDatabase.AsyncDatabaseTask(() -> appDatabase.universityDao().insertUniversities(university), null, null).execute();
        return university;
    }
}
