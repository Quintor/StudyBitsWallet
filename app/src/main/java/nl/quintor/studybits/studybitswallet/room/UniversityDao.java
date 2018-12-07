package nl.quintor.studybits.studybitswallet.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import nl.quintor.studybits.studybitswallet.room.entity.University;

@Dao
public interface UniversityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertUniversities(University... universities);

    @Query("SELECT * FROM university")
    public LiveData<List<University>> get();

    @Query("SELECT * FROM university")
    public List<University> getStatic();

    @Query("SELECT * FROM university WHERE theirDid = :did")
    public LiveData<University> getByDid(String did);

    @Query("DELETE FROM university")
    public void delete();
}
