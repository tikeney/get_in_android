package com.senai.get_in.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.senai.get_in.model.Requisicao;

@Database(entities = {Requisicao.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract RequisicaoDao requisicaoDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "get_in_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}