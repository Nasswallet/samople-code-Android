package com.example.paymentdemo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentGatewayActivity extends AppCompatActivity {
    WebView webView;
    ProgressDialog mDialog;
    String username = "";
    String password = "";
    String grantType = "";
    String transactionPin  = "";
    String orderId = "";
    String amount = "";
    String languageCode = "en";
    String basicToken = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_gateway);
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        // enable Web Storage: localStorage, sessionStorage
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        showProgressDialog();
        getMerchantToken();
    }

    private void getMerchantToken() {
        Data data = new Data();
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        loginRequest.setGrantType(grantType);
        data.setData(loginRequest);
        API api = RetrofitClient.getClient().create(API.class);
        Call<ResponseBody> call = api.getMerchantToken(basicToken, data);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                //http status
                System.out.println(response.code());
                if (response.code() == 200 && response.body() != null) {
                    //http status success
                    try {
                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        if (jsonObject.getInt("responseCode") == 0) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            String access_token = data.getString("access_token");
                            initTransaction(access_token);
                        } else {
                            // handle other response codes
                            String message = jsonObject.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            dismissProgressDialog();
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to get token", Toast.LENGTH_LONG).show();
                        dismissProgressDialog();
                        finish();
                    }
                } else {
                    //http status failure
                    Toast.makeText(getApplicationContext(), "Failed to get token", Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to get token", Toast.LENGTH_LONG).show();
                dismissProgressDialog();
                finish();
            }
        });
    }


    private void initTransaction(String acessToken) {
        String bearerToken = "Bearer " + acessToken;
        Data data = new Data();
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setUserIdentifier(username);
        transactionRequest.setTransactionPin(transactionPin);
        transactionRequest.setOrderId(orderId);
        transactionRequest.setAmount(amount);
        transactionRequest.setLanguageCode(languageCode);
        data.setData(transactionRequest);
        API api = RetrofitClient.getClient().create(API.class);
        Call<ResponseBody> call = api.initTransaction(bearerToken, data);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                //http status
                if (response.code() == 200 && response.body() != null) {
                    //http status success
                    try {
                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        if (jsonObject.getInt("responseCode") == 0) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            String transactionId = data.getString("transactionId");
                            String token = data.getString("token");
                            String url = API.PAYMENT_GATEWAY_URL + "?id=" + transactionId + "&token=" + token + "&userIdentifier=" + username;
                            Log.d("PAYMENT_GATEWAY_URL", url);
                            webView.loadUrl(url);
                        } else {
                            // handle other response codes
                            String message = jsonObject.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            dismissProgressDialog();
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to init transaction", Toast.LENGTH_LONG).show();
                        dismissProgressDialog();
                        finish();
                    }
                } else {
                    //http status failure
                    Toast.makeText(getApplicationContext(), "Failed to init transaction", Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to init transaction", Toast.LENGTH_LONG).show();
                dismissProgressDialog();
                finish();
            }
        });
    }

    void showProgressDialog() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    void dismissProgressDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            dismissProgressDialog();
        }
    }
}
