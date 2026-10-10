package com.junnyjoe.gestionclasse.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "etudiant")
public class Etudiant {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String nom = "";
    @NonNull
    public String prenom = "";
    @NonNull
    public String email = "";
    @NonNull
    public String telephone = "";

    public Etudiant() { }

    @Ignore
    public Etudiant(long id, @NonNull String nom, @NonNull String prenom,
                    @NonNull String email, @NonNull String telephone) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
    }

    public String getNomComplet() {
        return (prenom + " " + nom).trim();
    }

    public String getInitiales() {
        String p = prenom.isEmpty() ? "" : prenom.substring(0, 1);
        String n = nom.isEmpty() ? "" : nom.substring(0, 1);
        return (p + n).toUpperCase();
    }
}
