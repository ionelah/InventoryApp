package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.FlowerContract.FlowerEntry;

public class FlowerCursorAdapter extends CursorAdapter {

    // Constructs a new FlowerCursorAdapter
    public FlowerCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the flower data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current flower can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name_of_flower);
        TextView priceTextView = view.findViewById(R.id.price_for_flower);
        TextView quantityTextView = view.findViewById(R.id.quantity_of_flower);
        Button saleButton = view.findViewById(R.id.sale_button);

        // Find the columns of flower attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_FLOWER_NAME);
        int priceColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(FlowerEntry.COLUMN_QUANTITY);

        // Read the flower attributes from the Cursor for the current flower
        String flowerName = cursor.getString(nameColumnIndex);
        int flowerPrice = cursor.getInt(priceColumnIndex);
        int flowerQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current flower
        nameTextView.setText(flowerName);
        priceTextView.setText(flowerPrice + " eur");
        quantityTextView.setText(flowerQuantity + " pcs");

        final int flowerItemId = cursor.getInt(cursor.getColumnIndexOrThrow(FlowerEntry._ID));
        final int flowerItemQuantity = cursor.getInt(cursor.getColumnIndex(FlowerEntry.COLUMN_QUANTITY));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flowerItemQuantity > 0) {
                    int quantity = flowerItemQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(FlowerEntry.COLUMN_QUANTITY, quantity);
                    Uri newUri = ContentUris.withAppendedId(FlowerEntry.CONTENT_URI, flowerItemId);
                    context.getContentResolver().update(newUri, values, null, null);
                } else {
                    Toast.makeText(context, R.string.sale_bttn_error_msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
