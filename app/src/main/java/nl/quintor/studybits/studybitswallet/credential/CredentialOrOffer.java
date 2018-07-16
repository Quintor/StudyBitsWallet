package nl.quintor.studybits.studybitswallet.credential;

import nl.quintor.studybits.indy.wrapper.dto.Credential;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;

public class CredentialOrOffer {
    private String universityName;
    private String value;
    private CredentialOffer credentialOffer;
    private Credential credential;

    private CredentialOrOffer(String universityName, String value, CredentialOffer credentialOffer, Credential credential) {
        this.universityName = universityName;
        this.value = value;
        this.credentialOffer = credentialOffer;
        this.credential = credential;
    }

    public static CredentialOrOffer fromCredentialOffer(String universityName, CredentialOffer credentialOffer) {
        return new CredentialOrOffer(universityName, credentialOffer.getSchemaId(), credentialOffer, null);
    }

    public static CredentialOrOffer fromCredential(String universityName, Credential credential) {
        return new CredentialOrOffer(universityName, credential.getValues().toString(), null, credential);
    }

    public String getUniversityName() {
        return universityName;
    }

    public String getValue() {
        return value;
    }

    public CredentialOffer getCredentialOffer() {
        return credentialOffer;
    }

    public Credential getCredential() {
        return credential;
    }
}
