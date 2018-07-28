package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;

//  ContentProvider for InventoryApp
public class FlowerProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = FlowerProvider.class.getSimpleName();

    // URI matcher code for the content URI for the inventory table
    private static final int INVENTORY = 100;

    // URI matcher code for the content URI for a single flower from the inventory table
    private static final int INVENTORY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        /*
         * The content URI of the form "content://com.example.android.inventory/inventory" will map to the
         * integer code INVENTORY. This URI is used to provide access to MULTIPLE rows of inventory table
         */
        sUriMatcher.addURI(FlowerContract.CONTENT_AUTHORITY, FlowerContract.PATH_INVENTORY, INVENTORY);

        /*
         * The content URI of the form "content://com.example.android.inventory/inventory/#" will map to the
         * integer code INVENTORY_ID. This URI is used to provide access to ONE single row of the inventory table.
         */
        sUriMatcher.addURI(FlowerContract.CONTENT_AUTHORITY, FlowerContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    //Database helper object
    private FlowerDbHelper defaultDbHelper;

    //Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        defaultDbHelper = new FlowerDbHelper(getContext());
        return true;
    }

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //get readable database
        SQLiteDatabase database = defaultDbHelper.getReadableDatabase();

        //this cursor will hold the result of query
        Cursor cursor;

        //figure out if the URI Matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                /*
                 * For the INVENTORY code, query the inventory table directly with the given
                 * projection, selection, selection arguments, and sort order. The cursor
                 * could contain multiple rows of the inventory table.
                 */
                cursor = database.query(FlowerEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                /*
                 * For the INVENTORY_ID code, extract out the ID from the URI.
                 * For every "?" in the selection, we need to have an element in the selection
                 * arguments that will fill in the "?".
                 */
                selection = FlowerEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(FlowerEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        /*
         * Set notification URI on the Cursor, so we know what content URI the Cursor was created for.
         * If the data at this URI changes, then we know we need to update the Cursor.
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //Insert new data into the provider with the given ContentValues
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertFlower(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /*
     * Insert a flower into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertFlower(Uri uri, ContentValues values) {

        //check sanity data for the attributes
        String name = values.getAsString(FlowerEntry.COLUMN_FLOWER_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Flower requires a name");
        }

        Integer price = values.getAsInteger(FlowerEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price value must be completed and greater than 0");
        }

        Integer quantity = values.getAsInteger(FlowerEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater or equal with 0 ");
        }

        String supplierName = values.getAsString(FlowerEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null || supplierName.isEmpty()) {
            throw new IllegalArgumentException("Flower must have a supplier name");
        }

        String supplierPhoneNumber = values.getAsString(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO);
        if (supplierPhoneNumber == null || supplierPhoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Flower must have a supplier phone number");
        }

        //get writable databases
        SQLiteDatabase database = defaultDbHelper.getWritableDatabase();

        //insert the new flower with the given values
        long id = database.insert(FlowerEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the flower content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    // Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateFlower(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                /*
                 * For the INVENTORY_ID code, extract out the ID from the URI,
                 * so we know which row to update. Selection will be "_id=?" and selection
                 * arguments will be a String array containing the actual ID.
                 */
                selection = FlowerEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateFlower(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update flowers in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more flowers).
     * Return the number of rows that were successfully updated.
     */
    private int updateFlower(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        /*
         * Update the selected flowers in the inventory database table with the given ContentValues
         * Check the sanity for the attributes
         */
        if (values.containsKey(FlowerEntry.COLUMN_FLOWER_NAME)) {
            String name = values.getAsString(FlowerEntry.COLUMN_FLOWER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Flower requires a name");
            }
        }

        if (values.containsKey(FlowerEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(FlowerEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Price value must be completed and greater than 0");
            }
        }

        if (values.containsKey(FlowerEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(FlowerEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Quantity must be greater or equal with 0 ");
            }
        }

        if (values.containsKey(FlowerEntry.COLUMN_FLOWER_NAME)) {
            String supplierName = values.getAsString(FlowerEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null || supplierName.isEmpty()) {
                throw new IllegalArgumentException("Flower must have a supplier name");
            }
        }

        if (values.containsKey(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO)) {
            String supplierPhoneNumber = values.getAsString(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO);
            if (supplierPhoneNumber == null || supplierPhoneNumber.isEmpty()) {
                throw new IllegalArgumentException("Flower must have a supplier phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = defaultDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(FlowerEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    // Delete the data at the given selection and selection arguments.

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = defaultDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(FlowerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = FlowerEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(FlowerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    // Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return FlowerEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return FlowerEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}


