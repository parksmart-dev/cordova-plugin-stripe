#import <Cordova/CDV.h>
@import Stripe;

typedef void(^ApplePayCallback)(PKPaymentAuthorizationStatus);

@interface CordovaStripe : CDVPlugin
@property (nonatomic, retain) STPAPIClient *client;
@property (nonatomic, copy) ApplePayCallback applePayCompleteCallback;
@property (nonatomic, retain) NSString *applePayCDVCallbackId;
extern NSArray *CardBrands;

- (void) initApplePay: (CDVInvokedUrlCommand *) command;
- (void) payWithApplePay: (CDVInvokedUrlCommand *) command;
- (void) applePayContext: (STPApplePayContext *)context didCreatePaymentMethod:(STPPaymentMethod *)paymentMethod paymentInformation:(PKPayment *)paymentInformation completion:(STPIntentClientSecretCompletionBlock)completion;
//- (void) finalizeApplePayTransaction:(CDVInvokedUrlCommand *) command;

@end
