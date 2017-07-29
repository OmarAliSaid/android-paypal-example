package com.omar.paypal.paymentservice.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.omar.paypal.paymentservice.PayPalAPI;
import com.omar.paypal.paymentservice.R;
import com.omar.paypal.paymentservice.adapters.ProductListAdapter;
import com.omar.paypal.paymentservice.library.ActionItemBadge;
import com.omar.paypal.paymentservice.model.Product;
import com.omar.paypal.paymentservice.model.ProductsResponse;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductsActivity extends AppCompatActivity implements ProductListAdapter.AddToCartCallback{

    String TAG = this.getClass().getSimpleName();
    int badgeCount = 0;
    MenuItem mItem;

    @BindView(R.id.products_recycle_view)RecyclerView rv_products;
    @BindString(R.string.local_host)String local_host;

    // Progress dialog
    private ProgressDialog pDialog;
    ArrayList<Product>cardItems = new ArrayList<>();
    Retrofit retrofit;
    PayPalAPI service;
    Boolean logout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        ButterKnife.bind(this);

        setupRecycleView();

        build_API_Service();

        buildProgressDialog();

        getProducts();

    }

    /**
     * initial setup for the recycle view
     */
    private void setupRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv_products.setLayoutManager(linearLayoutManager);
        rv_products.setHasFixedSize(true);

    }

    /**
     * build retrofit service
     */
    private void build_API_Service() {
       retrofit = new Retrofit.Builder()
                .baseUrl(local_host)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

       service = retrofit.create(PayPalAPI.class);
    }


    /**
     * GET request to : https://frankz.herokuapp.com/api/products
     * to retrieve the products stored in firebase
     *
     * OnSuccess -> load the products in the RecycleView adapter
     * OnFail    -> log error message
     */
    private void getProducts(){
        showpDialog();
        service.getProducts().enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                ProductListAdapter adapter = new ProductListAdapter(ProductsActivity.this
                        ,ProductsActivity.this
                        ,response.body().getProducts());

                rv_products.setAdapter(adapter);
                hidepDialog();
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                Log.e(TAG , t.getMessage());
                hidepDialog();
            }
        });
    }

    private void buildProgressDialog(){
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("loading ...");
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    @Override
    public void OnAddToCartClicked(Product product) {
        if(alreadyInShoppingCart(product.getSku()))
            Toast.makeText(getApplicationContext(),
                    " item already in cart!", Toast.LENGTH_SHORT).show();
        else {
            badgeCount++;
            ActionItemBadge.update(mItem, badgeCount);
            cardItems.add(product);
            Toast.makeText(getApplicationContext(),
                    product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean alreadyInShoppingCart(String sku){
        for (int i=0;i<cardItems.size();i++){
            if(cardItems.get(i).getSku().equals(sku))
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mItem = menu.findItem(R.id.item_samplebadge);

        // hide it if the badgeCount == 0
        ActionItemBadge.update(this, mItem
                    , FontAwesome.Icon.faw_shopping_cart
                    , ActionItemBadge.BadgeStyles.DARK_GREY
                    , badgeCount);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

       if (id == R.id.action_contact_us) {
            contactUs();
            return true;
        }else if (id == R.id.action_logout) {
            //facebook logout
            LoginManager.getInstance().logOut();
            //firebase logout
            FirebaseAuth.getInstance().signOut();
            logout = true;
            startActivity(new Intent(this,LoginActivity.class));
            return true;
        }else if (id == R.id.item_samplebadge) {
            Intent intent = new Intent(this,ShoppingCardActivity.class);
            intent.putParcelableArrayListExtra("order",cardItems);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(logout)
            finish();
    }

    private void contactUs(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","frankzsupplier@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

}
