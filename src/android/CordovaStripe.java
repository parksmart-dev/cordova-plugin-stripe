package com.zyramedia.cordova.stripe;

import android.app.Activity;
import android.content.Intent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Optional;

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
    private CordovaWebView webView;

    
    public void initialize(CordovaInterface cordova, CordovaWebView webView) 
    {
        super.initialize(cordova, webView);
        webView = webView;
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


    private IsReadyToPayRequest createIsReadyToPayRequest() throws JSONException 
    {
        final JSONArray allowedAuthMethods = new JSONArray();
        allowedAuthMethods.put("PAN_ONLY");
        allowedAuthMethods.put("CRYPTOGRAM_3DS");

        final JSONArray allowedCardNetworks = new JSONArray();
        allowedCardNetworks.put("AMEX");
        allowedCardNetworks.put("DISCOVER");
        allowedCardNetworks.put("MASTERCARD");
        allowedCardNetworks.put("VISA");

        final JSONObject isReadyToPayRequestJson = new JSONObject();
        isReadyToPayRequestJson.put("allowedAuthMethods", allowedAuthMethods);
        isReadyToPayRequestJson.put("allowedCardNetworks", allowedCardNetworks);

        return IsReadyToPayRequest.fromJson(isReadyToPayRequestJson.toString());
    }


    private void initGooglePay(final String key, final CallbackContext callbackContext) 
    {
        publishableKey = key;

        stripeInstance = new Stripe(webView.getContext(), publishableKey);

        paymentsClient = Wallet.getPaymentsClient(
                cordova.getContext(),
                new Wallet.WalletOptions.Builder()
                    .setEnvironment(publishableKey == null || publishableKey.contains("test") ? WalletConstants.ENVIRONMENT_TEST : WalletConstants.ENVIRONMENT_PRODUCTION)
                    .build());

        IsReadyToPayRequest request = createIsReadyToPayRequest();

        paymentsClient.isReadyToPay(request).addOnCompleteListener(
            new OnCompleteListener<Boolean>() 
            {
                public void onComplete(Task<Boolean> task) 
                {
                    try 
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
                    catch (ApiException exception) 
                    { 
                        callbackContext.error(exception.getLocalizedMessage());
                    }
                }
            }
        );
    }


    private Optional<JSONObject> createPaymentDataRequest(String totalPrice, String currencyCode) 
    {

        return PaymentDataRequest.fromJson("{"
        + "\"apiVersion\": 2,"
        + "\"apiVersionMinor\": 0,"
        + "\"merchantInfo\": {"
            + "\"merchantName\": \"Example Merchant\""
            + "},"
            + "\"allowedPaymentMethods\": ["
                + "{"
                    + "\"type\": \"CARD\","
                    + "\"parameters\": {"
                        + "\"allowedAuthMethods\": [\"PAN_ONLY\", \"CRYPTOGRAM_3DS\"],"
                        + "\"allowedCardNetworks\": [\"AMEX\", \"DISCOVER\", \"MASTERCARD\", \"VISA\"]"
                        + "},"
                        + "\"tokenizationSpecification\": {"
                            + "\"type\": \" +  WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY + \","
                            + "\"parameters\": {"
                                + "\"gateway\": \"stripe\""
                                + "}"
                                + "}"
                                + "}"
                                + "],"
                                + "\"transactionInfo\": {"
                                    + "\"totalPriceStatus\": \" + WalletConstants.TOTAL_PRICE_STATUS_FINAL + \","
                                    + "\"totalPrice\": \" + totalPrice + \","
                                    + "\"currencyCode\": \" + currencyCode + \""
                                    + "}"
                                    + "}");

        /*

        final JSONObject tokenizationSpec = new JSONObject();
        tokenizationSpec.put("type", WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY);
        tokenizationSpec.put("parameters", new JSONObject().put("gateway", "stripe"));

        final JSONArray allowedAuthMethods = new JSONArray();
        allowedAuthMethods.put("PAN_ONLY");
        allowedAuthMethods.put("CRYPTOGRAM_3DS");

        final JSONArray allowedCardNetworks = new JSONArray();
        allowedCardNetworks.put("AMEX");
        allowedCardNetworks.put("DISCOVER");
        allowedCardNetworks.put("MASTERCARD");
        allowedCardNetworks.put("VISA");


        final JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");
        cardPaymentMethod.put("parameters", new JSONObject().put("allowedAuthMethods", allowedAuthMethods)
                                                            .put("allowedCardNetworks", allowedCardNetworks));
        cardPaymentMethod.put("tokenizationSpecification", tokenizationSpec);

        final JSONObject paymentDataRequest = new JSONObject();
        paymentDataRequest.put("apiVersion", 2);
        paymentDataRequest.put("apiVersionMinor", 0);
        paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod));
        paymentDataRequest.put("transactionInfo", new JSONObject()
                                .put("totalPrice", totalPrice)
                                .put("totalPriceStatus", WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                .put("currencyCode", currencyCode));
        paymentDataRequest.put("merchantInfo", new JSONObject()
                                .put("merchantName", "Example Merchant"));
        paymentDataRequest.put("emailRequired", false);

        return PaymentDataRequest.fromJson(paymentDataRequest.toString());

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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE: {
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        // You can get some data on the user's card, such as the
                        // brand and last 4 digits
                        CardInfo info = paymentData.getCardInfo();
                        // You can also pull the user address from the
                        // PaymentData object.
                        UserAddress address = paymentData.getShippingAddress();
                        // This is the raw JSON string version of your Stripe token.
                        String rawToken = paymentData.getPaymentMethodToken()
                            .getToken();

                        // Now that you have a Stripe token object,
                        // charge that by using the id
                        Token stripeToken = Token.fromString(rawToken);
                        if (stripeToken != null) {
                            // This chargeToken function is a call to your own
                            // server, which should then connect to Stripe's
                            // API to finish the charge.
                            chargeToken(stripeToken.getId());
                        }
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        break;
                    }
                    case AutoResolveHelper.RESULT_ERROR: {
                        // Log the status for debugging
                        // Generally there is no need to show an error to
                        // the user as the Google Payment API will do that
                        final Status status =
                            AutoResolveHelper.getStatusFromIntent(data);
                        break;
                    }
                    default: {
                        // Do nothing.
                    }
                }
                break;
            }
            default: {
                // Handle any other startActivityForResult calls you may have made.
            }
        }
    }

    /*

    OLD CODE

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

    */

    /*

    private void onGooglePayResult(@NonNull Intent data) 
    {
        PaymentData paymentData = PaymentData.getFromIntent(data);

        if (paymentData == null) 
        {
            return;
        }

        PaymentMethodCreateParams paymentMethodCreateParams = PaymentMethodCreateParams.createFromGooglePay(new JSONObject(paymentData.toJson()));

        stripe.createPaymentMethod(
            paymentMethodCreateParams,
            new ApiResultCallback<PaymentMethod>() {
                @Override
                public void onSuccess(@NonNull PaymentMethod result) 
                {
                    googlePayCallbackContext.success(result);
                }

                @Override
                public void onError(@NonNull Exception e) 
                {
                    googlePayCallbackContext.error("Error occurred while attempting to pay with GooglePay. Error #" + e.toString());
                }
            }
        );
    }

    */
}
