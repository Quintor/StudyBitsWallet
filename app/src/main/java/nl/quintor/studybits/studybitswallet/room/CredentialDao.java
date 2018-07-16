package nl.quintor.studybits.studybitswallet.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import nl.quintor.studybits.studybitswallet.room.entity.Credential;
import nl.quintor.studybits.studybitswallet.room.entity.University;

@Dao
public interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Credential... credentials);

    @Query("SELECT * FROM credential")
    public LiveData<List<Credential>> get();

    @Query("DELETE FROM credential")
    public void deleteAll();

}
