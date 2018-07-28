package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class FlowerContract {
    private FlowerContract() {
        throw new AssertionError("No instances for you!");
    }

    //content authority is the name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    //Base content uri will be used to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";

    //class that defines constant values for the inventory database table
    public static final class FlowerEntry implements BaseColumns {

        //The content URI to access the flower data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        // The MIME type of the CONTENT_URI for a list of flowers
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // The MIME type of the CONTENT_URI for a single flower
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FLOWER_NAME = "Product_name";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE_NO = "Supplier_phone_number";
    }
}
