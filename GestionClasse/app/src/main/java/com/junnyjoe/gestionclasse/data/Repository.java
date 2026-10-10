package com.junnyjoe.gestionclasse.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;

/** Point d'accès unique aux données : lectures en LiveData, écritures hors du thread principal. */
public class Repository {
    private final ClasseDao classeDao;
    private final EtudiantDao etudiantDao;
    private final MessageDao messageDao;
    private final Handler main = new Handler(Looper.getMainLooper());

    public Repository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        classeDao = db.classeDao();
        etudiantDao = db.etudiantDao();
        messageDao = db.messageDao();
    }

    // --- Classe ---
    public LiveData<Classe> getClasse() {
        return classeDao.observe();
    }

    public void saveClasse(Classe classe, Callback<Long> cb) {
        run(() -> {
            classeDao.upsert(classe);
            return 0L;
        }, cb);
    }

    // --- Étudiants ---
    public LiveData<List<Etudiant>> searchEtudiants(String query) {
        return etudiantDao.search("%" + query + "%");
    }

    public LiveData<Etudiant> getEtudiant(long id) {
        return etudiantDao.observeById(id);
    }

    public void saveEtudiant(Etudiant e, Callback<Long> cb) {
        run(() -> {
            if (e.id == 0) {
                return etudiantDao.insert(e);
            }
            etudiantDao.update(e);
            return e.id;
        }, cb);
    }

    public void deleteEtudiant(Etudiant e, Callback<Long> cb) {
        run(() -> {
            etudiantDao.delete(e);
            return e.id;
        }, cb);
    }

    // --- Messages ---
    public LiveData<List<Message>> getConversation(long a, long b) {
        return messageDao.conversation(a, b);
    }

    public void insertMessage(Message m, Callback<Long> cb) {
        run(() -> messageDao.insert(m), cb);
    }

    public void deleteMessage(Message m, Callback<Long> cb) {
        run(() -> {
            messageDao.delete(m);
            return m.id;
        }, cb);
    }

    private <T> void run(Callable<T> work, Callback<T> cb) {
        AppDatabase.EXECUTOR.execute(() -> {
            try {
                T result = work.call();
                if (cb != null) main.post(() -> cb.onSuccess(result));
            } catch (Exception e) {
                if (cb != null) main.post(() -> cb.onError(e));
            }
        });
    }
}
