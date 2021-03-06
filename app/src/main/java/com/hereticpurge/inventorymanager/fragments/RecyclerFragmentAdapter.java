package com.hereticpurge.inventorymanager.fragments;

import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.hereticpurge.inventorymanager.R;
import com.hereticpurge.inventorymanager.model.ProductItem;
import com.hereticpurge.inventorymanager.model.ProductViewModel;
import com.hereticpurge.inventorymanager.utils.CurrencyUtils;
import com.hereticpurge.inventorymanager.utils.CustomImageUtils;
import com.hereticpurge.inventorymanager.utils.DebugAssistant;

import java.util.ArrayList;
import java.util.List;

public class RecyclerFragmentAdapter extends RecyclerView.Adapter<RecyclerFragmentAdapter.ViewHolder> {

    private List<ProductItem> mProductItemList;
    private List<ProductItem> mMasterList;
    private RecyclerCallback mCallback;
    private ProductViewModel mViewModel;

    RecyclerFragmentAdapter(RecyclerCallback callback, ProductViewModel viewModel) {
        this.mCallback = callback;
        this.mViewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_fragment_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        ProductItem productItem = mProductItemList.get(i);

        // Load the image for the given product item into the recycler list item.
        CustomImageUtils.loadImage(viewHolder.itemView.getContext(),
                String.valueOf(productItem.getId()),
                viewHolder.mImageView);

        viewHolder.mNameText.setText(productItem.getName());
        viewHolder.mCost.setText(CurrencyUtils.addLocalCurrencySymbol(productItem.getCost()));
        viewHolder.mRetail.setText(CurrencyUtils.addLocalCurrencySymbol(productItem.getRetail()));
        viewHolder.mCurrentStock.setText(String.valueOf(productItem.getCurrentStock()));

        viewHolder.mTrackingSwitch.setChecked(productItem.isTracked());

        String contentDesc = viewHolder.mTrackingSwitch.isChecked() ?
                viewHolder.itemView.getResources().getString(R.string.tracking_yes_with_switch) :
                viewHolder.itemView.getResources().getString(R.string.tracking_no_with_switch);

        viewHolder.mTrackingSwitch.setContentDescription(contentDesc);

        if (i == 0) {
            viewHolder.itemView.setNextFocusUpId(R.id.app_bar_layout);
        }

        viewHolder.mTrackingSwitch.setOnClickListener(v -> {
            // when the tracking status of an item is changed the product item is updated
            // and saved in the database.
            productItem.setTracked(!productItem.isTracked());
            mViewModel.addProduct(productItem);
        });

        // Callback on a selected item.
        viewHolder.itemView.setOnClickListener(view -> mCallback.onItemSelected(i));
    }

    @Override
    public int getItemCount() {
        return mProductItemList == null ? 0 : mProductItemList.size();
    }

    public void updateList(List<ProductItem> productItems) {
        // Helper method so that when room detects new data on the observed data set it can load
        // new information into the recycler view and tell the recycler to update its information.
        this.mMasterList = productItems;
        this.mProductItemList = productItems;
        this.notifyDataSetChanged();
    }

    public void filterProducts(String query) {
        List<ProductItem> filteredList = new ArrayList<>();

        // Saddest search functionality ever.  UPDATE ME!!
        if (query != null && mMasterList != null) {
            for (ProductItem productItem : mMasterList) {
                for (String productTerm : productItem.getSearchTerms()) {
                    if (productTerm.equalsIgnoreCase(query) && !filteredList.contains(productItem)) {
                        filteredList.add(productItem);
                    }
                }
            }
        } else {
            filteredList = mMasterList;
        }
        this.mProductItemList = filteredList;
        this.notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;

        TextView mNameText;
        TextView mCost;
        TextView mRetail;
        TextView mCurrentStock;

        Switch mTrackingSwitch;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.recycler_item_image_view);

            mNameText = itemView.findViewById(R.id.recycler_item_product_name);
            mCost = itemView.findViewById(R.id.recycler_item_cost);
            mRetail = itemView.findViewById(R.id.recycler_item_retail);
            mCurrentStock = itemView.findViewById(R.id.recycler_item_current_stock);

            mTrackingSwitch = itemView.findViewById(R.id.recycler_item_tracking_switch);
        }
    }

    public interface RecyclerCallback {
        void onItemSelected(int id);
    }
}
