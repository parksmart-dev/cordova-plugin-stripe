package com.zyramedia.cordova.stripe;

import android.app.Activity;
import android.content.Intent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.stripe.android.CardUtils;
import com.stripe.android.Stripe;
import com.stripe.android.model.AccountParams;
import com.stripe.android.model.Card;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceParams;
import com.stripe.android.model.Token;
import com.stripe.android.GooglePayConfig;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.model.PaymentMethod;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CordovaStripe extends CordovaPlugin 
{
    private Stripe stripeInstance;
    private String publishableKey;
    private PaymentsClient paymentsClient;
    private boolean googlePayReady;
    private PaymentMethodTokenizationParameters googlePayParams;
    private final int LOAD_PAYMENT_DATA_REQUEST_CODE = 53;
    private CallbackContext googlePayCallbackContext;

    
    public void initialize(CordovaInterface cordova, CordovaWebView webView) 
    {
        super.initialize(cordova, webView);
    }


    @Override
    public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException 
    {
        switch (action) 
        {
            case "initGooglePay":
                initGooglePay(data.getString(0), callbackContext);
                break;

            case "payWithGooglePay":
                payWithGooglePay(data.getString(0), data.getString(1), callbackContext);
                break;

            default:
                return false;
        }

        return true;
    }


    private IsReadyToPayRequest createIsReadyToPayRequest()
    {
        return IsReadyToPayRequest.fromJson("{"
            + "\"allowedAuthMethods\": [\"PAN_ONLY\", \"CRYPTOGRAM_3DS\"],"
            + "\"allowedCardNetworks\": [\"AMEX\", \"DISCOVER\", \"MASTERCARD\", \"VISA\"]"
            + "}");
    }


    private void initGooglePay(String key, final CallbackContext callbackContext) 
    {
        publishableKey = key;

        stripeInstance = new Stripe(webView.getContext(), publishableKey);

        paymentsClient = Wallet.getPaymentsClient(
                cordova.getContext(),
                new Wallet.WalletOptions.Builder()
                    .setEnvironment(publishableKey == null || publishableKey.contains("test") ? WalletConstants.ENVIRONMENT_TEST : WalletConstants.ENVIRONMENT_PRODUCTION)
                    .build());

        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                    .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                    .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                    .build();
        
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                (Task<Boolean> task1) -> {
                    try {
                        googlePayReady = task1.getResult(ApiException.class);
                        if (googlePayReady) {
                            //show Google as payment option

                            callbackContext.success();
                        } else {
                            //hide Google as payment option
                            callbackContext.error("GooglePay not supported." + publishableKey);
                        }
                    } catch (ApiException exception) {
                        callbackContext.error(exception.getLocalizedMessage());
                    }
                });

        /*            

        IsReadyToPayRequest request = createIsReadyToPayRequest();

        paymentsClient.isReadyToPay(request).addOnCompleteListener(
            new OnCompleteListener<Boolean>() 
            {
                public void onComplete(Task<Boolean> task) 
                {
                    if (task.isSuccessful()) 
                    {
                        // show Google Pay as payment option
                        callbackContext.success();
                    } 
                    else 
                    {
                        // hide Google Pay as payment option
                        callbackContext.error("GooglePay not supported.");
                    }
                }
            }
        );
        */
    }


    private PaymentDataRequest createPaymentDataRequest(String totalPrice, String currencyCode) 
    {
        return PaymentDataRequest.fromJson("{"
            + "\"apiVersion\": 2,"
            + "\"apiVersionMinor\": 0,"
            + "\"allowedPaymentMethods\": ["
                + "{"
                    + "\"type\": \"CARD\","
                    + "\"parameters\": {"
                        + "\"allowedAuthMethods\": [\"PAN_ONLY\", \"CRYPTOGRAM_3DS\"],"
                        + "\"allowedCardNetworks\": [\"AMEX\", \"DISCOVER\", \"MASTERCARD\", \"VISA\"],"
                        + "\"billingAddressRequired\": " + false 
                    + "},"
                    + "\"tokenizationSpecification\": {"
                        + "\"type\": \"" + WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY + "\","
                        + "\"parameters\": {"
                            + "\"gateway\": \"stripe\""
                        + "}"
                    + "}"
                + "}"
            + "],"
            + "\"transactionInfo\": {"
                + "\"totalPriceStatus\": \"" + WalletConstants.TOTAL_PRICE_STATUS_FINAL + "\","
                + "\"totalPrice\": \"" + totalPrice + "\","
                + "\"currencyCode\": \"" + currencyCode + "\""
            + "},"
            + "\"merchantInfo\": {"
                + "\"merchantName\": \"Example Merchant\""
            + "},"
            + "\"emailRequired\": " + false 
        + "}");
          
        /*
        final JSONObject tokenizationSpec = new JSONObject()
            .put("type", WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            .put(
                "parameters",
                new JSONObject()
                    .put("gateway", "stripe")
            );

        final JSONObject cardPaymentMethod = new JSONObject()
            .put("type", "CARD")
            .put(
                "parameters",
                new JSONObject()
                    .put("allowedAuthMethods", new JSONArray()
                        .put("PAN_ONLY")
                        .put("CRYPTOGRAM_3DS"))
                    .put("allowedCardNetworks",
                        new JSONArray()
                            .put("AMEX")
                            .put("DISCOVER")
                            .put("MASTERCARD")
                            .put("VISA"))

                    // require billing address
                    .put("billingAddressRequired", false)
                    .put(
                        "billingAddressParameters",
                        new JSONObject()
                            // require full billing address
                            .put("format", "MIN")

                            // require phone number
                            .put("phoneNumberRequired", false)
                    )
            )
            .put("tokenizationSpecification", tokenizationSpec);

        // create PaymentDataRequest
        final JSONObject paymentDataRequest = new JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0)
            .put("allowedPaymentMethods",
                new JSONArray().put(cardPaymentMethod))
            .put("transactionInfo", new JSONObject()
                .put("totalPrice", totalPrice)
                .put("totalPriceStatus", WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .put("currencyCode", currencyCode)
            )
            .put("merchantInfo", new JSONObject()
                .put("merchantName", "Example Merchant"))
            .put("emailRequired", false)
            .toString();

        return PaymentDataRequest.fromJson(paymentDataRequest);
        */
    }


    
    private void payWithGooglePay(String totalPrice, String currencyCode, final CallbackContext callbackContext) 
    {
        cordova.getActivity().runOnUiThread(() -> {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(createPaymentDataRequest(totalPrice, currencyCode)),
                    cordova.getActivity(),
                    LOAD_PAYMENT_DATA_REQUEST_CODE
            );

            googlePayCallbackContext = callbackContext;
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) 
        {
            switch (resultCode) 
            {
                case Activity.RESULT_OK:

                    if (intent != null) 
                    {
                        onGooglePayResult(intent);
                    }
                    
                    break;

                case Activity.RESULT_CANCELED:
                    break;

                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(intent);
                    googlePayCallbackContext.error("Error occurred while attempting to pay with GooglePay. Error #" + status.toString());
                    break;
            }
        }
    }


    private void onGooglePayResult(@NonNull Intent data) 
    {
        PaymentData paymentData = PaymentData.getFromIntent(data);

        paymentData = null;
        if (paymentData == null) 
        {
            googlePayCallbackContext.error("Error with paymentData");
            return;
        }

        try 
        {
            JSONObject jsonObj = new JSONObject(paymentData.toJson());

            PaymentMethodCreateParams paymentMethodCreateParams = PaymentMethodCreateParams.createFromGooglePay(jsonObj);

            stripeInstance.createPaymentMethod(
                paymentMethodCreateParams,
                new ApiResultCallback<PaymentMethod>() {
                    @Override
                    public void onSuccess(@NonNull PaymentMethod result) 
                    {
                        googlePayCallbackContext.success(result.toString());
                    }

                    @Override
                    public void onError(@NonNull Exception e) 
                    {
                        googlePayCallbackContext.error("Error occurred while attempting to pay with GooglePay. Error #" + e.toString());
                    }
                }
            );

        } catch (JSONException e) {
            googlePayCallbackContext.error("JSON error");
        }
    }
}
