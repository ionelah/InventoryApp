package com.example.android.inventoryapp.data;

import android.provider.BaseColumns;

public final class FlowerContract {
    private FlowerContract() {
        throw new AssertionError("No instances for you!");
    }

    public static final class FlowerEntry implements BaseColumns {

        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FLOWER_NAME = "Product_name";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE_NO = "Supplier_phone_number";
    }
}
