package com.lsourtzo.app.inventroryapp;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lsourtzo.app.inventroryapp.data.ProductContract.ProductEntry;

import static java.lang.Integer.parseInt;

/**
 * {@link ProductsCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductsCursorAdapter extends CursorAdapter {

    private Context context;

    /**
     * Constructs a new {@link ProductsCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductsCursorAdapter(Context context, Cursor c) {
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
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        ImageView productImage = (ImageView) view.findViewById(R.id.product_image);
        TextView productName = (TextView) view.findViewById(R.id.text_product_name);
        TextView productCategory = (TextView) view.findViewById(R.id.text_product_cat);
        TextView productPrice = (TextView) view.findViewById(R.id.text_product_price);
        TextView productQuantity = (TextView) view.findViewById(R.id.text_product_available);
        TextView productSupplier = (TextView) view.findViewById(R.id.text_product_supplier);

        // Find the columns of product attributes that we're interested in
        int productImageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int productCategoryColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CATEGORY);
        int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productSupplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);

        // Read the product attributes from the Cursor for the current product
        byte[] prodImage = cursor.getBlob(productImageColumnIndex); // get byte array from table
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
                productCategory.setText(R.string.clothes_and_Shoes);
                break;
            case ProductEntry.CAT_HOME_AND_GARDEN:
                productCategory.setText(R.string.home_garden);
                break;
            case ProductEntry.CAT_TECHNOLOGY:
                productCategory.setText(R.string.technology);
                break;
            case ProductEntry.CAT_TOOLS:
                productCategory.setText(R.string.tools);
                break;
        }

        switch (parseInt(prodSupplier)) {
            case ProductEntry.SUPPLIER_GARDENER:
                productSupplier.setText(R.string.gardener);
                break;
            case ProductEntry.SUPPLIER_LIO_GETS:
                productSupplier.setText(R.string.liogets);
                break;
            case ProductEntry.SUPPLIER_NEO_TECH:
                productSupplier.setText(R.string.neothech);
                break;
            case ProductEntry.SUPPLIER_NICK_THE_FREAK:
                productSupplier.setText(R.string.nickthefreak);
                break;
        }

        /** This is the code that apply changes every time we press sell button
         * in any listview item
         */
        TextView buyOneButton = (TextView) view.findViewById(R.id.title_text_product_buy_one);
        final TextView quantity = (TextView) view.findViewById(R.id.text_product_available);

        // I am using tags to pass the correct position in to onclick listener otherwise
        // they are se on the last clicked item or last viewed in the list item.
        int pos = cursor.getPosition();
        buyOneButton.setTag(pos);

        buyOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int available = parseInt(quantity.getText().toString());

                //in the follow line we get the correct position for the list item where the button belong
                Integer pos = (Integer) v.getTag();
                // move the cursor to the correct position ...
                cursor.moveToPosition(pos);

                //recreate the menu and hide X button
                ActivityCompat.invalidateOptionsMenu((Activity) context);

                if (available > 0) {
                    available = available - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, available);

                    // Make a new uri for the current row
                    int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
                    Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                    // Update the current row
                    int rowsAffected = context.getContentResolver().update(currentUri, values, null, null);

                    Toast.makeText(context, context.getString(R.string.sellOne), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.noItemLeft), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** This is the code which be show seller other items every time
         *  the store button pressed
         */
        ImageView mStore = (ImageView) view.findViewById(R.id.supplier_store_button);
        final int supplierCode = parseInt(prodSupplier);
        mStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof CatalogActivity) {
                    ((CatalogActivity) context).seeOtherSellerItems(supplierCode);
                }
            }
        });
    }
}