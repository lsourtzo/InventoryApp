package com.lsourtzo.app.inventroryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry;

/**
 * {@link ContentProvider} for Products app.
 */
public class ProductProvider extends ContentProvider {

    private ProductDbHelper mDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the Products table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCTS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);

    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the products code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.products/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the category is valid
        Integer cat = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_CATEGORY);
        if (cat == null || !ProductEntry.isValidCategory(cat)) {
            throw new IllegalArgumentException("Category requires valid value");
        }

        // Check that the suplier is valid
        Integer supplier = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null || !ProductEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException("Supplier requires valid value");
        }

        // If the Price is provided, check that it's greater than 0
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price <= 0) {
            throw new IllegalArgumentException("Price requires be greater than 0");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity requires be greater than or equal to 0");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // notify all listener that data has changed for the product content Uri
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCTS_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_CATEGORY} key is present,
        // check that the gender value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_CATEGORY)) {
            Integer cat = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_CATEGORY);
            if (cat == null || !ProductEntry.isValidCategory(cat)) {
                throw new IllegalArgumentException("Category requires valid value");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER)) {
            Integer supplier = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier == null || !ProductEntry.isValidSupplier(supplier)) {
                throw new IllegalArgumentException("Supplier requires valid value");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the weight value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price <= 0) {
                throw new IllegalArgumentException("Price requires be greater than 0");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY} key is present,
        // check that the weight value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Quantity requires be greater than or equal to 0");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // notify all listener that data has changed for the products content Uri
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                return deleteProducts(uri, selection, selectionArgs);
            case PRODUCTS_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteProducts(uri, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    private int deleteProducts(Uri uri, String selection, String[] selectionArgs) {

        // Track the number of rows that were deleted
        int rowsDeleted;

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Delete all rows that match the selection and selection args
        // For  case PRODUCTS:
        rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);

        // notify all listener that data has changed for the product content Uri
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
