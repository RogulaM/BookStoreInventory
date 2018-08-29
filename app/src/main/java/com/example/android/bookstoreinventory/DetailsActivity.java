package com.example.android.bookstoreinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreinventory.data.ProductContract.ProductEntry;

public class DetailsActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri mCurrentProductUri;
    private TextView mProductNameTextView;
    private TextView mProductPriceTextView;
    private TextView mProductQuantityTextView;
    private TextView mProductSupplierNameTextView;
    private TextView mProductSupplierPhoneNumberTextView;
    int productQuantity;

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
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        setTitle(getString(R.string.details_activity_title));

        // Initialize a loader to read the data from the database and display the current values
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mProductNameTextView = findViewById(R.id.product_name_details);
        mProductPriceTextView = findViewById(R.id.product_price_details);
        mProductQuantityTextView = findViewById(R.id.product_quantity_details);
        mProductSupplierNameTextView = findViewById(R.id.product_supplier_name_details);
        mProductSupplierPhoneNumberTextView = findViewById(R.id.product_supplier_phone_number_details);
        Button productOrder = findViewById(R.id.order_button);
        Button increaseQuantity = findViewById(R.id.increase_quantity_button);
        Button decreaseQuantity = findViewById(R.id.decrease_quantity_button);

        // Set the order button to let the user contact the supplier via an intent to a phone app
        productOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mProductSupplierPhoneNumberTextView.getText()));
                if (callIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(callIntent);
                }
            }
        });

        // Change the quantity with the given buttons
        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productQuantity++;
                mProductQuantityTextView.setText(String.valueOf(productQuantity));
            }
        });

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantity > 0) {
                    productQuantity--;
                    mProductQuantityTextView.setText(String.valueOf(productQuantity));
                } else {
                    Toast.makeText(DetailsActivity.this, getString(R.string.quantity_below_zero_message),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        increaseQuantity.setOnTouchListener(mTouchListener);
        decreaseQuantity.setOnTouchListener(mTouchListener);
    }

    private void saveQuantityUpdate() {

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);

        int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

        // Show a toast message depending on whether or not the update was successful
        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.editor_update_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_update_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_editor options from the res/menu_editor/menu_editor.xmlile.
        // This adds menu_editor items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu_editor option in the app bar overflow menu_editor
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu_editor option
            case R.id.action_save:
                // Save product to database
                saveQuantityUpdate();
                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu_editor option
            case R.id.action_remove:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_edit:
                final Intent editorIntent = new Intent(DetailsActivity.this, EditorActivity.class);
                editorIntent.setData(mCurrentProductUri);
                if (!mProductHasChanged) {
                    startActivity(editorIntent);
                    return true;
                }

                // Oherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener1 =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to EditorActivity
                                startActivity(editorIntent);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener1);
                return true;

            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener2 =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener2);
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that contains all columns from the inventory table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
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
            // Find the columns of product attributes that we're interested in

            int productNameColumnIndex =
                    cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex =
                    cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex =
                    cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productSupplierNameColumnIndex =
                    cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int productSupplierPhoneNumberColumnIndex =
                    cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(productNameColumnIndex);
            double productPrice = cursor.getDouble(productPriceColumnIndex);
            productQuantity = cursor.getInt(productQuantityColumnIndex);
            String productSupplierName = cursor.getString(productSupplierNameColumnIndex);
            String productSupplierPhoneNumber = cursor.getString(productSupplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameTextView.setText(productName);
            mProductPriceTextView.setText(String.valueOf(productPrice));
            mProductQuantityTextView.setText(String.valueOf(productQuantity));
            mProductSupplierNameTextView.setText(productSupplierName);
            mProductSupplierPhoneNumberTextView.setText(productSupplierPhoneNumber);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameTextView.setText("");
        mProductPriceTextView.setText("");
        mProductQuantityTextView.setText("");
        mProductSupplierNameTextView.setText("");
        mProductSupplierPhoneNumberTextView.setText("");
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
        builder.setMessage(R.string.remove_dialog_msg);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {

        // Call the ContentResolver to delete the product at the given content URI.
        // Pass in null for the selection and selection args because the mCurrentProductUri
        // content URI already identifies the product that we want.
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_remove_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_remove_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
        // Close the activity
        finish();
    }
}