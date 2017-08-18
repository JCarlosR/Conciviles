package com.programacionymas.conciviles.io.sqlite.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.programacionymas.conciviles.io.sqlite.MyDbContract;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.model.Report;

public class ReportRepository {

    public static void save(Context context, final Report report) {
        MyDbHelper myHelper = new MyDbHelper(context);
        SQLiteDatabase db = myHelper.getWritableDatabase();

        int id = (int) db.insertWithOnConflict(MyDbContract.ReportEntry.TABLE_NAME, null, report.getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(MyDbContract.ReportEntry.TABLE_NAME, report.getContentValues(), "id=?", new String[] {String.valueOf(report.getId())});
        }
    }

    public static void delete(Context context, final String id) {
        MyDbHelper myHelper = new MyDbHelper(context);
        SQLiteDatabase db = myHelper.getWritableDatabase();

        // delete report with this id
        db.delete(MyDbContract.ReportEntry.TABLE_NAME, MyDbContract.ReportEntry.COLUMN_ID + "=" + id, null);
    }

}
