package nl.quintor.studybits.studybitswallet.room.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Credential {
    @PrimaryKey
    @NonNull
    private String credDefId;

    private String issuerDid;

    private String values;

    public Credential(@NonNull String credDefId, String issuerDid, String values) {
        this.credDefId = credDefId;
        this.issuerDid = issuerDid;
        this.values = values;
    }

    @NonNull
    public String getCredDefId() {
        return credDefId;
    }

    public void setCredDefId(@NonNull String credDefId) {
        this.credDefId = credDefId;
    }

    public String getIssuerDid() {
        return issuerDid;
    }

    public void setIssuerDid(String issuerDid) {
        this.issuerDid = issuerDid;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
