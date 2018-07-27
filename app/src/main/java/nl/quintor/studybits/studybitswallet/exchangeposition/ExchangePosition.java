package nl.quintor.studybits.studybitswallet.exchangeposition;

import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class ExchangePosition {
    private String name;
    private AuthcryptedMessage authcryptedProofRequest;
    private boolean fulfilled;
    private University university;

    public ExchangePosition() {

    }

    public ExchangePosition(String name, AuthcryptedMessage authcryptedProofRequest, University university, boolean fulfilled) {
        this.name = name;
        this.authcryptedProofRequest = authcryptedProofRequest;
        this.university = university;
        this.fulfilled = fulfilled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthcryptedMessage getAuthcryptedProofRequest() {
        return authcryptedProofRequest;
    }

    public void setAuthcryptedProofRequest(AuthcryptedMessage authcryptedProofRequest) {
        this.authcryptedProofRequest = authcryptedProofRequest;
    }

    public University getUniversity() {
        return university;
    }

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
