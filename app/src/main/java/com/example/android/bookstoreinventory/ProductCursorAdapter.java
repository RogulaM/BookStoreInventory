package com.example.android.bookstoreinventory;

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

import com.example.android.bookstoreinventory.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
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
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productNameTextView = (TextView) view.findViewById(R.id.product_name_main);
        TextView productPriceTextView = (TextView) view.findViewById(R.id.product_price_main);
        final TextView productQuantityTextView = (TextView) view.findViewById(R.id.product_quantity_main);
        Button button = view.findViewById(R.id.sale_button_main);

        int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        String productName = cursor.getString(productNameColumnIndex);
        double productPrice = cursor.getDouble(productPriceColumnIndex);
        final int productQuantity = cursor.getInt(productQuantityColumnIndex);

        productNameTextView.setText(productName);
        productPriceTextView.setText(String.valueOf(productPrice));
        productQuantityTextView.setText(String.valueOf(productQuantity));


        final int idInt = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

        if (productQuantity == 0) {
            button.setText(R.string.sold_button_text_view);
            button.setEnabled(false);
        } else {
            button.setText(R.string.sale_button_text_view);
            button.setEnabled(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, idInt);
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity - 1);
                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                }
            });
        }
    }
}
