package com.mofagundez.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mofagundez.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Mauricio on June 20, 2017
 * <p>
 * Udacity Android Basics Nanodegree
 * Project 10: Inventory App
 */
public class ProductDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ( " +
            ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            ProductEntry.COLUMN_PRICE + " REAL NOT NULL, " +
            ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
            ProductEntry.COLUMN_IMAGE +  " BLOB ) " +
            ";";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("SQLENTRIES", SQL_CREATE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table or read from existent
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing :)
    }
}
