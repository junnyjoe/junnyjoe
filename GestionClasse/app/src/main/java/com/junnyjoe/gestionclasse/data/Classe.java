package com.junnyjoe.gestionclasse.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/** Classe unique de l'application : l'identifiant est fixé à 1. */
@Entity(tableName = "classe")
public class Classe {
    public static final int UNIQUE_ID = 1;

    @PrimaryKey
    public int id = UNIQUE_ID;
    @NonNull
    public String intitule = "";
    @NonNull
    public String description = "";
    @NonNull
    public String anneeUniversitaire = "";

    public Classe() { }

    @Ignore
    public Classe(@NonNull String intitule, @NonNull String description, @NonNull String anneeUniversitaire) {
        this.intitule = intitule;
        this.description = description;
        this.anneeUniversitaire = anneeUniversitaire;
    }
}
