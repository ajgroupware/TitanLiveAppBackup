package com.bpt.tipi.streaming.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LABEL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TableLabel.TABLE_NAME);
    }

    /**
     * Estructura de la creación de la tabla {@link TableLabel}.
     */
    private static final String CREATE_TABLE_LABEL = "CREATE TABLE IF NOT EXISTS " +
            TableLabel.TABLE_NAME + "(" +
            TableLabel.COLUMN_ID + " INTEGER," +
            TableLabel.COLUMN_DESCRIPTION + " TEXT);";

    static abstract class TableLabel {
        static final String TABLE_NAME = "tbl_label";
        static final String COLUMN_ID = "id";
        static final String COLUMN_DESCRIPTION = "description";
    }
}
