package com.junnyjoe.gestionclasse.data;

/** Résultat d'une opération asynchrone, toujours livré sur le thread principal. */
public interface Callback<T> {
    void onSuccess(T result);

    void onError(Exception e);
}
