package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;
import com.example.android.inventoryapp.data.FlowerDbHelper;


public class EditorActivity extends AppCompatActivity {

    private EditText initialFlowerNameEditText;
    private EditText initialPriceEditText;
    private EditText initialQuantityEditText;
    private EditText initialSupplierNameEditText;
    private EditText initialSupplierPhoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //find all relevant views to read user input form
        initialFlowerNameEditText = findViewById(R.id.edit_flower_name);
        initialPriceEditText = findViewById(R.id.edit_price);
        initialQuantityEditText = findViewById(R.id.edit_quantity);
        initialSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        initialSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
    }

    //get user input from editor and save new flower into database
    private void insertFlower() {
        //read from input fields
        String nameString = initialFlowerNameEditText.getText().toString().trim();
        String priceString = initialPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String quantityString = initialQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierNameString = initialSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = initialSupplierPhoneEditText.getText().toString().trim();

        //create database helper
        FlowerDbHelper defaultDbHelper = new FlowerDbHelper(this);

        //get database in write mode
        SQLiteDatabase db = defaultDbHelper.getWritableDatabase();

        //create a ContentValue object where column name are keys and flower attributes from editor values
        ContentValues values = new ContentValues();
        values.put(FlowerEntry.COLUMN_FLOWER_NAME, nameString);
        values.put(FlowerEntry.COLUMN_PRICE, price);
        values.put(FlowerEntry.COLUMN_QUANTITY, quantity);
        values.put(FlowerEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO, supplierPhoneString);

        //insert a new row foe flower into database returning the id for that
        long newRowId = db.insert(FlowerEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            //if the row id is -1 than there was an error whit insertion
            Toast.makeText(this, R.string.error_message_to_save, Toast.LENGTH_SHORT).show();
        } else {
            //otherwise the insertion was successful and we can display a toast with the row id
            Toast.makeText(this, getString(R.string.flower_saved) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertFlower();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
