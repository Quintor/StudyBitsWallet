package nl.quintor.studybits.studybitswallet.credential;

import nl.quintor.studybits.indy.wrapper.dto.CredentialInfo;
import nl.quintor.studybits.indy.wrapper.dto.CredentialOffer;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class CredentialOrOffer {
    private University university;
    private String universityName;
    private String value;
    private CredentialOffer credentialOffer;
    private CredentialInfo credential;

    private CredentialOrOffer(University university, String value, CredentialOffer credentialOffer, CredentialInfo credential) {
        this.university = university;
        this.universityName = university.getName();
        this.value = value;
        this.credentialOffer = credentialOffer;
        this.credential = credential;
    }

    public static CredentialOrOffer fromCredentialOffer(University university, CredentialOffer credentialOffer) {
        return new CredentialOrOffer(university, credentialOffer.getSchemaId(), credentialOffer, null);
    }

    public static CredentialOrOffer fromCredential(University university, CredentialInfo credential) {
        return new CredentialOrOffer(university, credential.getAttrs().toString(), null, credential);
    }

    public String getUniversityName() {
        return universityName;
    }

    public String getValue() {
        return value;
    }

    public String getTheirDid() { return university.getTheirDid(); }

    public University getUniversity() { return university; }

    public CredentialOffer getCredentialOffer() {
        return credentialOffer;
    }

    public CredentialInfo getCredential() {
        return credential;
    }
}
