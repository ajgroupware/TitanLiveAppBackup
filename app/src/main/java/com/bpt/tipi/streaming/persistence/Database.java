package com.bpt.tipi.streaming.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bpt.tipi.streaming.model.Label;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_NAME = "titan_live.db";
    private static final int DB_VERSION = 1;

    private DbHelper dbHelper;
    private Context context;

    private SQLiteDatabase db;

    public Database(Context mContext) {
        context = mContext;
        dbHelper = new DbHelper(context, DB_NAME, null, DB_VERSION);
    }

    // Abre una nueva conexión a la base de datos.
    public Database open() throws Exception {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        db.close();
    }

    public void insertLabels(List<Label> labels) {
        deleteLabels();
        for (Label label : labels) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.TableLabel.COLUMN_ID, label.id);
            contentValues.put(DbHelper.TableLabel.COLUMN_DESCRIPTION, label.description);
            db.insert(DbHelper.TableLabel.TABLE_NAME, null, contentValues);
        }
    }

    public List<Label> getLabels() {
        List<Label> labels = new ArrayList<>();
        Cursor cursor = db.query(DbHelper.TableLabel.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Label label = new Label(cursor.getInt(0), cursor.getString(1));
                labels.add(label);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return labels;
    }

    public void deleteLabels() {
        db.delete(DbHelper.TableLabel.TABLE_NAME, null, null);
    }

}
