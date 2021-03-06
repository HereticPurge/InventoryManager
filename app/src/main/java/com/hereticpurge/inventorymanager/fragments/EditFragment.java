package com.hereticpurge.inventorymanager.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hereticpurge.inventorymanager.AnalyticsApplication;
import com.hereticpurge.inventorymanager.MainActivity;
import com.hereticpurge.inventorymanager.R;
import com.hereticpurge.inventorymanager.model.ProductItem;
import com.hereticpurge.inventorymanager.model.ProductViewModel;
import com.hereticpurge.inventorymanager.utils.AppbarStateChangeListener;
import com.hereticpurge.inventorymanager.utils.BarcodeReader;
import com.hereticpurge.inventorymanager.utils.CurrencyUtils;
import com.hereticpurge.inventorymanager.utils.CustomImageUtils;

public class EditFragment extends Fragment {

    public static final String TAG = "EditFragment";
    private static final String PRODUCT_KEY = "productKey";
    private static final String TEMP_IMAGE_FILENAME = "tempImage";

    private ProductViewModel mViewModel;

    private ProductItem mProductItem;

    private ImageView mMainImageView;

    private Bitmap mTempImage;

    private EditText mName;
    private EditText mBarcode;
    private EditText mCustomId;
    private EditText mCost;
    private EditText mRetail;
    private EditText mCurrentStock;
    private EditText mTargetStock;

    private Switch mTrackSwitch;

    private Tracker mTracker;

    private boolean isNewItem;

    private static final int MAIN_IMAGE_RESULT = 200;
    private static final int BARCODE_RESULT = 201;

    public static EditFragment createInstance(ProductItem productItem, boolean newItem) {
        // Static creation method returns a new instance of this class with variables set.
        EditFragment editFragment = new EditFragment();
        editFragment.mProductItem = productItem;
        editFragment.isNewItem = newItem;
        return editFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_fragment_layout, container, false);

        try {
            // Reload the display product if it exists.
            mProductItem = savedInstanceState.getParcelable(PRODUCT_KEY);
        } catch (NullPointerException npe) {
            // Couldn't reload the object.  Load as normal.
        }

        if (!MainActivity.isTablet) {
            // Setup for the app bar if we're not in tablet or landscape mode.
            initAppBar(view);
        }

        FloatingActionButton mFloatingActionButton = view.findViewById(R.id.main_fab);
        mFloatingActionButton.setOnClickListener(v -> doSave());

        ImageButton mMainImageButton = view.findViewById(R.id.edit_product_main_image_camera_button);
        mMainImageButton.setOnClickListener(v -> startCameraForResult(MAIN_IMAGE_RESULT));

        ImageButton mBarcodeImageButton = view.findViewById(R.id.edit_product_barcode_camera_button);
        mBarcodeImageButton.setOnClickListener(v -> startCameraForResult(BARCODE_RESULT));

        Button mDeleteButton = view.findViewById(R.id.edit_delete_button);
        mDeleteButton.setOnClickListener(v -> doDelete());

        mMainImageView = view.findViewById(R.id.edit_product_image_iv);

        mName = view.findViewById(R.id.edit_product_name_et);
        mBarcode = view.findViewById(R.id.edit_product_barcode_et);
        mCustomId = view.findViewById(R.id.edit_product_custom_id_et);
        mCost = view.findViewById(R.id.edit_fragment_cost_et);
        mRetail = view.findViewById(R.id.edit_fragment_retail_et);
        mCurrentStock = view.findViewById(R.id.edit_fragment_stock_current_et);
        mTargetStock = view.findViewById(R.id.edit_fragment_stock_target_et);

        mTrackSwitch = view.findViewById(R.id.edit_fragment_track_switch_button);

        if (!isNewItem) {
            initProductFields();
        }

        if (getActivity() != null) {
            // Google Analytics tracker.
            mTracker = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        }

