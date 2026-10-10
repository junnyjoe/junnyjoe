package com.junnyjoe.gestionclasse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.junnyjoe.gestionclasse.data.Callback;
import com.junnyjoe.gestionclasse.data.Etudiant;
import com.junnyjoe.gestionclasse.data.Message;
import com.junnyjoe.gestionclasse.data.Repository;

import java.util.List;

/** État d'une conversation entre deux profils étudiants. */
public class ConversationViewModel extends AndroidViewModel {
    private final Repository repository;
    private long meId;
    private long peerId;
    private LiveData<List<Message>> messages;
    private LiveData<Etudiant> me;
    private LiveData<Etudiant> peer;

    /** Dernier message enregistré (événement ponctuel, à consommer avec {@link #clearSaved()}). */
    private final MutableLiveData<Message> saved = new MutableLiveData<>();
    /** Dernière erreur d'enregistrement (événement ponctuel). */
    private final MutableLiveData<Boolean> error = new MutableLiveData<>();

    public ConversationViewModel(@NonNull Application app) {
        super(app);
        repository = new Repository(app);
    }

    public void init(long meId, long peerId) {
        if (messages != null) return;
        this.meId = meId;
        this.peerId = peerId;
        messages = repository.getConversation(meId, peerId);
        me = repository.getEtudiant(meId);
        peer = repository.getEtudiant(peerId);
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<Etudiant> getMe() {
        return me;
    }

    public LiveData<Etudiant> getPeer() {
        return peer;
    }

    public LiveData<Message> getSaved() {
        return saved;
    }

    public LiveData<Boolean> getError() {
        return error;
    }

    public void clearSaved() {
        saved.setValue(null);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void send(String content, @Nullable String uri, @Nullable String type,
                     @Nullable String mime, @Nullable String name) {
        Message m = new Message(meId, peerId, content, System.currentTimeMillis(), uri, type, mime, name);
        repository.insertMessage(m, new Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                m.id = id;
                saved.setValue(m);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(true);
            }
        });
    }

    public void deleteMessage(Message m) {
        repository.deleteMessage(m, new Callback<Long>() {
            @Override
            public void onSuccess(Long result) { }

            @Override
            public void onError(Exception e) {
                error.setValue(true);
            }
        });
    }
}
