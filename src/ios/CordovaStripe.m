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
    NSDictionary* const requestParams = [[command arguments] objectAtIndex:0];

    NSString* publishableKey = requestParams[@"publishableKey"];

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
    
    [self.commandDelegate runInBackground:^{
        
        NSDictionary* const requestParams = [[command arguments] objectAtIndex:0];

        NSString *merchantIdentifier = requestParams[@"merchantIdentifier"];
        NSString *currencyCode = requestParams[@"currencyCode"];
        NSString *amount = requestParams[@"amount"];

        PKPaymentRequest *paymentRequest = [Stripe paymentRequestWithMerchantIdentifier:merchantIdentifier currency:currencyCode];

        paymentRequest.paymentSummaryItems = @[
            // The final line should represent your company;
            // it'll be prepended with the word "Pay" (i.e. "Pay iHats, Inc $50")
            [PKPaymentSummaryItem summaryItemWithLabel:@"ParkSmart" amount:[NSDecimalNumber decimalNumberWithString:amount]],
        ];

        paymentRequest.paymentSummaryItems = paymentSummaryItems;

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
    }];    
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
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:token.allResponseFields];
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:self.applePayCDVCallbackId];
        self.applePayCDVCallbackId = nil;
    }];
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

