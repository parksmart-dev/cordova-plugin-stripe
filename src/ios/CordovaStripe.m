#import "CordovaStripe.h"
@import Stripe;

@implementation CordovaStripe

@synthesize client;
@synthesize applePayCDVCallbackId;
@synthesize applePayCompleteCallback;

NSArray *CardBrands = nil;

- (void)pluginInitialize
{
    CardBrands = [[NSArray alloc] initWithObjects:@"Visa", @"American Express", @"MasterCard", @"Discover", @"JCB", @"Diners Club", @"Unknown", nil];
}


- (void)initApplePay:(CDVInvokedUrlCommand*)command
{
    NSString* publishableKey = [command.arguments objectAtIndex:0];
    NSString* connectedAccount = [command.arguments objectAtIndex:1];

    if (self.client == nil) 
    {
        // init client if doesn't exist
        client = [[STPAPIClient alloc] init];
    }
    else
    {
        [[STPAPIClient sharedClient] setPublishableKey:publishableKey];
        [[STPAPIClient sharedClient] setStripeAccount:connectedAccount];
    }

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)payWithApplePay:(CDVInvokedUrlCommand *)command
{
    if (self.client == nil) 
    {
        [self throwNotInitializedError:command];
        return;
    }
    
    NSString *merchantIdentifier    = [command.arguments objectAtIndex:0];
    NSString *amount                = [command.arguments objectAtIndex:1];
    NSString *currencyCode          = [command.arguments objectAtIndex:2];
    NSString *stripeKey             = [command.arguments objectAtIndex:3];
    NSString *stripeAccount         = [command.arguments objectAtIndex:4];

    [[STPAPIClient sharedClient] setPublishableKey:stripeKey];
    [[STPAPIClient sharedClient] setStripeAccount:stripeAccount];

    PKPaymentRequest *paymentRequest = [Stripe paymentRequestWithMerchantIdentifier:merchantIdentifier country:@"GB" currency:currencyCode];

    paymentRequest.paymentSummaryItems = @[
        // The final line should represent your company;
        // it'll be prepended with the word "Pay" (i.e. "Pay iHats, Inc $50")
        [PKPaymentSummaryItem summaryItemWithLabel:@"ParkSmart" amount:[NSDecimalNumber decimalNumberWithString:amount]],
    ];

    if ([Stripe canSubmitPaymentRequest:paymentRequest]) 
    {
        // Initialize an STPApplePayContext instance
        STPApplePayContext *applePayContext = [[STPApplePayContext alloc] initWithPaymentRequest:paymentRequest delegate:self.appDelegate];
        
        if (applePayContext) 
        {
            self.applePayCDVCallbackId = command.callbackId;
            
            NSLog(@"Callback ID is %@", command.callbackId);

            [applePayContext presentApplePayOnViewController:self.viewController completion:nil];
        } 
        else 
        {
            // There is a problem with your Apple Pay configuration
            NSLog(@"Problem with integration");
        }
    } 
    else 
    {
        NSLog(@"Problem with integration");
    } 
}


- (void)applePayContext: (STPApplePayContext *)context didCreatePaymentMethod:(STPPaymentMethod *)paymentMethod paymentInformation:(PKPayment *)paymentInformation completion:(STPIntentClientSecretCompletionBlock)completion 
{
    //NSLog(@"TOKEN is %@", paymentMethod.stripeId);
    
    CDVPluginResult *result;
    
    if (paymentMethod == nil)
    {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to retrieve token"];
    }
    else
    {
        //self.applePayCompleteCallback = completion;
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:paymentMethod.stripeId];
    }

    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    
    [self.commandDelegate sendPluginResult:result callbackId:self.applePayCDVCallbackId];
    self.applePayCDVCallbackId = nil;
}


- (void)applePayContext:(STPApplePayContext *)context didCompleteWithStatus:(STPPaymentStatus)status error:(NSError *)error
{
    switch (status)
    {
        case STPPaymentStatusSuccess:
            // Payment succeeded, show a receipt view
            NSLog(@"SUCCESS");
            break;

        case STPPaymentStatusError:
        {
            NSString *theError = [error localizedDescription];
            NSLog(@"ERROR ID is %@", theError);
            // Payment failed, show the error

            break;
        }
        case STPPaymentStatusUserCancellation:
            // User cancelled the payment
             NSLog(@"CANCELLED");
            break;
    }
}

- (void)checkApplePaySupport: (CDVInvokedUrlCommand *)command
{
    CDVPluginResult* const result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[Stripe deviceSupportsApplePay]];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void)throwNotInitializedError:(CDVInvokedUrlCommand *) command
{
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must call setPublishableKey method before executing this command."];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


@end

