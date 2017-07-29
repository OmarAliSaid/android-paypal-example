package com.omar.paypal.paymentservice.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.omar.paypal.paymentservice.R;
import com.omar.paypal.paymentservice.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Omar on 7/8/2017.
 */

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.MyViewHolder> {

    ArrayList<Product> mProducts = new ArrayList<>();
    Context ctx;
    AddToCartCallback callback;
    public ProductListAdapter(Context ctx,AddToCartCallback mCallback ,ArrayList<Product> mProducts) {
        this.mProducts = mProducts;
        this.ctx = ctx;
        this.callback = mCallback;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_product,
                viewGroup, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, int i) {
        myViewHolder.productName.setText(mProducts.get(i).getName());
        myViewHolder.productDescription.setText(mProducts.get(i).getDescription());
        myViewHolder.productPrice.setText(String.valueOf(mProducts.get(i).getPrice())+" USD");
        Picasso.with(ctx).load(mProducts.get(i).getImage()).into(myViewHolder.productImage);
        myViewHolder.AddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.OnAddToCartClicked(mProducts.get(myViewHolder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.card_product_item)
        CardView cv;
        @Nullable
        @BindView(R.id.productName)
        TextView productName;
        @Nullable
        @BindView(R.id.productDescription)
        TextView productDescription;
        @Nullable
        @BindView(R.id.productPrice)
        TextView productPrice;
        @Nullable
        @BindView(R.id.productImage)
        ImageView productImage;
        @Nullable
        @BindView(R.id.btnAddToCart)
        Button AddToCart;


        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface AddToCartCallback{
        void OnAddToCartClicked(Product product);
    }
}