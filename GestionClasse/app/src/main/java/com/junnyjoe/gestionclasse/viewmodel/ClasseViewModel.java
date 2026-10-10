package com.junnyjoe.gestionclasse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.junnyjoe.gestionclasse.data.Callback;
import com.junnyjoe.gestionclasse.data.Classe;
import com.junnyjoe.gestionclasse.data.Repository;

public class ClasseViewModel extends AndroidViewModel {
    private final Repository repository;
    private final LiveData<Classe> classe;

    public ClasseViewModel(@NonNull Application app) {
        super(app);
        repository = new Repository(app);
        classe = repository.getClasse();
    }

    public LiveData<Classe> getClasse() {
        return classe;
    }

    public void save(Classe c, Callback<Long> cb) {
        repository.saveClasse(c, cb);
    }
}
