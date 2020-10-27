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
  
  export interface ApplePayOptions {
    merchantId: string;
    amount: string;
    currencyCode: string;
    stripeKey: string;
    stripeAccount: string;
  }

  export interface ApplePayInit {
    publishableKey: string;
    stripeAccount: string;
  }

  export interface GooglePayInit {
    publishableKey: string;
    stripeAccount: string;
  }

  export interface GooglePayOptions {
    amount: string;
    currencyCode: string;
    stripeKey: string;
    stripeAccount: string;
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

  export interface Error {
    message: string;
  }

  export type BlankCallback = () => void;
  export type ErrorCallback = (error: Error) => void;
  export type PaymentSuccessCallback = (result: string) => void;

  export class Plugin 
  {  
    static initApplePay(options: ApplePayInit, success = NOOP, error: ErrorCallback = NOOP) 
    {
      exec(success, error, 'CordovaStripe', 'initApplePay', [options.publishableKey, options.stripeAccount]);
    }

    static payWithApplePay(options: ApplePayOptions, success: PaymentSuccessCallback, error: ErrorCallback = NOOP) 
    {
      if (!options || !options.merchantId) 
      {
        error({
          message: 'Missing one or more payment options.'
        });
        return;
      }

      exec(success, error, 'CordovaStripe', 'payWithApplePay', [options.merchantId, options.amount, options.currencyCode, options.stripeKey, options.stripeAccount]);
    }

    static initGooglePay(options: GooglePayInit, success = NOOP, error: ErrorCallback = NOOP) 
    {
      exec(success, error, 'CordovaStripe', 'initGooglePay', [options.publishableKey, options.stripeAccount]);
    }

    static payWithGooglePay(options: GooglePayOptions, success: PaymentSuccessCallback, error: ErrorCallback = NOOP) 
    {
      exec(success, error, 'CordovaStripe', 'payWithGooglePay', [options.amount, options.currencyCode, options.stripeKey, options.stripeAccount]);
    }
  }
}

