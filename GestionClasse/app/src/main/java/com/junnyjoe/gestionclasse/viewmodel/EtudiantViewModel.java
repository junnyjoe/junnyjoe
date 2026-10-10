package com.junnyjoe.gestionclasse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.junnyjoe.gestionclasse.data.Callback;
import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.data.Repository;

import java.util.List;

public class EtudiantViewModel extends AndroidViewModel {
    private final Repository repository;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<Etudiant>> etudiants;

    public EtudiantViewModel(@NonNull Application app) {
        super(app);
        repository = new Repository(app);
        etudiants = Transformations.switchMap(query, q -> repository.searchEtudiants(q.trim()));
    }

    public LiveData<List<Etudiant>> getEtudiants() {
        return etudiants;
    }

    public void setQuery(String q) {
        query.setValue(q == null ? "" : q);
    }

    public String getQuery() {
        String q = query.getValue();
        return q == null ? "" : q;
    }

    public LiveData<Etudiant> getEtudiant(long id) {
        return repository.getEtudiant(id);
    }

    public void save(Etudiant e, Callback<Long> cb) {
        repository.saveEtudiant(e, cb);
    }

    public void delete(Etudiant e, Callback<Long> cb) {
        repository.deleteEtudiant(e, cb);
    }
}
