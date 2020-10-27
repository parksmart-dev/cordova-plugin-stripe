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

        Plugin.initApplePay = function (options, success, error) 
        {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'initApplePay', [options.publishableKey, options.stripeAccount]);
        };
        
        Plugin.payWithApplePay = function (options, success, error) 
        {
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'payWithApplePay', [options.merchantId, options.amount, options.currencyCode, options.stripeKey, options.stripeAccount]);
        };

        Plugin.initGooglePay = function (options, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'initGooglePay', [options.publishableKey, options.stripeAccount]);
        };
        
        Plugin.payWithGooglePay = function (options, success, error) 
        {
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'payWithGooglePay', [options.amount, options.currencyCode, options.stripeKey, options.stripeAccount]);
        };
        
        return Plugin;
    }());
    CordovaStripe.Plugin = Plugin;
})(CordovaStripe = exports.CordovaStripe || (exports.CordovaStripe = {}));
