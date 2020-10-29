#import "AppDelegate+CordovaStripe.h"
#import "CordovaStripe.h"
@import Stripe;

@implementation AppDelegate (CordovaStripe)
static NSString* const PLUGIN_NAME = @"CordovaStripe";

- (void)applePayContext: (STPApplePayContext *)context didCreatePaymentMethod:(STPPaymentMethod *)paymentMethod paymentInformation:(PKPayment *)paymentInformation completion:(STPIntentClientSecretCompletionBlock)completion
{
    CordovaStripe* pluginInstance = [self.viewController getCommandInstance:PLUGIN_NAME];

    if (pluginInstance != nil)
    {
        // Send token back to plugin
        [pluginInstance applePayContext:context didCreatePaymentMethod:paymentMethod paymentInformation:paymentInformation completion:completion];
    }
    else
    {
        // Discard payment
        NSLog(@"Unable to get plugin instsnce, discarding payment.");
        completion(STPPaymentStatusError);
    }
}

/*
- (void)paymentAuthorizationViewControllerDidFinish:(PKPaymentAuthorizationViewController *)controller 
{
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}
*/

@end
