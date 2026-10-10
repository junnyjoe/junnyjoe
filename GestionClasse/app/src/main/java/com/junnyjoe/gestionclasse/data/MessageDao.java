package com.junnyjoe.gestionclasse.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    long insert(Message message);

    @Delete
    void delete(Message message);

    @Query("SELECT * FROM message WHERE (expediteurId = :a AND destinataireId = :b) "
            + "OR (expediteurId = :b AND destinataireId = :a) ORDER BY dateTime ASC, id ASC")
    LiveData<List<Message>> conversation(long a, long b);
}
