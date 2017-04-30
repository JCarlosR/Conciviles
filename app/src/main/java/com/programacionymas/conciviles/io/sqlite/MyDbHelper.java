package com.programacionymas.conciviles.io.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.programacionymas.conciviles.io.sqlite.MyDbContract.AreaEntry;
import com.programacionymas.conciviles.io.sqlite.MyDbContract.InformEntry;
import com.programacionymas.conciviles.io.sqlite.MyDbContract.ReportEntry;

import com.programacionymas.conciviles.model.Inform;
import com.programacionymas.conciviles.model.Report;

import java.util.ArrayList;

public class MyDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
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

        db.execSQL("CREATE TABLE " + ReportEntry.TABLE_NAME + " (" +
                ReportEntry._ID + " INTEGER PRIMARY KEY," +

                ReportEntry.COLUMN_ID + " INTEGER UNIQUE," +
                ReportEntry.COLUMN_INFORM_ID + " INTEGER NOT NULL," +

                ReportEntry.COLUMN_USER_ID + " INTEGER," +
                ReportEntry.COLUMN_ASPECT+ " TEXT," +
                ReportEntry.COLUMN_POTENTIAL + " TEXT," +
                ReportEntry.COLUMN_STATE + " TEXT," +
                ReportEntry.COLUMN_IMG + " TEXT," +
                ReportEntry.COLUMN_IMG_ACTION + " TEXT," +
                ReportEntry.COLUMN_PLANNED_DATE + " TEXT," +
                ReportEntry.COLUMN_DEADLINE + " TEXT," +
                ReportEntry.COLUMN_INSPECTIONS + " INTEGER," +
                ReportEntry.COLUMN_DESCRIP + " TEXT," +
                ReportEntry.COLUMN_ACTIONS + " TEXT," +
                ReportEntry.COLUMN_OBS + " TEXT," +

                ReportEntry.COLUMN_CREATED_AT + " TEXT," +
                ReportEntry.COLUMN_WORK_FRONT_NAME + " TEXT," +
                ReportEntry.COLUMN_AREA_NAME + " TEXT," +
                ReportEntry.COLUMN_RESPONSIBLE + " TEXT," +
                ReportEntry.COLUMN_CRIT_RISKS + " TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AreaEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + InformEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReportEntry.TABLE_NAME);
        onCreate(db);
    }


    // Informs

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

                    columnIndex = cursor.getColumnIndex(InformEntry.COLUMN_CREATED_AT);
                    if (columnIndex > -1) {
                        String createdAt = cursor.getString(columnIndex);
                        inform.setCreatedAt(createdAt);
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


    // Reports

    public void updateReportsForInform(ArrayList<Report> reports, final int inform_id) {
        SQLiteDatabase db = getWritableDatabase();

        // discard reports associated with the inform_id
        db.delete(ReportEntry.TABLE_NAME, ReportEntry.COLUMN_INFORM_ID + "=" + inform_id, null);

        for (Report report : reports) {
            db.insert(ReportEntry.TABLE_NAME, null, report.getContentValues(inform_id));
        }
        db.close();
    }

    public ArrayList<Report> getReports(final int inform_id) {
        ArrayList<Report> reports = new ArrayList<>();

        // select all
        String selectQuery = "SELECT  * FROM " + ReportEntry.TABLE_NAME + " WHERE inform_id = " + inform_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Report report = new Report();

                    int columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_ID);
                    if (columnIndex > -1) {
                        int id;
                        try {
                            id = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            id = 0;
                        }
                        report.setId(id);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_INFORM_ID);
                    if (columnIndex > -1) {
                        int informId;
                        try {
                            informId = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            informId = 0;
                        }
                        report.setUserId(informId);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_USER_ID);
                    if (columnIndex > -1) {
                        int userId;
                        try {
                            userId = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            userId = 0;
                        }
                        report.setUserId(userId);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_ASPECT);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setAspect(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_POTENTIAL);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setPotential(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_STATE);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setState(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_IMG);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setImage(value);
                    }
                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_IMG_ACTION);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setImageAction(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_PLANNED_DATE);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setPlannedDate(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_DEADLINE);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setDeadline(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_INSPECTIONS);
                    if (columnIndex > -1) {
                        int inspections;
                        try {
                            inspections = Integer.parseInt(cursor.getString(columnIndex));
                        } catch (NumberFormatException nfe) {
                            inspections = 0;
                        }
                        report.setInspections(inspections);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_DESCRIP);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setDescription(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_ACTIONS);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setActions(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_OBS);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setObservations(value);
                    }


                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_CREATED_AT);
                    if (columnIndex > -1) {
                        String createdAt = cursor.getString(columnIndex);
                        report.setCreatedAt(createdAt);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_WORK_FRONT_NAME);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setWorkFrontName(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_AREA_NAME);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setAreaName(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_RESPONSIBLE);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setResponsibleName(value);
                    }

                    columnIndex = cursor.getColumnIndex(ReportEntry.COLUMN_CRIT_RISKS);
                    if (columnIndex > -1) {
                        String value = cursor.getString(columnIndex);
                        report.setCriticalRisksName(value);
                    }

                    // adding report to list
                    reports.add(report);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // return list
        return reports;
    }
}
