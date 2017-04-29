package com.programacionymas.conciviles.io.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.programacionymas.conciviles.io.sqlite.MyDbContract.AreaEntry;
import com.programacionymas.conciviles.io.sqlite.MyDbContract.InformEntry;
import com.programacionymas.conciviles.model.Inform;

import java.util.ArrayList;

public class MyDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "pym.db";

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + AreaEntry.TABLE_NAME + " (" +
                AreaEntry._ID + " INTEGER PRIMARY KEY," +
                AreaEntry.COLUMN_NAME + " TEXT)");

        db.execSQL("CREATE TABLE " + InformEntry.TABLE_NAME + " (" +
                InformEntry._ID + " INTEGER PRIMARY KEY," +
                InformEntry.COLUMN_ID + " INTEGER UNIQUE," +
                InformEntry.COLUMN_USER_ID + " INTEGER," +
                InformEntry.COLUMN_FROM_DATE + " TEXT," +
                InformEntry.COLUMN_TO_DATE + " TEXT," +
                InformEntry.COLUMN_CREATED_AT + " TEXT," +
                InformEntry.COLUMN_USER_NAME + " TEXT," +
                InformEntry.COLUMN_IS_EDITABLE + " INTEGER)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AreaEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + InformEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean isAnyInfoAvailable(String tableName){
        SQLiteDatabase db = getWritableDatabase();

        boolean result = false;

        Cursor c = db.rawQuery(
            "SELECT 1 FROM " + tableName, null
        );

        if (c != null) {
            if(c.moveToFirst()) {
                result = true;
            }

            c.close();
        }

        return result;
    }

    public void updateInformsTable(ArrayList<Inform> informs) {
        SQLiteDatabase db = getWritableDatabase();

        // discard all the informs
        db.delete(InformEntry.TABLE_NAME, null, null);

        for (Inform inform : informs) {
            db.insert(InformEntry.TABLE_NAME, null, inform.getContentValues());
        }
        db.close();
    }

    public ArrayList<Inform> getInforms() {
        ArrayList<Inform> informs = new ArrayList<>();

        // select all
        String selectQuery = "SELECT  * FROM " + InformEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Inform inform = new Inform();

                    int columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_ID);
                    if (columnIndex > -1) {
                        int id;
                        try {
                            id = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            id = 0;
                        }
                        inform.setId(id);
                    }

                    columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_USER_ID);
                    if (columnIndex > -1) {
                        int userId;
                        try {
                            userId = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            userId = 0;
                        }
                        inform.setUserId(userId);
                    }

                    columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_FROM_DATE);
                    if (columnIndex > -1) {
                        String fromDate = cursor.getString(columnIndex);
                        inform.setFromDate(fromDate);
                    }

                    columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_TO_DATE);
                    if (columnIndex > -1) {
                        String toDate = cursor.getString(columnIndex);
                        inform.setToDate(toDate);
                    }

                    columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_USER_NAME);
                    if (columnIndex > -1) {
                        String userName = cursor.getString(columnIndex);
                        inform.setUserName(userName);
                    }

                    columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_IS_EDITABLE);
                    if (columnIndex > -1) {
                        int isEditable;
                        try {
                            isEditable = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            isEditable = 0;
                        }
                        inform.setEditable(isEditable==1);
                    }

                    // adding inform to list
                    informs.add(inform);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // return list
        return informs;
    }
}
