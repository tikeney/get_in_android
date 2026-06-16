package com.senai.get_in.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.senai.get_in.model.Requisicao;

import java.util.List;

@Dao
public interface RequisicaoDao {
    @Query("SELECT * FROM requisicoes WHERE status != 'pendente' ORDER BY id DESC")
    List<Requisicao> getHistorico();

    @Query("SELECT * FROM requisicoes WHERE status != 'pendente' AND idSetor = :idSetor ORDER BY id DESC")
    List<Requisicao> getHistoricoPorSetor(int idSetor);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Requisicao> requisicoes);

    @Query("DELETE FROM requisicoes WHERE status != 'pendente'")
    void clearHistorico();
}