package com.example.paymentdemo;

import com.example.paymentdemo.Data;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;


public interface API {
    String DOMAIN = "https://uatgw.nasswallet.com";
    String BASE_URL = DOMAIN + "/payment/transaction/";
    String PAYMENT_GATEWAY_URL = "https://uatcheckout.nasswallet.com/payment-gateway";


    @POST("login")
    Call<ResponseBody> getMerchantToken(@Header("authorization") String basicToken, @Body() Data body);
    @POST("initTransaction")
    Call<ResponseBody> initTransaction(@Header("authorization") String bearerToken, @Body() Data body);

}
