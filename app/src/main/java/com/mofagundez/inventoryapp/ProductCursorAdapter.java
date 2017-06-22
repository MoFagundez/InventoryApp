package com.mofagundez.inventoryapp;

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

import com.mofagundez.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Mauricio on June 20, 2017
 * <p>
 * Udacity Android Basics Nanodegree
 * Project 10: Inventory App
 */
public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate and return a new view without binding any data
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Find product name, price and quantity fields to populate when inflating view
        TextView productName = (TextView) view.findViewById(R.id.text_view_name);
        TextView productPrice = (TextView) view.findViewById(R.id.text_view_price);
        TextView productQuantity = (TextView) view.findViewById(R.id.text_view_quantity);

        // Extract values from Cursor object
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));
        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID)));

        // Populate TextViews with values extracted from Cursor object
        productName.setText(name);
        // Price wasn't formatted to currency on purpose
        productPrice.setText(context.getString(R.string.label_price) + " " + price);
        productQuantity.setText(quantity + " " + context.getString(R.string.label_quantity));

        // Find sale Button
        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        // Set Button click listener
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity in stock is higher than zero
                if (quantity > 0) {
                    // Assign a new quantity value of minus one to represent one item sold
                    int newQuantity = quantity - 1;
                    // Create and initialise a new ContentValue object with the new quantity
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, newQuantity);
                    // Update the database
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    // Inform the user that quantity is zero and can't be updated
                    Toast.makeText(context, context.getString(R.string.toast_product_out_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
