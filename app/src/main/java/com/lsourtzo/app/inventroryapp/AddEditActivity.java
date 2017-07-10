package com.lsourtzo.app.inventroryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY;
import static java.lang.Integer.parseInt;

/**
 * Created by lsourtzo on 09/07/2017.
 */

public class AddEditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Photo parameters
     */
    private static final int CAMERA_REQUEST = 1;
    private static final int LIBRARY_REQUEST = 2;
    byte imageInByte[];

    /**
     * Identifier for context
     */
    private Context context;

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the price name
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the quantity name
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the price name
     */
    private TextView mQuantityAddEditText;

    /**
     * Spinner field to enter the product category
     */
    private Spinner mCategorySpinner;
    private int mCategory = ProductEntry.CAT_TOOLS; //looks like never accessed but they are int a switch statement do not delete

    /**
     * Spinner field to enter the product supplier name
     */
    private Spinner mSupplierSpinner;
    private int mSupplier = ProductEntry.SUPPLIER_NEO_TECH; //looks like never accessed but they are int a switch statement do not delete

    /**
     * ImageView field to enter the Image
     */
    private ImageView mImage;
    /**
     * ImageView field of the add Image button
     */
    private ImageView mImageAdd;

    /**
     * TextView field of the increase quantity button
     */
    private TextView mIncreaseQuantity;
    /**
     * TextView field of the decrease quantity button
     */
    private TextView mDecreaseQuantity;
    /**
     * TextView field of the apply quantity button
     */
    private TextView mApplyQuantity;

    /**
     * TextView field of the Contact with supplier button
     */
    private TextView mContactSupplier;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        context = getBaseContext();

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add New Product"
            setTitle(getString(R.string.AddNewProductTitle));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {

            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.EditProductTitle));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.addEditProductName);
        mPriceEditText = (EditText) findViewById(R.id.addEditPrice);
        mQuantityEditText = (EditText) findViewById(R.id.addEditQuantity);
        mQuantityAddEditText = (TextView) findViewById(R.id.addEditChangeQuantity);

        mCategorySpinner = (Spinner) findViewById(R.id.addEditCategory);
        mSupplierSpinner = (Spinner) findViewById(R.id.addEditSupplier);

        mImage = (ImageView) findViewById(R.id.addEditPhoto);
        mImageAdd = (ImageView) findViewById(R.id.addEditAddPhoto);

        mIncreaseQuantity = (TextView) findViewById(R.id.addEditQuantityIncrease);
        mDecreaseQuantity = (TextView) findViewById(R.id.addEditQuantityDecrease);
        mApplyQuantity = (TextView) findViewById(R.id.addEditQuantityApply);
        mContactSupplier = (TextView) findViewById(R.id.contactSupplier);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);
        mImageAdd.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);

        setupCategorySpinners();
        setupSupplierSpinners();


        // Click Listener For Add Image Button
        mImageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add photo
                addingImageSellectionDialog();
            }
        });

        // Click Listener For Increase by one adding quantity
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumber = parseInt(mQuantityAddEditText.getText().toString());
                int newNumber = currentNumber + 1;
                mQuantityAddEditText.setText(String.valueOf(newNumber));
            }
        });

        // Click Listener For Decrease by one adding quantity
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumber = parseInt(mQuantityAddEditText.getText().toString());
                int currentQuantity = parseInt(mQuantityEditText.getText().toString());
                if (currentNumber > currentQuantity * (-1)) {
                    int newNumber = currentNumber - 1;
                    mQuantityAddEditText.setText(String.valueOf(newNumber));
                } else {
                    Toast.makeText(context, getString(R.string.change_quantity_error_msg), Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Click Listener For Apply adding quantity
        mApplyQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumber = parseInt(mQuantityAddEditText.getText().toString());
                if (currentNumber != 0) {
                    int currentQuantity = parseInt(mQuantityEditText.getText().toString());
                    int newNumber = currentQuantity + currentNumber;
                    // we need to check this in case that we change edit text value manually and bypass above check
                    if (newNumber >= 0) {
                        mQuantityEditText.setText(String.valueOf(newNumber));
                        Toast.makeText(context, getString(R.string.quantity_update_msg), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getString(R.string.change_quantity_error_msg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.change_quantity_zero_error_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Click Listener For Contact with supplier button
        mContactSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierEmail[] = {getString(R.string.emailbasic)};

                // get Supplier email.
                switch (mSupplierSpinner.getSelectedItemPosition()) {
                    case ProductEntry.SUPPLIER_GARDENER:
                        supplierEmail[0] = getString(R.string.email_gardener);
                        break;
                    case ProductEntry.SUPPLIER_LIO_GETS:
                        supplierEmail[0] = getString(R.string.email_liogets);
                        break;
                    case ProductEntry.SUPPLIER_NEO_TECH:
                        supplierEmail[0] = getString(R.string.email_neotech);
                        break;
                    case ProductEntry.SUPPLIER_NICK_THE_FREAK:
                        supplierEmail[0] = getString(R.string.email_nickthefreak);
                        break;
                }

                // call email intent
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, supplierEmail);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_message1) + mNameEditText.getText() + getString(R.string.email_message2));

                try {
                    startActivity(Intent.createChooser(intent, getString(R.string.email_send)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, getString(R.string.no_email_client_error_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the Category of the product.
     */
    private void setupCategorySpinners() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item category per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.cat_technology))) {
                        mCategory = ProductEntry.CAT_TECHNOLOGY;
                    } else if (selection.equals(getString(R.string.cat_home))) {
                        mCategory = ProductEntry.CAT_HOME_AND_GARDEN;
                    } else if (selection.equals(getString(R.string.cat_clothes))) {
                        mCategory = ProductEntry.CAT_CLOTHES_AND_SHOES;
                    } else {
                        mCategory = ProductEntry.CAT_TOOLS;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = ProductEntry.CAT_TECHNOLOGY;
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the Category of the product.
     */
    private void setupSupplierSpinners() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item category per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_gardener))) {
                        mSupplier = ProductEntry.SUPPLIER_GARDENER;
                    } else if (selection.equals(getString(R.string.supplier_lio_gets))) {
                        mSupplier = ProductEntry.SUPPLIER_LIO_GETS;
                    } else if (selection.equals(getString(R.string.supplier_neo_tech))) {
                        mSupplier = ProductEntry.SUPPLIER_NEO_TECH;
                    } else {
                        mSupplier = ProductEntry.SUPPLIER_NICK_THE_FREAK;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = ProductEntry.CAT_TECHNOLOGY;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.save_menu, menu);
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:
                // Save product to database
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                mCurrentProductUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find individual views that we want to modify in the list item layout
            ImageView productImage = (ImageView) findViewById(R.id.addEditPhoto);
            EditText productName = (EditText) findViewById(R.id.addEditProductName);
            Spinner productCategory = (Spinner) findViewById(R.id.addEditCategory);
            EditText productPrice = (EditText) findViewById(R.id.addEditPrice);
            EditText productQuantity = (EditText) findViewById(R.id.addEditQuantity);
            Spinner productSupplier = (Spinner) findViewById(R.id.addEditSupplier);

            // Find the columns of product attributes that we're interested in
            int productImageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productCategoryColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CATEGORY);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productSupplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);

            // Read the product attributes from the Cursor for the current product
            byte[] prodImage = cursor.getBlob(productImageColumnIndex); // get byte array from table
            imageInByte = prodImage;
            Bitmap bmpImage = BitmapFactory.decodeByteArray(prodImage, 0, prodImage.length); // Convert the byte array into bitmap
            String prodName = cursor.getString(productNameColumnIndex);
            String prodCategory = cursor.getString(productCategoryColumnIndex);
            String prodPrice = cursor.getString(productPriceColumnIndex);
            String prodQuantity = cursor.getString(productQuantityColumnIndex);
            String prodSupplier = cursor.getString(productSupplierColumnIndex);

            // Update the TextViews with the attributes for the current product
            productImage.setImageBitmap(bmpImage);
            productName.setText(prodName);
            productPrice.setText(prodPrice);
            productQuantity.setText(prodQuantity);

            switch (parseInt(prodCategory)) {
                case ProductEntry.CAT_CLOTHES_AND_SHOES:
                    productCategory.setSelection(2);
                    break;
                case ProductEntry.CAT_HOME_AND_GARDEN:
                    productCategory.setSelection(3);
                    break;
                case ProductEntry.CAT_TECHNOLOGY:
                    productCategory.setSelection(0);
                    break;
                case ProductEntry.CAT_TOOLS:
                    productCategory.setSelection(1);
                    break;
            }

            switch (parseInt(prodSupplier)) {
                case ProductEntry.SUPPLIER_GARDENER:
                    productSupplier.setSelection(3);
                    break;
                case ProductEntry.SUPPLIER_LIO_GETS:
                    productSupplier.setSelection(0);
                    break;
                case ProductEntry.SUPPLIER_NEO_TECH:
                    productSupplier.setSelection(2);
                    break;
                case ProductEntry.SUPPLIER_NICK_THE_FREAK:
                    productSupplier.setSelection(1);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mImage.setImageResource(R.drawable.nophoto);
        mQuantityAddEditText.setText("0");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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
     * Prompt the user to select from where to take the picture.
     */
    private void addingImageSellectionDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.photo_msg);
        builder.setPositiveButton(R.string.photo_camera, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked to take a photo with camera
                callCamera();
            }
        });
        builder.setNegativeButton(R.string.photo_library, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked to take a photo from library
                callLibrary();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Save Product Method
     */
    public void saveProduct() {
        //get values
        // Create a ContentValues object where column names are the keys
        ContentValues values = new ContentValues();

        String name = mNameEditText.getText().toString();
        if (name.equals("") || name.length() == 0 || name.isEmpty()) {
            Toast.makeText(this, getString(R.string.name_error_msg), Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_CATEGORY, mCategorySpinner.getSelectedItemPosition());
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, mSupplierSpinner.getSelectedItemPosition());
        String price = mPriceEditText.getText().toString();
        if (price.equals("") || price.length() == 0 || price.isEmpty() || Double.parseDouble(price) <= 0) {
            Toast.makeText(this, getString(R.string.price_error_msg), Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        String quantity = mQuantityEditText.getText().toString();
        if (quantity.equals("") || quantity.length() == 0 || quantity.isEmpty() || parseInt(quantity) < 0) {
            Toast.makeText(this, getString(R.string.quantity_error_msg), Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(COLUMN_PRODUCT_QUANTITY, quantity);
        if (imageInByte == null) {
            Toast.makeText(this, getString(R.string.image_error_msg), Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageInByte);

        if (mCurrentProductUri == null) {
            // Insert a new row for product into the provider using the ContentResolver.
            // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
            // into the products database table.
            // Receive the new content URI that will allow us to access dummies data in the future.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.error_add_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.correct_add_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.error_update_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.correct_update_message),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Exit activity
        finish();
    }

    /**
     * open camera method
     */
    public void callCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
        intent.setType("image/*");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 160);
    }

    /**
     * open library method
     */
    public void callLibrary() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("aspectX", 0);
        photoPickerIntent.putExtra("aspectY", 0);
        photoPickerIntent.putExtra("outputX", 160);
        photoPickerIntent.putExtra("outputY", 200);
        startActivityForResult(photoPickerIntent, LIBRARY_REQUEST);
    }

    /**
     * On activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case CAMERA_REQUEST:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    //get bitmap from camera
                    Bitmap yourImage = extras.getParcelable("data");

                    // prepare image to store into table - covert to bitmap and finaly to byte array
                    // convert string image file name to id
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageInByte = stream.toByteArray();

                    // show image to activities imageview
                    mImage.setImageBitmap(yourImage);
                }
                break;

            case LIBRARY_REQUEST:
                // Read picked image data - its URI
                final Uri pickedImage = data.getData();
                if (pickedImage != null) {
                    // Let's read picked image path using content resolver
                    try {
                        Bitmap yourImage2 = MediaStore.Images.Media.getBitmap(context.getContentResolver(), pickedImage);
                        Bitmap resized = Bitmap.createScaledBitmap(yourImage2, 360, 400, true);

                        // prepare image to store into table - covert to bitmap and finally to byte array
                        // convert string image file name to id
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.PNG, 0, stream);
                        imageInByte = stream.toByteArray();

                        // show image to activities imageview
                        mImage.setImageBitmap(resized);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.error_delete_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.correct_delete_message),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
