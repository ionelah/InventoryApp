package com.example.android.inventoryapp;

//import android.app.LoaderManager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;

//import android.content.CursorLoader;
//import android.content.Loader;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Identifier for the floawer data loader
    private static final int FLOWER_LOADER = 0;
    //our adapter for the ListView
    FlowerCursorAdapter defaultCursorAdaptor;

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

        // Find the ListView which will be populated with the flower data
        ListView flowerListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = findViewById(R.id.empty_text_view);
        flowerListView.setEmptyView(emptyView);

        //setup an adapter to create a list item for each row of flower data in the Cursor
        defaultCursorAdaptor = new FlowerCursorAdapter(this, null);
        flowerListView.setAdapter(defaultCursorAdaptor);
        //setup item click listener
        flowerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //create new intent to go to EditorActivity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                /*
                 * form the content URI that represents the specific flower that was clicked on,
                 * by appending the id (passed as input to this method) onto the FlowerEntry.CONTENT_URI
                 */
                Uri currentFlowerUri = ContentUris.withAppendedId(FlowerEntry.CONTENT_URI, id);

                //set the Uri on the data field of the intent
                intent.setData(currentFlowerUri);

                //launch the EditorActivity to display the data for the current flower
                startActivity(intent);
            }
        });

        //kick off the loader
        getSupportLoaderManager().initLoader(FLOWER_LOADER, null, this);
    }

    //inflate the menu options for the main menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //Helper method to insert hardcoded flower data into the database.
    private void insertFlower() {

        // Create a ContentValues object where column names are the keys,
        // and Daisy's flower attributes are the values.
        ContentValues values = new ContentValues();
        values.put(FlowerEntry.COLUMN_FLOWER_NAME, getString(R.string.flower_name_added));
        values.put(FlowerEntry.COLUMN_PRICE, 10);
        values.put(FlowerEntry.COLUMN_QUANTITY, 4);
        values.put(FlowerEntry.COLUMN_SUPPLIER_NAME, getString(R.string.supplier_name_added));
        values.put(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO, getString(R.string.supplier_phone_added));

        // Insert a new row for Daisy into the provider using the ContentResolver.
        Uri newUri = getContentResolver().insert(FlowerEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertFlower();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllFlowers();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define a projection that specifies the columns from the table we care about
        String[] projection = {
                FlowerEntry._ID,
                FlowerEntry.COLUMN_FLOWER_NAME,
                FlowerEntry.COLUMN_PRICE,
                FlowerEntry.COLUMN_QUANTITY};

        //this loader will execute the Content Provider query method on a background thread
        return new CursorLoader(this, //parent activity context
                FlowerEntry.CONTENT_URI,       // provider content URI to query
                projection,                 //columns to include in the resulting Cursor
                null,               // no selection cause
                null,            // no selection arguments
                null);              // default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update FlowerCursorAdaptor with this new cursor containing updated data flower
        defaultCursorAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        defaultCursorAdaptor.swapCursor(null);

    }

    // Helper method to delete all flowers in the database.
    private void deleteAllFlowers() {
        int rowsDeleted = getContentResolver().delete(FlowerEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from inventory database");
    }
}
