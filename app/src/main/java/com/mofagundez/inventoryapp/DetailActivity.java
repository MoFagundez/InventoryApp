package com.mofagundez.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mofagundez.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;

/**
 * Created by Mauricio on June 20, 2017
 * <p>
 * Udacity Android Basics Nanodegree
 * Project 10: Inventory App
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter product name */
    private EditText mNameEditText;

    /** EditText field to enter product large quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter product price */
    private EditText mPriceEditText;

    /** TextView to show current product quantity */
    private TextView mQuantityTextView;

    /** Product information variables */
    private String mProductName;
    private int mProductQuantity;

    /** Four Buttons that will be used to modify quantity */
    private Button mIncreaseQuantityByOneButton;    // Increase by one
    private Button mDecreaseQuantityByOneButton;    // Decrease by one
    private Button mIncreaseQuantityLargeButton;    // Increase by many (n)
    private Button mDecreaseQuantityLargeButton;    // Decrease by many (n)

    /** Final for the image intent request code */
    private final static int SELECT_PHOTO = 200;

    /** Constant to be used when asking for storage read */
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 666;    // Yes, 666! Took me a while to figure this beast!

    /** Button to select image, ImageView to display selected image
     * and Bitmap to store/retrieve from the database */
    private Button mSelectImageButton;
    private ImageView mProductImageView;
    private Bitmap mProductBitmap;

    /** Button to order more quantity from supplier */
    private Button mOrderButton;

    /** Constant field for email intent */
    private static final String URI_EMAIL = "mailto:";

    /** Uri loader */
    private static final int URI_LOADER = 0;

    /** Uri received with the Intent from {@link MainActivity} */
    private Uri mProductUri;

    /** Boolean to check whether or not the register has changed */
    private boolean mProductHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Receive Uri data from intent
        Intent intent = getIntent();
        mProductUri = intent.getData();

        // Check if Uri is null or not
        if (mProductUri != null) {
            // If not null means that a pet register will be edited
            setTitle(R.string.activity_detail_edit);
            // Kick off LoaderManager
            getLoaderManager().initLoader(URI_LOADER, null, this);
        } else {
            // If null means that a new pet register will be created
            setTitle(R.string.activity_detail_new);
            // Invalidate options menu (delete button) since there's no record
            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read or show user input
        initialiseViews();

        // Set on touch listener to all relevant views
        setOnTouchListener();
    }

    private void initialiseViews() {
        // Check if it's an existing product to make the btton visible so
        // the user can order more from existing product
        if (mProductUri != null) {
            // Initialise Button to order more from supplier
            mOrderButton = (Button) findViewById(R.id.button_order_from_supplier);
            // Make button visible
            mOrderButton.setVisibility(View.VISIBLE);
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // I can't assess whether or not this one works since my emulator does not
                    // run email intent. I am sorry!
                    // TODO: Test this email intent properly :(
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.setData(Uri.parse(URI_EMAIL));
                    // Defining supplier's email. Ideally it would come from the product database in a real world
                    // application but I am using a string to make it simple for this exercise
                    intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.supplier_email));
                    intent.putExtra(Intent.EXTRA_SUBJECT, mProductName);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }

        // Initialise EditTexts
        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);

        // Initialise TextView
        mQuantityTextView = (TextView) findViewById(R.id.text_view_quantity_final);

        // Initialise increase Button and set click listener
        mIncreaseQuantityByOneButton = (Button) findViewById(R.id.button_increase_one);
        mIncreaseQuantityByOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add +1 to product quantity
                mProductQuantity++;
                // Update UI
                mQuantityTextView.setText(String.valueOf(mProductQuantity));
            }
        });

        // Initialise decrease Button and set click listener
        mDecreaseQuantityByOneButton = (Button) findViewById(R.id.button_decrease_one);
        mDecreaseQuantityByOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Decrease 1 to product quantity if higher than 0
                if (mProductQuantity > 0) {
                    mProductQuantity--;
                    // Update UI
                    mQuantityTextView.setText(String.valueOf(mProductQuantity));
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.toast_invalid_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialise increase large quantity Button and set click listener
        mIncreaseQuantityLargeButton = (Button) findViewById(R.id.button_increase_n);
        mIncreaseQuantityLargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity edit text is empty and higher than zero
                if (!TextUtils.isEmpty(mQuantityEditText.getText()) && Integer.valueOf(mQuantityEditText.getText().toString()) > 0) {
                    // Add the quantity in the edit text to the variable keeping track of product stock quantity
                    mProductQuantity += Integer.valueOf(mQuantityEditText.getText().toString());
                    // Update the UI
                    mQuantityTextView.setText(String.valueOf(mProductQuantity));
                } else {
                    // Show toast asking user to fill out edit text
                    Toast.makeText(DetailActivity.this, getString(R.string.toast_missing_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialise decrease large quantity Button and set click listener
        mDecreaseQuantityLargeButton = (Button) findViewById(R.id.button_decrease_n);
        mDecreaseQuantityLargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity edit text is empty and higher than zero
                if (!TextUtils.isEmpty(mQuantityEditText.getText()) && Integer.valueOf(mQuantityEditText.getText().toString()) > 0) {
                    int newQuantity = mProductQuantity - Integer.valueOf(mQuantityEditText.getText().toString());
                    if (newQuantity < 0) {
                        Toast.makeText(DetailActivity.this, getString(R.string.toast_invalid_quantity), Toast.LENGTH_SHORT).show();
                    } else {
                        // Decrease the quantity in the edit text to the variable keeping track of product stock quantity
                        mProductQuantity -= Integer.valueOf(mQuantityEditText.getText().toString());
                        // Update the UI
                        mQuantityTextView.setText(String.valueOf(mProductQuantity));
                    }
                } else {
                    // Show toast asking user to fill out edit text
                    Toast.makeText(DetailActivity.this, getString(R.string.toast_missing_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialise the image view to show preview of the product image
        mProductImageView = (ImageView) findViewById(R.id.image);

        // Initialise button to select the product image
        mSelectImageButton = (Button) findViewById(R.id.button_select_image);
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ask for user permission to explore image gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // If not authorized, ask for authorization
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // Do something
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                        return;
                    }
                    // If permission granted, create a new intent
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    // Set intent type as IMAGE
                    intent.setType("image/*");
                    // Launch image chooser
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
                }
            }
        });
    }

    /** Handle the result of the image chooser intent launch */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if request code, result and intent match the image chooser
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            // Get image Uri
            Uri selectedImage = data.getData();
            // Get image file path
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            // Create cursor object and query image
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // Get image path from cursor
            String picturePath = cursor.getString(columnIndex);
            // Close cursor to avoid memory leak
            cursor.close();
            // Set the image to a Bitmap object
            mProductBitmap = BitmapFactory.decodeFile(picturePath);
            // Set Bitmap to the image view
            mProductImageView.setImageBitmap(mProductBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main_activity.xml file.
        // This adds the given menu to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Add" menu option
            case R.id.action_add:
                if (mProductHasChanged) {
                    // Call save/edit method
                    saveProduct();
                } else {
                    // Show toast when no product is updated nor created
                    Toast.makeText(this, getString(R.string.toast_insert_or_update_product_failed), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_delete:
                // Call delete confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If product hasn't changed, continue with navigating up to parent activity
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                } else {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity
                                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle the back button press on the device
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with closing and back to parent activity
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Close the current activity without adding/saving
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Add new product or commit changes to existing register being edited
     */
    private void saveProduct() {
        // Define whether or not EditText fields are empty
        boolean nameIsEmpty = checkFieldEmpty(mNameEditText.getText().toString().trim());
        boolean priceIsEmpty = checkFieldEmpty(mPriceEditText.getText().toString().trim());

        // Check if Name, Quantity or Price are null/zero and inform the user to change to a valid value
        if (nameIsEmpty) {
            Toast.makeText(this, getString(R.string.toast_invalid_name_add), Toast.LENGTH_SHORT).show();
        } else if (mProductQuantity <= 0) {
            Toast.makeText(this, getString(R.string.toast_invalid_quantity_add), Toast.LENGTH_SHORT).show();
        } else if (priceIsEmpty) {
            Toast.makeText(this, getString(R.string.toast_invalid_price_add), Toast.LENGTH_SHORT).show();
        } else {
            // Assuming that all fields are valid, pass the edit text
            // value to a String for easier manipulation
            String name = mNameEditText.getText().toString().trim();
            // Pass the edit text value to a double for easier manipulation
            double price = Double.parseDouble(mPriceEditText.getText().toString().trim());

            // Create new ContentValues and put the product info into it
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_NAME, name);
            values.put(ProductEntry.COLUMN_QUANTITY, mProductQuantity);
            values.put(ProductEntry.COLUMN_PRICE, price);

            // Check if user wants to add a picture to the current product.
            // I made the decision of allowing this field to be null since it won't
            // always have an image to add
            if (mProductBitmap != null) {
                // If an image was picked, transform to byte array before put into ContentValues
                byte[] image = getBytes(mProductBitmap);
                values.put(ProductEntry.COLUMN_IMAGE, image);
            }

            // Check if Uri is valid to determine whether is new product insertion or existing product update
            if (mProductUri == null) {
                // If Uri is null then we're inserting a new product
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                // Inform user of the successful product insertion
                Toast.makeText(this, getString(R.string.toast_insert_product_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If Uri is not null then we're updating an existing product
                int newUri = getContentResolver().update(mProductUri, values, null, null);
                // Inform user of the successful product update
                Toast.makeText(this, getString(R.string.toast_update_product_success),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    /**
     * Convert from bitmap to byte array
     *
     * @param bitmap: Data retrieved from the user galery that will be
     *              converted to byte[] in order to store in database BLOB
     */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /**
     * Convert from byte array to bitmap
     *
     * @param image: BLOB from the database converted to a Bitmap
     *             in order to display in the UI
     */
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * Method to define if any of the EditText fields are empty or contain invalid inputs
     *
     * @param string: String received as a parameter to be checked with this method
     */
    private boolean checkFieldEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals(".");
    }

    /**
     * Perform the deletion of the product record in the database.
     */
    private void deleteProduct() {
        if (mProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.toast_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_delete_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Ask for user confirmation before deleting product from database
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.prompt_delete_product));
        builder.setPositiveButton(getString(R.string.prompt_delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Call deleteProduct method, so delete the product register from database.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.prompt_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dismiss the dialog and continue editing the product record.
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
     * Ask for user confirmation to exit activity before saving
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.prompt_leave_no_save));
        builder.setPositiveButton(getString(R.string.prompt_yes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.prompt_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product register
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
     * Set touch listeners to the UI
     */
    private void setOnTouchListener() {
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mIncreaseQuantityByOneButton.setOnTouchListener(mTouchListener);
        mDecreaseQuantityByOneButton.setOnTouchListener(mTouchListener);
        mIncreaseQuantityLargeButton.setOnTouchListener(mTouchListener);
        mDecreaseQuantityLargeButton.setOnTouchListener(mTouchListener);
        mSelectImageButton.setOnTouchListener(mTouchListener);
    }

    /**
     * Set onTouchListener on the UI and changes the boolean value to TRUE in order to indicate
     * that the user is changing the current product register
     */
    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URI_LOADER:
                return new CursorLoader(
                        this,
                        mProductUri,
                        null,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mProductName = data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME));
            mNameEditText.setText(mProductName);
            // Price wasn't formatted to currency on purpose
            mPriceEditText.setText(data.getString(data.getColumnIndex(ProductEntry.COLUMN_PRICE)));
            mProductQuantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
            mQuantityTextView.setText(String.valueOf(mProductQuantity));
            // If an image is available, the method getImage will retrieve as a byte array
            // and transform into a Bitmap so it can be shown in the UI.
            // Again, I decided to allow the user not to add image to the product.
            if (data.getBlob(data.getColumnIndex(ProductEntry.COLUMN_IMAGE)) != null) {
                mProductImageView.setImageBitmap(getImage(data.getBlob(data.getColumnIndex(ProductEntry.COLUMN_IMAGE))));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.getText().clear();
        mQuantityEditText.getText().clear();
        mQuantityTextView.setText("");
    }
}
