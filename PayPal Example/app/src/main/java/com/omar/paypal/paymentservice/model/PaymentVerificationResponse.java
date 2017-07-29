package com.omar.paypal.paymentservice.model;

/**
 * Created by Omar on 7/7/2017.
 */

public class PaymentVerificationResponse {
    String msg;
    int status;

    public PaymentVerificationResponse(String msg, int status) {
        this.msg = msg;
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public int getStatus() {
        return status;
    }
}
