package nl.quintor.studybits.studybitswallet.room;

import android.app.Activity;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import nl.quintor.studybits.studybitswallet.room.entity.University;

@Database(entities = {University.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UniversityDao universityDao();

    private static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, "studybits-db").fallbackToDestructiveMigration().build();
        }

        return appDatabase;
    }

    public static class AsyncDatabaseTask extends AsyncTask<Void, Void, Void> {
        private final Runnable function;
        private final AtomicInteger countDownLatch;
        private final Runnable finished;
        public AsyncDatabaseTask(Runnable function, AtomicInteger countDownLatch, Runnable finished) {
            this.function = function;
            this.countDownLatch = countDownLatch;
            this.finished = finished;
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            function.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (countDownLatch != null) {
                int count = countDownLatch.decrementAndGet();
                if (count == 0) {
                    finished.run();
                }
            }
        }
    }
}
