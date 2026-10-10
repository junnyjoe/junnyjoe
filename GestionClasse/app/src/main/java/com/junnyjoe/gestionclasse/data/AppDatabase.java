package com.junnyjoe.gestionclasse.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Classe.class, Etudiant.class, Message.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /** Les écritures passent par ce pool pour ne jamais bloquer le thread principal. */
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    private static volatile AppDatabase instance;

    public abstract ClasseDao classeDao();

    public abstract EtudiantDao etudiantDao();

    public abstract MessageDao messageDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "gestion_classe.db").build();
                }
            }
        }
        return instance;
    }
}
