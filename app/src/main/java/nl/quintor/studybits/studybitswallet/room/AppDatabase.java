package nl.quintor.studybits.studybitswallet.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import nl.quintor.studybits.studybitswallet.room.entity.University;

@Database(entities = {University.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UniversityDao universityDao();

    static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, "studybits-db").allowMainThreadQueries().build();
        }

        return appDatabase;
    }
}
