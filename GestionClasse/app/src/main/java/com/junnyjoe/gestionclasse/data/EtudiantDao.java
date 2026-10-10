package com.junnyjoe.gestionclasse.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EtudiantDao {
    @Insert
    long insert(Etudiant etudiant);

    @Update
    void update(Etudiant etudiant);

    @Delete
    void delete(Etudiant etudiant);

    @Query("SELECT * FROM etudiant WHERE id = :id")
    LiveData<Etudiant> observeById(long id);

    @Query("SELECT * FROM etudiant WHERE nom LIKE :q OR prenom LIKE :q OR (prenom || ' ' || nom) LIKE :q "
            + "ORDER BY nom COLLATE NOCASE, prenom COLLATE NOCASE")
    LiveData<List<Etudiant>> search(String q);
}
