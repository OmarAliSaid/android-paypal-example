package com.omar.paypal.paymentservice.model;

import java.util.ArrayList;

/**
 * Created by Omar on 7/9/2017.
 */

public class ProductsResponse {

    ArrayList<Product>products = new ArrayList<>();
    int status;

    public ArrayList<Product> getProducts() {
        return products;
    }

    public int getStatus() {
        return status;
    }
}
