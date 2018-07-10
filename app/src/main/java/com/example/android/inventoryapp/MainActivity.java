package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;
import com.example.android.inventoryapp.data.FlowerDbHelper;

import android.support.design.widget.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // Database helper that will provide us access to the database
    private FlowerDbHelper defaultDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set FAB to open Editor activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        defaultDbHelper = new FlowerDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    //method what helps to display the inventory in a TextView
    private void displayDatabaseInfo() {

        SQLiteDatabase db = defaultDbHelper.getReadableDatabase();
        //query
        String[] projection = {
                FlowerEntry._ID,
                FlowerEntry.COLUMN_FLOWER_NAME,
                FlowerEntry.COLUMN_PRICE,
                FlowerEntry.COLUMN_QUANTITY,
                FlowerEntry.COLUMN_SUPPLIER_NAME,
                FlowerEntry.COLUMN_SUPPLIER_PHONE_NO
        };

        Cursor cursor = db.query(
                FlowerEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        try {
            //display the number of rows in the Cursor
            TextView displayView = findViewById(R.id.text_view_flower);

            //create the header for the table to be shown
            displayView.setText(getString(R.string.display_table_contains) + cursor.getCount() + " " + getString(R.string.flowers) + "\n");
            displayView.append(FlowerEntry._ID + " - " +
                    FlowerEntry.COLUMN_FLOWER_NAME + " - " +
                    FlowerEntry.COLUMN_PRICE + " - " +
                    FlowerEntry.COLUMN_QUANTITY + " - " +
                    FlowerEntry.COLUMN_SUPPLIER_NAME + " - " +
                    FlowerEntry.COLUMN_SUPPLIER_PHONE_NO + "\n");

            //find out the index for each column
            int idColumnIndex = cursor.getColumnIndex(FlowerEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_FLOWER_NAME);
            int priceColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO);

            while (cursor.moveToNext()) {
                int currentId = cursor.getInt(idColumnIndex);
                String currentFlowerName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentPhoneNoSupplier = cursor.getString(supplierPhoneColumnIndex);

                //display the value in TextView
                displayView.append("\n" + currentId + " - " + currentFlowerName + " - " +
                        currentPrice + " - " + currentQuantity + " - " + currentSupplierName +
                        " - " + currentPhoneNoSupplier);
            }
        } finally {
            cursor.close();
        }
    }

    //inflate the menu options for the main menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void insertFlower() {

        // Gets the database in write mode
        SQLiteDatabase db = defaultDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and Daisy's flower attributes are the values.
        ContentValues values = new ContentValues();
        values.put(FlowerEntry.COLUMN_FLOWER_NAME, getString(R.string.flower_name_added));
        values.put(FlowerEntry.COLUMN_PRICE, 10);
        values.put(FlowerEntry.COLUMN_QUANTITY, 4);
        values.put(FlowerEntry.COLUMN_SUPPLIER_NAME, getString(R.string.supplier_name_added));
        values.put(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO, getString(R.string.supplier_phone_added));


        long newRowId = db.insert(FlowerEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertFlower();
                displayDatabaseInfo();
                return true;
            case R.id.action_delete_all_entries:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
