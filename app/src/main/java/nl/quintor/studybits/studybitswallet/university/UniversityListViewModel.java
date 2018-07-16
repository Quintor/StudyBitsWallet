package nl.quintor.studybits.studybitswallet.university;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import nl.quintor.studybits.studybitswallet.room.AppDatabase;
import nl.quintor.studybits.studybitswallet.room.entity.University;

public class UniversityListViewModel extends AndroidViewModel {
    private final LiveData<List<University>> universityList;

    public UniversityListViewModel(@NonNull Application application) {
        super(application);
        universityList = AppDatabase.getInstance(this.getApplication()).universityDao().get();
    }

    public LiveData<List<University>> getUniversityList() {
        return universityList;
    }
}
