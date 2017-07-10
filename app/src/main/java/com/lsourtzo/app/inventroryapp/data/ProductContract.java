package com.lsourtzo.app.inventroryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the products app.
 */
public final class ProductContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.lsourtzo.app.inventroryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.products/products/ is a valid path for
     * looking at product data. content://com.example.android.products/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "products";

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        //set Loader
        public static final int PRODUCT_LOADER = 0;

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);


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

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Price of the product.
         *
         * Type: Double
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Image of the product.
         *
         * Type: BLOB
         */
        public final static String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * Category of the product.
         *
         * The only possible values are {@link #CAT_TECHNOLOGY}, {@link #CAT_TOOLS},
         * {@link #CAT_CLOTHES_AND_SHOES} or {@link #CAT_HOME_AND_GARDEN}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_CATEGORY = "category";

        /**
         * Possible values for the Category of the product.
         */
        public static final int CAT_TECHNOLOGY = 0;
        public static final int CAT_TOOLS = 1;
        public static final int CAT_CLOTHES_AND_SHOES = 2;
        public static final int CAT_HOME_AND_GARDEN = 3;

        /**
         * Returns whether or not the given Category is {@link #CAT_TECHNOLOGY}, {@link #CAT_TOOLS},
         * {@link #CAT_CLOTHES_AND_SHOES} or {@link #CAT_HOME_AND_GARDEN}.
         */
        public static boolean isValidCategory(int category) {
            if (category == CAT_TECHNOLOGY || category == CAT_TOOLS || category == CAT_CLOTHES_AND_SHOES || category == CAT_HOME_AND_GARDEN ) {
                return true;
            }
            return false;
        }

        /**
         * Supplier of the product.
         *
         * The only possible values are {@link #SUPPLIER_LIO_GETS}, {@link #SUPPLIER_NICK_THE_FREAK},
         * {@link #SUPPLIER_NEO_TECH} or {@link #SUPPLIER_GARDENER}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_SUPPLIER = "supplier";

        /**
         * Possible values for the Supplier of the product.
         */
        public static final int SUPPLIER_LIO_GETS = 0;
        public static final int SUPPLIER_NICK_THE_FREAK = 1;
        public static final int SUPPLIER_NEO_TECH = 2;
        public static final int SUPPLIER_GARDENER = 3;

        /**
         * Returns whether or not the given Supplier is {@link #SUPPLIER_LIO_GETS}, {@link #SUPPLIER_NICK_THE_FREAK},
         * {@link #SUPPLIER_NEO_TECH} or {@link #SUPPLIER_GARDENER}.
         */
        public static boolean isValidSupplier(int supplier) {
            if (supplier == SUPPLIER_LIO_GETS || supplier == SUPPLIER_NICK_THE_FREAK || supplier == SUPPLIER_NEO_TECH || supplier == SUPPLIER_GARDENER ) {
                return true;
            }
            return false;
        }
    }
}