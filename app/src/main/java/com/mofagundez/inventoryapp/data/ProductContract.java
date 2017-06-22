package com.mofagundez.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Mauricio on June 20, 2017
 * <p>
 * Udacity Android Basics Nanodegree
 * Project 10: Inventory App
 */
public final class ProductContract {

    /** Define Content Authority according to {@link android.Manifest} and {@link ProductProvider} */
    public static final String CONTENT_AUTHORITY = "com.mofagundez.inventoryapp";

    /** Define Content URI  */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**  Define path for the table which will be appended to the base content URI */
    public static final String PATH_PRODUCTS = "products";

    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {

        // Create a full URI for the class
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // Table name
        public static final String TABLE_NAME = "products";

        // The _id field to index the table content
        public static final String _ID = BaseColumns._ID;

        // The product name
        public static final String COLUMN_NAME = "name";

        // The product quantity
        public static final String COLUMN_QUANTITY = "quantity";

        // The product price
        public static final String COLUMN_PRICE = "price";

        // The product image
        public static final String COLUMN_IMAGE = "image";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    }

}
