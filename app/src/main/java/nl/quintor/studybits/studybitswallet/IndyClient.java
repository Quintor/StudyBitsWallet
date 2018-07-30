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
import nl.quintor.studybits.indy.wrapper.dto.Credential;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.dto.CredentialWithRequest;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
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
            Prover studentProver = new Prover(studentWallet, WalletActivity.STUDENT_SECRET_NAME);

            AuthcryptedMessage authcryptedCredentialRequest = studentProver.createCredentialRequest(credentialOffer)
                    .thenCompose(AsyncUtil.wrapException(studentProver::authEncrypt)).get();
            MessageEnvelope credentialRequestEnvelope = envelopeFromAuthcrypted(authcryptedCredentialRequest, MessageEnvelope.MessageType.CREDENTIAL_REQUEST);

            University university = appDatabase.universityDao().getByDid(credentialOffer.getTheirDid());

            MessageEnvelope credentialEnvelope = new AgentClient(university.getEndpoint()).postAndReturnMessage(credentialRequestEnvelope);

            AuthcryptedMessage authcryptedCredential = new AuthcryptedMessage(Base64.decode(credentialEnvelope.getMessage().asText(), Base64.NO_WRAP), credentialEnvelope.getId());

            CredentialWithRequest credentialWithRequest = studentProver.authDecrypt(authcryptedCredential, CredentialWithRequest.class).get();

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

    @NonNull
    public MessageEnvelope envelopeFromAuthcrypted(AuthcryptedMessage authcryptedCredentialRequest, MessageEnvelope.MessageType type) {
        return new MessageEnvelope(authcryptedCredentialRequest.getDid(), type,
                new TextNode(new String(Base64.encode(authcryptedCredentialRequest.getMessage(), Base64.NO_WRAP), Charset.forName("utf8"))));
    }

    public static AuthcryptedMessage authcryptedMessageFromEnvelope(MessageEnvelope envelope) {
        return new AuthcryptedMessage(Base64.decode(envelope.getMessage().asText(), Base64.NO_WRAP), envelope.getId());
    }

    public void fulfillExchangePosition(ExchangePosition exchangePosition) throws IndyException, IOException, ExecutionException, InterruptedException {
        ProofRequest proofRequest = studentWallet.authDecrypt(exchangePosition.getAuthcryptedProofRequest(), ProofRequest.class).get();

        Prover prover = new Prover(studentWallet, WalletActivity.STUDENT_SECRET_NAME);
        Map<String, String> values = new HashMap<>();

        AuthcryptedMessage authcryptedProof = prover.fulfillProofRequest(proofRequest, values)
                .thenCompose(AsyncUtil.wrapException(prover::authEncrypt)).get();

        MessageEnvelope proofEnvelope = envelopeFromAuthcrypted(authcryptedProof, MessageEnvelope.MessageType.PROOF);

        MessageEnvelope messageEnvelope = new AgentClient(exchangePosition.getUniversity().getEndpoint()).postAndReturnMessage(proofEnvelope);
    }

    @NonNull
    public University connect(String endpoint, AgentClient agentClient, ConnectionRequest connectionRequest) throws InterruptedException, ExecutionException, IndyException, IOException {
        AnoncryptedMessage anoncryptedConnectionResponse = studentWallet.acceptConnectionRequest(connectionRequest)
                .thenCompose(AsyncUtil.wrapException(studentWallet::anonEncrypt)).get();

        MessageEnvelope connectionResponseEnvelope = fromAnoncryptedMessage(connectionRequest.getRequestNonce(), MessageEnvelope.MessageType.CONNECTION_RESPONSE, anoncryptedConnectionResponse.getMessage());
        MessageEnvelope connectionAcknowledgementEnvelope = agentClient.postAndReturnMessage(connectionResponseEnvelope);

        String uniName = connectionAcknowledgementEnvelope.getMessage().asText();

        University university = new University(uniName, endpoint, connectionRequest.getDid());

        Log.d("STUDYBITS", "Inserting university: " + university);
        appDatabase.universityDao().insertUniversities(university);
        return university;
    }

    @NonNull
    private MessageEnvelope fromAnoncryptedMessage(String requestNonce, MessageEnvelope.MessageType connectionResponse, byte[] message) {
        return new MessageEnvelope(requestNonce, connectionResponse,
                new TextNode(new String(Base64.encode(message, Base64.NO_WRAP), Charset.forName("utf8"))));
    }
}
