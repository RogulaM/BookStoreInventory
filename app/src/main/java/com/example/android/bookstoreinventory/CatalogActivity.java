package com.example.android.bookstoreinventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.bookstoreinventory.data.BookDbHelper;
import com.example.android.bookstoreinventory.data.StoreContract;
import com.example.android.bookstoreinventory.data.StoreContract.BookEntry;

public class CatalogActivity extends AppCompatActivity {

    private BookDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        mDbHelper = new BookDbHelper(this);
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(BookEntry.TABLE_NAME, null, null, null,
                null, null, null);

        try {
            TextView displayView = (TextView) findViewById(R.id.text_view_bookstore);
            displayView.setText(getString(R.string.rows_number_text_view_message)
                    + " "
                    + cursor.getCount()
                    + "\n");

            displayView.append(StoreContract.BookEntry._ID + " "
                    + StoreContract.BookEntry.COLUMN_PRODUCT_NAME + " "
                    + StoreContract.BookEntry.COLUMN_PRICE + " "
                    + StoreContract.BookEntry.COLUMN_QUANTITY + " "
                    + StoreContract.BookEntry.COLUMN_SUPPLIER_NAME + " "
                    + StoreContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "\n");

            while (cursor.moveToNext()) {

                displayView.append(cursor.getInt(cursor.getColumnIndex(StoreContract.BookEntry._ID)) + " "
                        + cursor.getString(cursor.getColumnIndex(StoreContract.BookEntry.COLUMN_PRODUCT_NAME)) + " $"
                        + cursor.getDouble(cursor.getColumnIndex(StoreContract.BookEntry.COLUMN_PRICE)) + " "
                        + cursor.getInt(cursor.getColumnIndex(StoreContract.BookEntry.COLUMN_QUANTITY)) + " "
                        + cursor.getString(cursor.getColumnIndex(StoreContract.BookEntry.COLUMN_SUPPLIER_NAME)) + " "
                        + cursor.getString(cursor.getColumnIndex(StoreContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER))
                        + "\n");
            }

        } finally {
            cursor.close();
        }
    }

    public void insertBook() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "ABC Book");
        values.put(BookEntry.COLUMN_PRICE, 9.99);
        values.put(BookEntry.COLUMN_QUANTITY, 100);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Supplier Name");
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "123 456 789");

        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);

        Log.v("CatalogActivity", "New row ID: " + newRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