        return view;
    }

    private void initAppBar(View view) {
        android.support.v7.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!MainActivity.isLandscape) {
            AppBarLayout appBarLayout = view.findViewById(R.id.app_bar_layout);
            appBarLayout.addOnOffsetChangedListener(new AppbarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    TextView textView = view.findViewById(R.id.toolbar_text_view);
                    if (state == State.COLLAPSED) {
                        textView.setVisibility(View.VISIBLE);
                    } else if (state == State.EXPANDED) {
                        textView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mViewModel = ViewModelProviders
                .of(this)
                .get(ProductViewModel.class);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(PRODUCT_KEY, mProductItem);
        super.onSaveInstanceState(outState);
    }

    private void initProductFields() {
        if (mProductItem != null) {
            mName.setText(mProductItem.getName());
            mBarcode.setText(mProductItem.getBarcode());
            mCustomId.setText(mProductItem.getCustomId());
            mCost.setText(CurrencyUtils.addLocalCurrencySymbol(mProductItem.getCost()));
            mRetail.setText(CurrencyUtils.addLocalCurrencySymbol(mProductItem.getRetail()));
            mCurrentStock.setText(String.valueOf(mProductItem.getCurrentStock()));
            mTargetStock.setText(String.valueOf(mProductItem.getTargetStock()));
            mTrackSwitch.setChecked(mProductItem.isTracked());
            if (mTempImage == null) {
                CustomImageUtils.loadImage(getContext(), String.valueOf(mProductItem.getId()), mMainImageView);
            }
        }
    }

    private void startCameraForResult(int requestCode) {
        // Just what the method sais.  Started the camera to take a picture and return a broadcast
        // containing the picture.  Set the request code from the above for different picture types.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(getContext(),
                    R.string.error_no_camera_app,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Receive the camera broadcast and determine what to do with the result based on request
        // code.
        Object dataObject = null;

        if (data != null && data.hasExtra("data") && data.getExtras().containsKey("data")) {
            dataObject = data.getExtras().get("data");
        }

        switch (requestCode) {

            case (MAIN_IMAGE_RESULT):
                // Set the image of the product to the new main image.
                if (dataObject instanceof Bitmap) {
                    mTempImage = (Bitmap) dataObject;
                    mMainImageView.setImageBitmap(mTempImage);
                    CustomImageUtils.saveImage(getContext(), mTempImage, String.valueOf(mProductItem.getId()));
                    break;
                }

            case (BARCODE_RESULT):
                // Decode the barcode of the returned image and set it in the product.
                if (dataObject instanceof Bitmap) {
                    String barcode = BarcodeReader.getBarcode(getContext(), (Bitmap) dataObject);
                    mBarcode.setText(barcode);
                    break;
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void doSave() {
        // As advertised.  Save the current product item to the room database.

        if (mName.getText().equals("") || mName.getText().equals(" ")) {
            Toast.makeText(getContext(), R.string.error_product_name, Toast.LENGTH_LONG).show();
        }

        try {
            // Long list of if statements just checking to see if the user actually input any data
            // if they didn't input data the .toStrings should be equal to "" and we just leave
            // the product item defaults in place for the save.
            String checkStringInput = "";

            if (!mName.getText().toString().equals(checkStringInput)) {
                mProductItem.setName(mName.getText().toString());
            }

            if (!mBarcode.getText().toString().equals(checkStringInput)) {
                mProductItem.setBarcode(mBarcode.getText().toString());
            }

            if (!mCustomId.getText().toString().equals(checkStringInput)) {
                mProductItem.setCustomId(mCustomId.getText().toString());
            }

            if (!mCost.getText().toString().equals(checkStringInput)) {
                mProductItem.setCost(CurrencyUtils.removeLocalCurrencySymbol(mCost.getText().toString()));
            }

            if (!mRetail.getText().toString().equals(checkStringInput)) {
                mProductItem.setRetail(CurrencyUtils.removeLocalCurrencySymbol(mRetail.getText().toString()));
            }

            if (!mCurrentStock.getText().toString().equals(checkStringInput)) {
                mProductItem.setCurrentStock(Integer.parseInt(mCurrentStock.getText().toString()));
            }

            if (!mTargetStock.getText().toString().equals(checkStringInput)) {
                mProductItem.setTargetStock(Integer.parseInt(mTargetStock.getText().toString()));
            }

            mProductItem.setTracked(mTrackSwitch.isChecked());


            if (mTempImage != null) {
                CustomImageUtils.saveImage(getContext(), mTempImage, String.valueOf(mProductItem.getId()));
            }

            mViewModel.addProduct(mProductItem);

            try {
                // Hide the soft keyboard when we save so it's not visible after the back stack is popped
                // and the user is sent back to the detail page.
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            } catch (NullPointerException e) {
                Log.e(TAG, "doSave: Error Removing Soft Keyboard");
            }

            popParentBackStack();
        } catch (NumberFormatException nfe) {
            // User entered invalid input in one of the fields.
            Toast.makeText(getContext(), R.string.error_number_error, Toast.LENGTH_LONG).show();
        }
    }

    private void popParentBackStack() {
        // helper method to push the app backwards one fragment view.
        try {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        } catch (NullPointerException npe) {
            Log.e(TAG, "popParentBackStack: Failed to get support fragment manager");
        }
    }

    private void doDelete() {
        // Delete the currently visible product item from the database.

        ConfirmDialog.ConfirmDialogCallback confirmDialogCallback = new ConfirmDialog.ConfirmDialogCallback() {
            @Override
            public void onConfirm() {
                if (getActivity() != null) {
                    CustomImageUtils.deleteImage(getActivity(), String.valueOf(mProductItem.getId()));
                    mViewModel.deleteSingleProduct(mProductItem);
                    // popping the backstack twice to clear out remnant fragments associated with the deleted
                    // product item.
                    popParentBackStack();
                    popParentBackStack();
                }
            }

            @Override
            public void onCancel() {
                // Do nothing.
            }
        };

        ConfirmDialog.createDialog(confirmDialogCallback,
                R.string.dialog_delete,
                R.string.dialog_yes,
                R.string.dialog_no)
                .show(getChildFragmentManager(), null);
    }

    public void confirmNavigateAwaySave() {
        // Called when the Main Activity detects the user attempting to leave an edit fragment by
        // some means other than the save button.
        ConfirmDialog.ConfirmDialogCallback confirmDialogCallback = new ConfirmDialog.ConfirmDialogCallback() {
            @Override
            public void onConfirm() {
                doSave();
            }

            @Override
            public void onCancel() {
                popParentBackStack();
            }
        };
        ConfirmDialog confirmDialog = ConfirmDialog.createDialog(confirmDialogCallback,
                R.string.dialog_save,
                R.string.dialog_yes,
                R.string.dialog_no);
        confirmDialog.show(getChildFragmentManager(), null);
    }
}
