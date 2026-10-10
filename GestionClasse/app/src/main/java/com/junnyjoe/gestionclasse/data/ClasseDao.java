package com.junnyjoe.gestionclasse.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ClasseDao {
    @Query("SELECT * FROM classe WHERE id = 1 LIMIT 1")
    LiveData<Classe> observe();

    /** Création ou modification : la ligne unique est remplacée. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Classe classe);
}
