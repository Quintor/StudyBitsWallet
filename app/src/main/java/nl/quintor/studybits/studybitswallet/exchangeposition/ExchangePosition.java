package nl.quintor.studybits.studybitswallet.exchangeposition;

import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class ExchangePosition {
    private String name;
    private ProofRequest proofRequest;
    private University university;

    public ExchangePosition() {
        
    }

    public ExchangePosition(String name, ProofRequest proofRequest, University university) {
        this.name = name;
        this.proofRequest = proofRequest;
        this.university = university;
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

    public void setProofRequest(ProofRequest proofRequest) {
        this.proofRequest = proofRequest;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }
}
