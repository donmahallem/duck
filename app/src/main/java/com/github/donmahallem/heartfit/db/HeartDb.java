package com.github.donmahallem.heartfit.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class HeartDb {
    public final static int DATABASE_VERSION = 0;
    private final HeartDbHelper mDbHelper;

    private HeartDb(Context context) {

        this.mDbHelper = new HeartDbHelper(context, "heartrate.db", new SQLiteDatabase.CursorFactory() {
            @Override
            public Cursor newCursor(SQLiteDatabase sqLiteDatabase, SQLiteCursorDriver sqLiteCursorDriver, String s, SQLiteQuery sqLiteQuery) {
                return null;
            }
        }, DATABASE_VERSION);
    }
}
