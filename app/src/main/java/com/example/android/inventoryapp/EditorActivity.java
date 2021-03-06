package com.example.android.inventoryapp;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the flower data loader
    private static final int EXISTING_FLOWER_LOADER = 0;

    // Content URI for the existing flower (null if it's a new flower/product)
    private Uri initialCurrentFlowerUri;

    private EditText initialFlowerNameEditText;
    private EditText initialPriceEditText;
    private EditText initialQuantityEditText;
    private EditText initialSupplierNameEditText;
    private EditText initialSupplierPhoneEditText;

    //current quantity of product
    private int initialQuantity;

    //Boolean flag that keeps track of whether the flower has been edited (true) or not (false)
    private boolean initialFlowerHasChanged = false;

    /*
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the initialFlowerHasChanged boolean to true.
     */
    private View.OnTouchListener defaultTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            initialFlowerHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /*
         * examine the intent that was used to launch this activity
         *in order to figure out if we are creating a new flower or editing an existing one
         */
        Intent intent = getIntent();
        initialCurrentFlowerUri = intent.getData();

        //find all relevant views to read user input form
        initialFlowerNameEditText = findViewById(R.id.edit_flower_name);
        initialPriceEditText = findViewById(R.id.edit_price);
        initialQuantityEditText = findViewById(R.id.edit_quantity);
        initialSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        initialSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
        Button increaseQuantityButton = findViewById(R.id.increase_button);
        Button decreaseQuantityButton = findViewById(R.id.decrease_button);

        //if the intent doesn't contain a flower content URI, then we know that we are creating a new flower
        if (initialCurrentFlowerUri == null) {
            //this is a new flower so we have to change the bar app to "add a new flower"
            setTitle(getString(R.string.editor_activity_title_add_new_flower));
            initialFlowerNameEditText.setEnabled(true);
            initialPriceEditText.setEnabled(true);
            initialQuantityEditText.setEnabled(true);
            initialSupplierNameEditText.setEnabled(true);
            initialSupplierPhoneEditText.setEnabled(true);
            increaseQuantityButton.setVisibility(View.GONE);
            decreaseQuantityButton.setVisibility(View.GONE);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            //otherwise this is a an existing flower so change the bar app to "Edit flower"
            setTitle(getString(R.string.editor_activity_title_edit_flower));
            initialFlowerNameEditText.setEnabled(false);
            initialPriceEditText.setEnabled(false);
            initialQuantityEditText.setEnabled(false);
            initialSupplierNameEditText.setEnabled(false);
            initialSupplierPhoneEditText.setEnabled(false);
            increaseQuantityButton.setVisibility(View.VISIBLE);
            decreaseQuantityButton.setVisibility(View.VISIBLE);
            //Initialize a loader to read the flower data from the database and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_FLOWER_LOADER, null, this);
        }

        /*
         * Setup OnTouchListeners on all the input fields, so we can determine if the user
         * has touched or modified them. This will let us know if there are unsaved changes
         * or not, if the user tries to leave the editor without saving.
         */
        initialFlowerNameEditText.setOnTouchListener(defaultTouchListener);
        initialPriceEditText.setOnTouchListener(defaultTouchListener);
        initialQuantityEditText.setOnTouchListener(defaultTouchListener);
        initialSupplierNameEditText.setOnTouchListener(defaultTouchListener);
        initialSupplierPhoneEditText.setOnTouchListener(defaultTouchListener);
        increaseQuantityButton.setOnTouchListener(defaultTouchListener);
        decreaseQuantityButton.setOnTouchListener(defaultTouchListener);

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity(v);
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity(v);
            }
        });
    }

    //get user input from editor and save new flower into database
    private boolean saveFlower() {

        //read from input fields
        String nameString = initialFlowerNameEditText.getText().toString().trim();
        String priceString = initialPriceEditText.getText().toString().trim();
        //int price = Integer.parseInt(priceString);
        String quantityString = initialQuantityEditText.getText().toString().trim();
        //int quantity = Integer.parseInt(quantityString);
        String supplierNameString = initialSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = initialSupplierPhoneEditText.getText().toString().trim();

        // Check if this is supposed to be a new flower and check if any of the fields in the editor are blank
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.product_name_err_msg), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.product_price_err_msg), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.product_quantity_err_msg), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.product_supplierName_err_msg), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(this, getString(R.string.product_supplierPhone_err_msg), Toast.LENGTH_SHORT).show();
            return false;
        }

        //create a ContentValue object where column name are keys and flower attributes from editor values
        ContentValues values = new ContentValues();
        values.put(FlowerEntry.COLUMN_FLOWER_NAME, nameString);
        values.put(FlowerEntry.COLUMN_PRICE, Integer.parseInt(priceString));
        values.put(FlowerEntry.COLUMN_QUANTITY, Integer.parseInt(quantityString));
        values.put(FlowerEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO, supplierPhoneString);

        // Determine if this is a new or existing flower by checking if initialCurrentFlowerUri is null or not
        if (initialCurrentFlowerUri == null) {
            // This is a NEW flower, so insert a new flower into the provider, returning the content URI for the new flower.
            Uri newUri = getContentResolver().insert(FlowerEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_flower_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_flower_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING flower, so update the flower with content URI: initialCurrentFlowerUri
            int rowsAffected = getContentResolver().update(initialCurrentFlowerUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_flower_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_flower_successful), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new flower, hide the "Delete" menu item.
        if (initialCurrentFlowerUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //save flower to database
               if (saveFlower()) {
                   finish();
               }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order" menu option
            case R.id.action_order:
                orderFlowers();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                if (!initialFlowerHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                /*
                 * Otherwise if there are unsaved changes, setup a dialog to warn the user.
                 * Create a click listener to handle the user confirming that changes should be discarded.
                 */

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This method is called when Order Menu is sellected
    private void orderFlowers() {
        if (initialQuantity > 0) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            String supplierPhoneString = initialSupplierPhoneEditText.getText().toString().trim();
            intent.setData(Uri.parse("tel:" + supplierPhoneString));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.phone_app_missing_err_msg, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.order_error_msg, Toast.LENGTH_SHORT).show();
        }
    }

    // This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the flower hasn't changed, continue with handling back button press
        if (!initialFlowerHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        // Since the editor shows all flower attributes, define a projection that contains all columns from the inventory table
        String[] projection = {
                FlowerEntry._ID,
                FlowerEntry.COLUMN_FLOWER_NAME,
                FlowerEntry.COLUMN_PRICE,
                FlowerEntry.COLUMN_QUANTITY,
                FlowerEntry.COLUMN_SUPPLIER_NAME,
                FlowerEntry.COLUMN_SUPPLIER_PHONE_NO};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,      // Parent activity context
                initialCurrentFlowerUri,          // Query the content URI for the current flower
                projection,                      // Columns to include in the resulting Cursor
                null,                    // No selection clause
                null,                 // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of flower attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_FLOWER_NAME);
            int priceColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_SUPPLIER_PHONE_NO);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            initialQuantity = quantity;
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            initialFlowerNameEditText.setText(name);
            initialPriceEditText.setText(String.valueOf(price));
            initialQuantityEditText.setText(String.valueOf(quantity));
            initialSupplierNameEditText.setText(supplierName);
            initialSupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        initialFlowerNameEditText.setText("");
        initialPriceEditText.setText("");
        initialQuantityEditText.setText("");
        initialSupplierNameEditText.setText("");
        initialSupplierPhoneEditText.setText("");
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        /*
         * Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the flower.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Prompt the user to confirm that they want to delete this flower.
    private void showDeleteConfirmationDialog() {
        /*
         * Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the flower
                deleteFlower();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog and continue editing the flower
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Perform the deletion of the flower in the database.
    private void deleteFlower() {
        // Only perform the delete if this is an existing flower.
        if (initialCurrentFlowerUri != null) {
            /*
             * Call the ContentResolver to delete the flower at the given content URi
             * Pass in null for the selection and selection args because the initialCurrentFlowerUri
             * content URI already identifies the flower that we want.
             */
            int rowsDeleted = getContentResolver().delete(initialCurrentFlowerUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_flower_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_flower_successful), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    public void increaseQuantity(View view) {
        initialQuantity++;
        initialQuantityEditText.setText(String.valueOf(initialQuantity));
    }

    public void decreaseQuantity(View view) {
        if (initialQuantity == 0) {
            Toast.makeText(this, R.string.decrease_bttn_error_msg, Toast.LENGTH_SHORT).show();
        } else {
            initialQuantity--;
            initialQuantityEditText.setText(String.valueOf(initialQuantity));
        }
    }
}
