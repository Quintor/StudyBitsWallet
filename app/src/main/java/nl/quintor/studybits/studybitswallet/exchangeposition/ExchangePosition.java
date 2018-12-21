package nl.quintor.studybits.studybitswallet.exchangeposition;

import org.hyperledger.indy.sdk.anoncreds.ProofRejectedException;

import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class ExchangePosition {
    private String name;
    private ProofRequest proofRequest;
    private boolean fulfilled;
    private University university;

    public ExchangePosition() {

    }

    public ExchangePosition(String name, ProofRequest proofRequest, University university, boolean fulfilled) {
        this.name = name;
        this.proofRequest = proofRequest;
        this.university = university;
        this.fulfilled = fulfilled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProofRequest getProofRequest() {
        return proofRequest;
    }

    public void setAuthcryptedProofRequest(ProofRequest proofRequest) {
        this.proofRequest = proofRequest;
    }

    public University getUniversity() {
        return university;
    }

    public String getTheirDid() { return university.getTheirDid(); }

    public void setUniversity(University university) {
        this.university = university;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }
}
