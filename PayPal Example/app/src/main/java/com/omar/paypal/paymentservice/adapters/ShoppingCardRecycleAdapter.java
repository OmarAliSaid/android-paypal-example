package com.omar.paypal.paymentservice.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.omar.paypal.paymentservice.R;
import com.omar.paypal.paymentservice.model.CardItem;
import com.omar.paypal.paymentservice.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Omar on 7/10/2017.
 */

public class ShoppingCardRecycleAdapter extends RecyclerView.Adapter<ShoppingCardRecycleAdapter.MyViewHolder> {

    Context context;
    ArrayList<Product> cardItems = new ArrayList<>();
    ShoppingCardCallback mShoppingCardCallback;

    public ShoppingCardRecycleAdapter(Context context, ShoppingCardCallback mShoppingCardCallback ,ArrayList<Product> cardItems) {
        this.context = context;
        this.mShoppingCardCallback = mShoppingCardCallback;
        this.cardItems = cardItems;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_card_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Product item = cardItems.get(position);
        holder.name.setText(item.getName());
        holder.price.setText(String.valueOf(item.getPrice())+"  USD");
        Picasso.with(context).load(item.getImage()).into(holder.image);
        if(holder.quantity.getText()==null)
            holder.quantity.setText("0");

        holder.decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(holder.quantity.getText().toString());
                if(quantity>1)
                    holder.quantity.setText(String.valueOf( (quantity-1) ));

                // send callback to notify decrease
                mShoppingCardCallback.OnDecreaseClicked(position);
            }
        });

        holder.increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(holder.quantity.getText().toString());
                holder.quantity.setText(String.valueOf( (quantity+1) ));

                // send callback to notify increase
                mShoppingCardCallback.OnIncreaseClicked(position);

            }
        });

        holder.remove_from_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShoppingCardCallback.OnRemoveFromCartClicked(position , holder.shopping_card);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.product_name)TextView name;
        @BindView(R.id.product_price)TextView price;
        @BindView(R.id.product_image)ImageView image;
        @BindView(R.id.shopping_card)CardView shopping_card;
        @BindView(R.id.item_quantity)TextView quantity;
        @BindView(R.id.remove_from_cart)ImageView remove_from_cart;
        @BindView(R.id.increase)ImageView increase;
        @BindView(R.id.decrease)ImageView decrease;

        public MyViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public interface ShoppingCardCallback{
        void OnIncreaseClicked(int position);
        void OnDecreaseClicked(int position);
        void OnRemoveFromCartClicked(int position , View view);
    }
}