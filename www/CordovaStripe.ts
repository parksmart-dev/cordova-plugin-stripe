import { exec } from 'cordova';

const NOOP: any = () => {};

export interface Window {
  cordova: Cordova;
}

export interface Cordova {
  plugins: CordovaPlugins;
}

export interface CordovaPlugins {
  stripe: typeof CordovaStripe.Plugin;
}

export namespace CordovaStripe {
  

  export interface TokenResponse {
    id: string;
    type: string;
    created: Date;
  }

  export interface ApplePayItem {
    label: string;
    amount: number | string;
  }

  export interface ApplePayOptions {
    merchantId: string;
    country: string;
    currency: string;
    items: ApplePayItem[];
  }

  export interface GooglePayInit {
    publishableKey: string;
  }

  export interface GooglePayOptions {
    amount: string;
    currencyCode: string;
  }

  export interface ThreeDeeSecureParams {
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

  export interface GiroPayParams {
    amount: number;
    name: string;
    returnURL: string;
    statementDescriptor: string;
  }

  export interface iDEALParams {
    amount: number;
    name: string;
    returnURL: string;
    statementDescriptor: string;
    bank: string;
  }

  export interface SEPADebitParams {
    name: string;
    iban: string;
    addressLine1: string;
    city: string;
    postalCode: string;
    country: string;
  }

  export interface SofortParams {
    amount: number;
    returnURL: string;
    country: string;
    statementDescriptor: string;
  }

  export interface AlipayParams {
    amount: number;
    currency: string;
    returnURL: string;
  }

  export interface AlipayReusableParams {
    currency: string;
    returnURL: string;
  }

  export interface P24Params {
    amount: number;
    currency: string;
    email: string;
    name: string;
    returnURL: string;
  }

  export interface VisaCheckoutParams {
    callId: string;
  }

  export type SourceParams = ThreeDeeSecureParams | GiroPayParams | iDEALParams | SEPADebitParams | SofortParams | AlipayParams | AlipayReusableParams | P24Params | VisaCheckoutParams;

  export enum SourceType {
    ThreeDeeSecure = '3ds',
    GiroPay = 'giropay',
    iDEAL = 'ideal',
    SEPADebit = 'sepadebit',
    Sofort = 'sofort',
    AliPay = 'alipay',
    AliPayReusable = 'alipayreusable',
    P24 = 'p24',
    VisaCheckout = 'visacheckout',
  }

  const SourceTypeArray: SourceType[] = Object.keys(SourceType).map(key => SourceType[key]);

  export interface Address {
    line1: string;
    line2: string;
    city: string;
    postal_code: string;
    state: string;
    country: string;
  }

  export interface LegalEntity {
    address?: Address;
    dob?: {
      day: number;
      month: number;
      year: number;
    },
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
    },
    personal_id_number_provided?: boolean;
    phone_number?: string;
    ssn_last_4_provided?: boolean;
    tax_id_registrar?: string;
    type?: 'individual' | 'company';
    verification?: any;
  }

  export interface AccountParams {
    tosShownAndAccepted: boolean;
    legalEntity: LegalEntity;
  }

  export interface Error {
    message: string;
  }

  export interface PaymentResult {
      id: string;
  }

  export type BlankCallback = () => void;
  export type ErrorCallback = (error: Error) => void; 
  export type PaymentSuccessCallback = (result: PaymentResult) => void;

  export class Plugin {
    /**
     * Set publishable key
     * @param {string} key
     * @param {Function} success
     * @param {Function} error
     */
    static setPublishableKey(key: string, success: BlankCallback = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'setPublishableKey', [key]);
    }

    /**
     * Pay with ApplePay
     * @param {CordovaStripe.ApplePayOptions} options
     * @param {(token: string, callback: (paymentProcessed: boolean) => void) => void} success
     * @param {Function} error
     */
    static payWithApplePay(options: ApplePayOptions, success: (token: TokenResponse, callback: (paymentProcessed: boolean) => void) => void, error: ErrorCallback = NOOP) {
      if (!options || !options.merchantId || !options.country || !options.currency || !options.items || !options.items.length) {
        error({
          message: 'Missing one or more payment options.'
        });
        return;
      }

      options.items = options.items.map(item => {
        item.amount = String(item.amount);
        return item;
      });

      exec((token: TokenResponse) => {
        success(token, (paymentProcessed: boolean) => {
          exec(NOOP, NOOP, 'CordovaStripe', 'finalizeApplePayTransaction', [Boolean(paymentProcessed)]);
        });
      }, error, 'CordovaStripe', 'initializeApplePayTransaction', [
        options.merchantId,
        options.country,
        options.currency,
        options.items
      ])
    }

    static initGooglePay(options: GooglePayInit, success = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'initGooglePay', [options.publishableKey]);
    }

    static payWithGooglePay(options: GooglePayOptions, success: PaymentSuccessCallback = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'payWithGooglePay', [options.amount, options.currencyCode]);
    }
  }
}

