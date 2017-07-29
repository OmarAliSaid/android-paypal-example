package com.omar.paypal.paymentservice.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.omar.paypal.paymentservice.Config;
import com.omar.paypal.paymentservice.PayPalAPI;
import com.omar.paypal.paymentservice.R;
import com.omar.paypal.paymentservice.adapters.ShoppingCardRecycleAdapter;
import com.omar.paypal.paymentservice.model.PaymentVerificationResponse;
import com.omar.paypal.paymentservice.model.Product;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShoppingCardActivity extends AppCompatActivity implements ShoppingCardRecycleAdapter.ShoppingCardCallback{


    String TAG = this.getClass().getSimpleName();
    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     *
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     *
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static final int REQUEST_CODE_PAYMENT = 1;


    private static PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(Config.CONFIG_CLIENT_ID);

    float total =0;
    Boolean isEmptyCart = false;

    // To store the products which are added to cart
    private List<PayPalItem> payPalItems = new ArrayList<PayPalItem>();
    // Progress dialog
    private ProgressDialog pDialog;
    Retrofit retrofit;
    PayPalAPI service;


    @BindView(R.id.card_recycler_view)
    RecyclerView card_recycler_view;
    @BindView(R.id.txt_price)
    TextView totalPrice;
    @BindView(R.id.btn_buy)
    Button btn_buy;
    @BindView(R.id.iv_empty_cart)ImageView iv_empty_cart;
    @BindView(R.id.tv_empty_cart)TextView tv_empty_cart;
    @BindString(R.string.label_continue_shopping) String label_continue_shopping;

    ShoppingCardRecycleAdapter mCardRecycleAdapter;
    ArrayList<Product> mCardItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_card);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCardItems = new ArrayList<>();

        getExtras();

        checkEmptyCart();

        setupAdapter();

        build_API_Service();

        startPayPalService();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    /**
     * get extras from intent
     * get total price by adding the price of all shopping card products
     */
    public void getExtras() {
        if(getIntent().getExtras()!=null){
            mCardItems = getIntent().getParcelableArrayListExtra("order");
            for(int i=0;i<mCardItems.size();i++){
                total+=mCardItems.get(i).getPrice();
            }
            printTotalPrice(String.valueOf(total));
        }
    }

    /**
     * check if shopping cart is empty
     *
     * if it is empty then show the empty cart view
     */
    private void checkEmptyCart() {
        if(mCardItems.isEmpty()){
            isEmptyCart = true;
            tv_empty_cart.setVisibility(View.VISIBLE);
            iv_empty_cart.setVisibility(View.VISIBLE);
            totalPrice.setVisibility(View.INVISIBLE);
            btn_buy.setText(label_continue_shopping);
        }
    }


    private void setupAdapter() {
        mCardRecycleAdapter = new ShoppingCardRecycleAdapter(this,this, mCardItems);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        card_recycler_view.setLayoutManager(linearLayoutManager);
        card_recycler_view.setHasFixedSize(true);
        card_recycler_view.setNestedScrollingEnabled(false);
        card_recycler_view.setAdapter(mCardRecycleAdapter);
    }


    private void build_API_Service() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://frankz.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(PayPalAPI.class);
    }

    private void startPayPalService(){
        // Starting PayPal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);
    }


    /**
     * if shopping cart not empty then launch paypal payment
     * else
     * continue shopping
     */
    @OnClick(R.id.btn_buy)
    public void OnBuyClicked() {

        // Check for empty cart
        if (mCardItems.size() > 0) {
            // check if paypal Items list has products ?!
            // if so then clear it to avoid duplication
            if(!payPalItems.isEmpty())
                payPalItems.clear();

            // adding shopping cart items to paypal
            for(int i=0;i<mCardItems.size();i++){
                PayPalItem item = new PayPalItem(mCardItems.get(i).getName()
                        , mCardItems.get(i).getQuantity()
                        , BigDecimal.valueOf(mCardItems.get(i).getPrice())
                        , Config.DEFAULT_CURRENCY
                        , mCardItems.get(i).getSku());

                payPalItems.add(item);
            }

            launchPayPalPayment();

        }
        else {
            startActivity(new Intent(this,ProductsActivity.class));
        }
    }


    /**
     * Launching PalPay payment activity to complete the payment
     * */
    private void launchPayPalPayment() {

        PayPalPayment thingsToBuy = prepareFinalCart();

        Intent intent = new Intent(this, PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingsToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    /**
     * Preparing final cart amount that needs to be sent to PayPal for payment
     * */
    private PayPalPayment prepareFinalCart() {

        PayPalItem[] items = new PayPalItem[payPalItems.size()];
        items = payPalItems.toArray(items);

        // Total amount
        BigDecimal subtotal = PayPalItem.getItemTotal(items);

        // If you have shipping cost, add it here
        BigDecimal shipping = new BigDecimal("0.0");

        // If you have tax, add it here
        BigDecimal tax = new BigDecimal("0.0");

        PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(
                shipping, subtotal, tax);

        BigDecimal amount = subtotal.add(shipping).add(tax);

        PayPalPayment payment = new PayPalPayment(
                amount,
                Config.DEFAULT_CURRENCY,
                "we are happy to serve.",
                PayPalPayment.PAYMENT_INTENT_SALE);

        payment.items(items).paymentDetails(paymentDetails);

        // Custom field like invoice_number etc.,
        payment.custom("This is text that will be associated with the payment that the app can use.");

        return payment;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */

                        String payment_id = confirm.toJSONObject().getJSONObject("response").getString("id");

                        verifyPaymentOnServer(payment_id , confirm);

                        displayResultText("PaymentConfirmation info received from PayPal");

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    /**
     * Verifying the mobile payment on the server to avoid fraudulent payment
     * */
    private void verifyPaymentOnServer(final String paymentId , PaymentConfirmation confirmation ){
        // Showing progress dialog before making request
        pDialog.setMessage("Verifying payment...");
        showpDialog();

        try {
            String amount = confirmation.getPayment().toJSONObject().getString("amount");
            String currency = confirmation.getPayment().toJSONObject().getString("currency_code");
            String userID = "";
            if(FirebaseAuth.getInstance()!=null){
                userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            service.VerifyMobilePayment(paymentId ,userID, amount , currency ).enqueue(new Callback<PaymentVerificationResponse>() {
                @Override
                public void onResponse(Call<PaymentVerificationResponse> call, Response<PaymentVerificationResponse> response) {
                    // hiding the progress dialog
                    hidepDialog();
                    Log.d("msg" , response.body().getMsg());
                    Log.d("state" , response.body().getMsg());
                }

                @Override
                public void onFailure(Call<PaymentVerificationResponse> call, Throwable e) {
                    // hiding the progress dialog
                    hidepDialog();
                    Log.e(TAG , e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    protected void displayResultText(String result) {
        Toast.makeText(
                getApplicationContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }


    @Override
    public void OnIncreaseClicked(int position) {
        int quantity = mCardItems.get(position).getQuantity();
        mCardItems.get(position).setQuantity(++quantity);
        total+=mCardItems.get(position).getPrice();
        printTotalPrice(String.valueOf(total));
    }

    @Override
    public void OnDecreaseClicked(int position) {
        int quantity = mCardItems.get(position).getQuantity();
        if(quantity>1){
            mCardItems.get(position).setQuantity(--quantity);
            total-=mCardItems.get(position).getPrice();
            printTotalPrice(String.valueOf(total));
        }

    }

    @Override
    public void OnRemoveFromCartClicked(int position, View view) {
        Product productDetails = mCardItems.get(position);
        float price = (float)(productDetails.getPrice() * productDetails.getQuantity());
        total-=price;
        printTotalPrice(String.valueOf(total));
        mCardItems.remove(mCardItems.get(position));
        mCardRecycleAdapter.notifyDataSetChanged();
        checkEmptyCart();
    }

    void printTotalPrice(String price){
        totalPrice.setText("TOTAL     :   "+String.valueOf(price)+"   USD");
    }

}
