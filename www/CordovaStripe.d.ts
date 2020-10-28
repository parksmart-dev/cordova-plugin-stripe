export interface Window {
    cordova: Cordova;
}
export interface Cordova {
    plugins: CordovaPlugins;
}
export interface CordovaPlugins {
    stripe: typeof CordovaStripe.Plugin;
}
export declare namespace CordovaStripe {
    
    export interface GooglePayInit {
        publishableKey: string;
        stripeAccount: string;
    }

    export interface ApplePayInit {
        publishableKey: string;
        stripeAccount: string;
    }

    interface ApplePayOptions {
        merchantId: string;
        amount: string;
        currencyCode: string;
        stripeKey: string;
        stripeAccount: string;
    }

    interface GooglePayOptions {
        amount: string;
        currencyCode: string;
        stripeKey: string;
        stripeAccount: string;
    }

    interface ThreeDeeSecureParams {
        /**
         * Amount
         */
        amount: number;
        /**
         * Currency code
         */
        currency: string;
        /**
         * URL to redirect to after successfully verifying the card
         */
        returnURL: string;
        /**
         * Card source ID
         */
        card: string;
    }
    interface GiroPayParams {
        amount: number;
        name: string;
        returnURL: string;
        statementDescriptor: string;
    }
    interface iDEALParams {
        amount: number;
        name: string;
        returnURL: string;
        statementDescriptor: string;
        bank: string;
    }
    interface SEPADebitParams {
        name: string;
        iban: string;
        addressLine1: string;
        city: string;
        postalCode: string;
        country: string;
    }
    interface SofortParams {
        amount: number;
        returnURL: string;
        country: string;
        statementDescriptor: string;
    }
    interface AlipayParams {
        amount: number;
        currency: string;
        returnURL: string;
    }
    interface AlipayReusableParams {
        currency: string;
        returnURL: string;
    }
    interface P24Params {
        amount: number;
        currency: string;
        email: string;
        name: string;
        returnURL: string;
    }
    interface VisaCheckoutParams {
        callId: string;
    }
    type SourceParams = ThreeDeeSecureParams | GiroPayParams | iDEALParams | SEPADebitParams | SofortParams | AlipayParams | AlipayReusableParams | P24Params | VisaCheckoutParams;
    enum SourceType {
        ThreeDeeSecure = "3ds",
        GiroPay = "giropay",
        iDEAL = "ideal",
        SEPADebit = "sepadebit",
        Sofort = "sofort",
        AliPay = "alipay",
        AliPayReusable = "alipayreusable",
        P24 = "p24",
        VisaCheckout = "visacheckout",
    }
    interface Address {
        line1: string;
        line2: string;
        city: string;
        postal_code: string;
        state: string;
        country: string;
    }

    interface Error {
        message: string;
    }

    type BlankCallback = () => void;
    type ErrorCallback = (error: Error) => void;
    type PaymentSuccessCallback = (result: string) => void;
    
    class Plugin {

        static initApplePay(options: ApplePayInit, success: string, error?: ErrorCallback): void;

        static payWithApplePay(options: ApplePayOptions, success?: PaymentSuccessCallback, error?: ErrorCallback): void;
        
        static initGooglePay(options: GooglePayInit, success: string, error?: ErrorCallback): void;

        static payWithGoogle(options: GooglePayOptions, success?: PaymentSuccessCallback, error?: ErrorCallback): void;
    }
}
