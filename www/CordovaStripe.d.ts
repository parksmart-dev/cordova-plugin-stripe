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
      }
    interface PaymentMethod {
        id: string;
        type: string;
        created: Date;
    }
    interface TokenResponse {
        id: string;
        type: string;
        created: Date;
    }
    interface ApplePayItem {
        label: string;
        amount: number | string;
    }
    interface ApplePayOptions {
        merchantId: string;
        country: string;
        currency: string;
        items: ApplePayItem[];
    }
    interface GooglePayOptions {
        amount: string;
        currencyCode: string;
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
    interface LegalEntity {
        address?: Address;
        dob?: {
            day: number;
            month: number;
            year: number;
        };
        first_name?: string;
        last_name?: string;
        gender?: 'male' | 'female';
        personal_address?: Address;
        business_name?: string;
        business_url?: string;
        business_tax_id_provided?: boolean;
        business_vat_id_provided?: string;
        country?: string;
        tos_acceptance?: {
            date: number;
            ip: string;
        };
        personal_id_number_provided?: boolean;
        phone_number?: string;
        ssn_last_4_provided?: boolean;
        tax_id_registrar?: string;
        type?: 'individual' | 'company';
        verification?: any;
    }
    interface AccountParams {
        tosShownAndAccepted: boolean;
        legalEntity: LegalEntity;
    }
    interface Error {
        message: string;
    }

    type BlankCallback = () => void;
    type ErrorCallback = (error: Error) => void;
    type PaymentSuccessCallback = (result: PaymentMethod) => void;
    
    class Plugin {
        /**
         * Set publishable key
         * @param {string} key
         * @param {Function} success
         * @param {Function} error
         */
        static setPublishableKey(key: string, success?: BlankCallback, error?: ErrorCallback): void;
        /**
         * Create a credit card token
         * @param {CordovaStripe.CardTokenRequest} creditCard
         * @param {CordovaStripe.CardTokenCallback} success
         * @param {CordovaStripe.ErrorCallback} error
         */
        static payWithApplePay(options: ApplePayOptions, success: (token: TokenResponse, callback: (paymentProcessed: boolean) => void) => void, error?: ErrorCallback): void;
        static initGooglePay(options: GooglePayInit, success?: any, error?: ErrorCallback): void;
        static payWithGoogle(options: GooglePayOptions, success?: PaymentSuccessCallback, error?: ErrorCallback): void;
    }
}
