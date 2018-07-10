package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;

public class FlowerDbHelper extends SQLiteOpenHelper {

    //if we change database schema we must increment the database version
    private static final int DATABASE_VERSION = 1;
    //name of database
    private static final String DATABASE_NAME = "storage.db";

    //constructs a new version of FlowerDbHelper
    public FlowerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + FlowerEntry.TABLE_NAME + " (" +
                FlowerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FlowerEntry.COLUMN_FLOWER_NAME + " TEXT NOT NULL, " +
                FlowerEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                FlowerEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                FlowerEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL," +
                FlowerEntry.COLUMN_SUPPLIER_PHONE_NO + " TEXT NOT NULL );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    //this is called when database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
