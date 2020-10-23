"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var cordova_1 = require("cordova");
var NOOP = function () { };
var CordovaStripe;
(function (CordovaStripe) {
    var SourceType;
    (function (SourceType) {
        SourceType["ThreeDeeSecure"] = "3ds";
        SourceType["GiroPay"] = "giropay";
        SourceType["iDEAL"] = "ideal";
        SourceType["SEPADebit"] = "sepadebit";
        SourceType["Sofort"] = "sofort";
        SourceType["AliPay"] = "alipay";
        SourceType["AliPayReusable"] = "alipayreusable";
        SourceType["P24"] = "p24";
        SourceType["VisaCheckout"] = "visacheckout";
    })(SourceType = CordovaStripe.SourceType || (CordovaStripe.SourceType = {}));
    var SourceTypeArray = Object.keys(SourceType).map(function (key) { return SourceType[key]; });
    var Plugin = /** @class */ (function () {
        function Plugin() {
        }
        /**
         * Set publishable key
         * @param {string} key
         * @param {Function} success
         * @param {Function} error
         */
        Plugin.setPublishableKey = function (key, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'setPublishableKey', [key]);
        };
        /**
         * Pay with ApplePay
         * @param {CordovaStripe.ApplePayOptions} options
         * @param {(token: string, callback: (paymentProcessed: boolean) => void) => void} success
         * @param {Function} error
         */
        Plugin.payWithApplePay = function (options, success, error) {
            if (error === void 0) { error = NOOP; }
            if (!options || !options.merchantId || !options.country || !options.currency || !options.items || !options.items.length) {
                error({
                    message: 'Missing one or more payment options.'
                });
                return;
            }
            options.items = options.items.map(function (item) {
                item.amount = String(item.amount);
                return item;
            });
            cordova_1.exec(function (token) {
                success(token, function (paymentProcessed) {
                    cordova_1.exec(NOOP, NOOP, 'CordovaStripe', 'finalizeApplePayTransaction', [Boolean(paymentProcessed)]);
                });
            }, error, 'CordovaStripe', 'initializeApplePayTransaction', [
                options.merchantId,
                options.country,
                options.currency,
                options.items
            ]);
        };
        Plugin.initGooglePay = function (options, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'initGooglePay', [options.publishableKey]);
        };
        
        Plugin.payWithGooglePay = function (options, success, error) 
        {
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'payWithGooglePay', [options.amount, options.currencyCode, options.stripeKey]);
        };
        
        return Plugin;
    }());
    CordovaStripe.Plugin = Plugin;
})(CordovaStripe = exports.CordovaStripe || (exports.CordovaStripe = {}));
