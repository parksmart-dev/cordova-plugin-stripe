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

    [[STPPaymentConfiguration sharedConfiguration] setPublishableKey:publishableKey];
    
    if (self.client == nil) 
    {
        // init client if doesn't exist
        client = [[STPAPIClient alloc] init];
    } 
    else 
    {
        [self.client setPublishableKey:publishableKey];
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

    PKPaymentRequest *paymentRequest = [Stripe paymentRequestWithMerchantIdentifier:merchantIdentifier country:@"GB" currency:currencyCode];

    paymentRequest.paymentSummaryItems = @[
        // The final line should represent your company;
        // it'll be prepended with the word "Pay" (i.e. "Pay iHats, Inc $50")
        [PKPaymentSummaryItem summaryItemWithLabel:@"ParkSmart" amount:[NSDecimalNumber decimalNumberWithString:amount]],
    ];

    if ([Stripe canSubmitPaymentRequest:paymentRequest]) 
    {
        PKPaymentAuthorizationViewController *paymentAuthorizationViewController = [[PKPaymentAuthorizationViewController alloc] initWithPaymentRequest:paymentRequest];
        
        paymentAuthorizationViewController.delegate = self.appDelegate;
        self.applePayCDVCallbackId = command.callbackId;
        
        NSLog(@"Callback ID is %@", command.callbackId);
        
        [self.viewController presentViewController:paymentAuthorizationViewController animated:YES completion:nil];
    } 
    else 
    {
        NSLog(@"Problem with integration");
    } 
}


- (void)processPayment: (PKPaymentAuthorizationViewController *)controller didAuthorizePayment:(PKPayment *)payment completion:(void (^)(PKPaymentAuthorizationStatus))completion
{
    [self.client createTokenWithPayment:payment completion:^(STPToken *token, NSError *error) 
    {
        CDVPluginResult *result;
        
        if (error != nil) 
        {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        } 
        else if (token == nil) 
        {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unable to retrieve token"];
        } 
        else 
        {
            self.applePayCompleteCallback = completion;
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:token.tokenId];
        }
        
        //NSLog(@"Result is %@", token.tokenId);

        [self.viewController dismissViewControllerAnimated:YES completion:nil];
        
        [self.commandDelegate sendPluginResult:result callbackId:self.applePayCDVCallbackId];
        self.applePayCDVCallbackId = nil;
    }];
}


- (void)finalizeApplePayTransaction: (CDVInvokedUrlCommand *) command
{
    BOOL successful = [command.arguments objectAtIndex:0];
    if (self.applePayCompleteCallback) {
        self.applePayCompleteCallback(successful? PKPaymentAuthorizationStatusSuccess : PKPaymentAuthorizationStatusFailure);
        self.applePayCompleteCallback = nil;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
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

