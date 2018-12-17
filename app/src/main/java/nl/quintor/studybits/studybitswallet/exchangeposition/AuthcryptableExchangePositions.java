package nl.quintor.studybits.studybitswallet.exchangeposition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.dto.Serializable;

import java.util.List;

public class AuthcryptableExchangePositions implements Serializable {

    public AuthcryptableExchangePositions() {

    }

    public AuthcryptableExchangePositions(List<ExchangePosition> exchangePositions, String theirDid) {
        this.exchangePositions = exchangePositions;
        this.theirDid = theirDid;
    }

    private List<ExchangePosition> exchangePositions;

    private String theirDid;

    public List<ExchangePosition> getExchangePositions() {
        return exchangePositions;
    }

    public void setExchangePositions(List<ExchangePosition> exchangePositions) {
        this.exchangePositions = exchangePositions;
    }

    public static class ExchangePositionDto {
        private String name;
        private ProofRequest proofRequest;
        private boolean fulfilled;

        public ExchangePositionDto(String name, ProofRequest proofRequest, boolean fulfilled) {
            this.name = name;
            this.proofRequest = proofRequest;
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

        public void setProofRequest(ProofRequest proofRequest) {
            this.proofRequest = proofRequest;
        }

        public boolean isFulfilled() {
            return fulfilled;
        }

        public void setFulfilled(boolean fulfilled) {
            this.fulfilled = fulfilled;
        }
    }

}
