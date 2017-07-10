package com.lsourtzo.app.inventroryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry.COLUMN_PRODUCT_NAME;
import static com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;
import static com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER;
import static java.lang.Integer.parseInt;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * No result Image
     */
    ImageView noResult;

    /**
     * Close search
     */
    MenuItem closeSearchItem;

    /**
     * searchview
     */
    MenuItem myActionMenuItem;

    /**
     * searchview
     */
    SearchView searchView;

    /**
     * Identifier for the products data loader
     */
    private static final int PRODUCT_LOADER_ID = 1;

    /**
     * Adapter for the ListView
     */
    ProductsCursorAdapter mCursorAdapter;

    /**
     * Add dummies data textview
     */
    TextView addDummiesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });

        // Find the noresult mageview
        noResult = (ImageView) findViewById(R.id.noresults);

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ProductsCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        // Define add dummies text view button
        addDummiesButton = (TextView) findViewById(R.id.title_text_add_dummies);
        // Setup the add dummies button listener
        addDummiesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addDummiesData();
            }
        });

        // Setup the item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, AddEditActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.products/product/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener deleteAllButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int rowsAffected = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
                            invalidateOptionsMenu();
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(deleteAllButtonClickListener);
            return true;
        } else if (id == R.id.close) {
            closeSearchItem.setVisible(false);

            // clean the exist adapter to create new one with new cursor
            mCursorAdapter.swapCursor(null);

            // Define a projection that specifies the columns from the table we care about.
            String[] projection = {
                    ProductEntry._ID,
                    ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductEntry.COLUMN_PRODUCT_IMAGE,
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                    ProductEntry.COLUMN_PRODUCT_CATEGORY,
                    ProductEntry.COLUMN_PRODUCT_PRICE,
                    COLUMN_PRODUCT_QUANTITY};

            // creating cursor
            Cursor cursor = getContentResolver().query(
                    ProductEntry.CONTENT_URI,
                    projection,    // Which columns to return.
                    null,          // WHERE clause.
                    null,          // WHERE clause value substitution
                    null);      // Sort order.

            // apply the new cursor in to adapter
            mCursorAdapter.swapCursor(cursor);

            // change title back to normal
            setTitle(getString(R.string.app_name));

            // show search icon again
            myActionMenuItem.setVisible(true);

            // Hide no result image
            noResult.setVisibility(View.GONE);

        } else if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        getMenuInflater().inflate(R.menu.close_menu, menu);
        getMenuInflater().inflate(R.menu.delete_menu, menu);

        // change title back to normal
        setTitle(getString(R.string.app_name));

        // disappear X button ... that will be showed only when we have done some search
        closeSearchItem = menu.findItem(R.id.close);
        closeSearchItem.setVisible(false);

        myActionMenuItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // clean the exist adapter to create new one with new cursor
                mCursorAdapter.swapCursor(null);

                // Define a projection that specifies the columns from the table we care about.
                String[] projection = {
                        ProductEntry._ID,
                        ProductEntry.COLUMN_PRODUCT_NAME,
                        ProductEntry.COLUMN_PRODUCT_IMAGE,
                        ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                        ProductEntry.COLUMN_PRODUCT_CATEGORY,
                        ProductEntry.COLUMN_PRODUCT_PRICE,
                        COLUMN_PRODUCT_QUANTITY};

                // create selection which must be LIKE and not = because we want to find all records
                // that contain the query text.
                String selection = COLUMN_PRODUCT_NAME + " LIKE ?";
                // create the arguments string array which must be included in % %
                String[] selectionArgs = {"%" + query + "%"};

                // set title as the query text
                setTitle(query);

                // creating cursor
                Cursor cursor = getContentResolver().query(
                        ProductEntry.CONTENT_URI,
                        projection,    // Which columns to return.
                        selection,          // WHERE clause.
                        selectionArgs,          // WHERE clause value substitution
                        null);      // Sort order.

                // apply the new cursor in to adapter
                mCursorAdapter.swapCursor(cursor);

                // hide search icon
                myActionMenuItem.setVisible(false);

                if (cursor.getCount() == 0) {
                    // Show no result image
                    noResult.setVisibility(View.VISIBLE);
                }
                closeSearchItem.setVisible(true);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    /**
     * Helper method to insert hardcoded products data into the database. For debugging purposes only.
     */
    private void addDummiesData() {

        new Thread(new Runnable() {
            public void run() {

                //Get Data From Text Resource File Contains Json Data.
                InputStream inputStream = getResources().openRawResource(R.raw.dummies);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                // Create a list
                final ArrayList<List> lists = new ArrayList<List>();
                // Parsing json file to take data
                int ctr;
                try {
                    ctr = inputStream.read();
                    while (ctr != -1) {
                        byteArrayOutputStream.write(ctr);
                        ctr = inputStream.read();
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    // Parse the data into json object to get dummies data in form of json.
                    JSONObject jObject = new JSONObject(
                            byteArrayOutputStream.toString());
                    JSONArray jArray = jObject.getJSONArray("Products");

                    String jName;
                    String jImage;
                    int jCategory;
                    int jQuantity;
                    int jSupplier;
                    double jPrice;

                    // parse json file for extract dummies data
                    for (int i = 0; i < jArray.length(); i++) {

                        jName = jArray.getJSONObject(i).getString("name");
                        jImage = jArray.getJSONObject(i).getString("image");
                        jPrice = Double.parseDouble(jArray.getJSONObject(i).getString("price"));
                        jCategory = parseInt(jArray.getJSONObject(i).getString("category"));
                        jSupplier = parseInt(jArray.getJSONObject(i).getString("supplier"));
                        jQuantity = parseInt(jArray.getJSONObject(i).getString("quantity"));

                        // Create a ContentValues object where column names are the keys,
                        // and dummies data attributes are the values.
                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_PRODUCT_NAME, jName);
                        values.put(ProductEntry.COLUMN_PRODUCT_CATEGORY, jCategory);
                        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, jSupplier);
                        values.put(COLUMN_PRODUCT_QUANTITY, jQuantity);
                        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, jPrice);

                        // prepare image to store into table - covert to bitmap and finaly to byte array
                        // convert string image file name to id
                        int idImage = getResources().getIdentifier(jImage, "drawable", getBaseContext().getPackageName());
                        Bitmap icon = BitmapFactory.decodeResource(getBaseContext().getResources(), idImage);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        icon.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bmp = stream.toByteArray();
                        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, bmp);

                        // Insert a new row for dummies into the provider using the ContentResolver.
                        // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
                        // into the products database table.
                        // Receive the new content URI that will allow us to access dummies data in the future.
                        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Set Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_CATEGORY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                COLUMN_PRODUCT_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated products data
        mCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param deleteAllButtonClickListener is the click listener for what to do when
     *                                     the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener deleteAllButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_delete_all_database);
        builder.setPositiveButton(R.string.delete, deleteAllButtonClickListener);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * this method will be show all supplier other items
     *
     * @param supplierCode recieve the code of supplier
     */
    void seeOtherSellerItems(int supplierCode) {
        // clean the exist adapter to create new one with new cursor
        mCursorAdapter.swapCursor(null);

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_CATEGORY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                COLUMN_PRODUCT_QUANTITY};

        // create selection which must be LIKE and not = because we want to find all records
        // that contain the query text.
        String selection = COLUMN_PRODUCT_SUPPLIER + " =?";
        // create the arguments string array which must be included in % %
        String[] selectionArgs = {String.valueOf(supplierCode)};

        // set title as the query text
        switch (supplierCode) {
            case ProductEntry.SUPPLIER_GARDENER:
                setTitle("Supplier: Gardener");
                break;
            case ProductEntry.SUPPLIER_LIO_GETS:
                setTitle("Supplier: Lio Gets");
                break;
            case ProductEntry.SUPPLIER_NEO_TECH:
                setTitle("Supplier: Neo Tech");
                break;
            case ProductEntry.SUPPLIER_NICK_THE_FREAK:
                setTitle("Supplier: Nick The Freak");
                break;
        }

        // creating cursor
        Cursor cursor = getContentResolver().query(
                ProductEntry.CONTENT_URI,
                projection,    // Which columns to return.
                selection,          // WHERE clause.
                selectionArgs,          // WHERE clause value substitution
                null);      // Sort order.

        // apply the new cursor in to adapter
        mCursorAdapter.swapCursor(cursor);

        // hide search icon
        myActionMenuItem.setVisible(false);

        //show X close icon in toolbar
        closeSearchItem.setVisible(true);
    }
}
