#import "PianoSdk.h"

@import PianoOAuth;
@import UIKit;
@import PianoComposer;

@interface PianoSdk (NSObject) <PianoComposerDelegate, PianoShowTemplateDelegate, PianoIDDelegate>
@end


@implementation PianoSdk

@synthesize didSignInHandler;
@synthesize didCancelSignInHandler;
@synthesize didSignOutHandler;
@synthesize eventParameters;
@synthesize showLoginHandler;
@synthesize showTemplateHandler;
@synthesize presentTemplateController;


BOOL _hasListeners;
static NSString *eventName = @"PIANO_LISTENER";
static const float popViewPreferredWidth = 600;
static const float popViewPreferredHeight = 730;


RCT_EXPORT_MODULE(PianoSdk)

RCT_EXPORT_METHOD(initWithAID:(NSString *)AID endpointURL:(NSString *)endpointURL) {
    [PianoID.shared setDelegate:self];
    
    [PianoID.shared setAid:AID];
    [PianoID.shared setEndpointUrl:endpointURL];
}
                  
RCT_EXPORT_METHOD(signInWithGoogleCID:(NSString *)GCID
                  widgetType:(int)widgetType
                  didSignInForTokenWithError:(RCTResponseSenderBlock)didSignInHandler
                  didCancelSignIn:(RCTResponseSenderBlock)didCancelSignInHandler)

{
    [self setDidSignInHandler:didSignInHandler];
    [self setDidCancelSignInHandler:didCancelSignInHandler];
    
    [PianoID.shared setWidgetType:widgetType];
    [PianoID.shared setSignUpEnabled:YES];
    [PianoID.shared setGoogleClientId:GCID];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *topViewController = [PianoSdk topMostController];
        [PianoID.shared setPresentingViewController:topViewController];
    });
    
    [PianoID.shared signIn];
}

RCT_EXPORT_METHOD(signOutWithToken:(NSString *)token
                  didSignOutHandler:(RCTResponseSenderBlock)didSignOutHandler)
{
    [self setDidSignOutHandler:didSignOutHandler];
    
    [PianoID.shared signOutWithToken:token];
}


#pragma mark - Helper methods

+ (UIViewController*) topMostController
{
    UIViewController *topController = [UIApplication sharedApplication].keyWindow.rootViewController;
    while (topController.presentedViewController) {
        topController = topController.presentedViewController;
    }
    
    return topController;
}


#pragma mark - PianoIDDelegate

-(void)signInResult:(PianoIDSignInResult *)result withError:(NSError *)error {
    if (error) { // Failed
        NSMutableDictionary *errorInfo = error.userInfo.mutableCopy;
        [errorInfo setObject:error.domain forKey:@"domain"];
        self.didSignInHandler(@[@{@"error": errorInfo}]);
    } else if (result.token) { // Success
        self.didSignInHandler(@[result.token.accessToken]);
    }
}

-(void)signOutWithError:(NSError *)error {
    self.didSignOutHandler(@[error? error : [NSNull null]]);
}

-(void)cancel {
    self.didCancelSignInHandler(@[]);
}

RCT_EXPORT_METHOD(
                  executeWithAID:(NSString *)AID
                  sandbox:(BOOL)sandbox
                  tags:(nullable NSSet *)tags
                  zoneID:(nullable NSString *)zoneID
                  referrer:(nullable NSString *)referrer
                  url:(nullable NSString *)url
                  contentAuthor:(nullable NSString *)contentAuthor
                  contentCreated:(nullable NSString *)contentCreated
                  contentSection:(nullable NSString *)contentSection
                  customVariables:(nullable NSDictionary *)customnVariables
                  userToken:(nullable NSString *)userToken
                  showLoginHandler:(RCTResponseSenderBlock)showLoginHandler
                  showTemplateHandler:(RCTResponseSenderBlock)showTemplateHandler
                  )
{
    [self setEventParameters:[NSMutableDictionary new]];
    [self setShowLoginHandler:showLoginHandler];
    [self setShowTemplateHandler:showTemplateHandler];
    
    PianoComposer *composer = [[PianoComposer alloc] initWithAid:AID sandbox:sandbox];
    [composer setDelegate:self];
    
    if(tags.count > 0) {
        for (id tag in tags) {
            if ([tag isEqual:[NSNull null]]) {
                @throw @"PianoSdk: While calling [PianoComposer \
                executeWithAID:sandbox:tags:...] All values of tags must not be NSNull";
            }
        }
        [composer setTags:tags];
    }
    if(zoneID != nil) {
        [composer setZoneId:zoneID];
    }
    if(referrer != nil) {
        [composer setReferrer:referrer];
    }
    if(url != nil) {
        [composer setUrl:url];
    }
    if(customnVariables != nil) {
        [composer setCustomVariables:customnVariables];
    }
    if(userToken != nil) {
        [composer setUserToken:userToken];
    }
    
    if (contentAuthor != nil) {
        [composer setContentAuthor:contentAuthor];
    }
       
    if (contentCreated != nil) {
        [composer setContentCreated:contentCreated];
    }
       
    if (contentSection != nil) {
        [composer setContentSection:contentSection];
    }
    
    [composer execute];
}

RCT_EXPORT_METHOD(closeTemplateControllerWithCompleteHandler:(RCTResponseSenderBlock)completeHandler) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.presentTemplateController dismissViewControllerAnimated:YES
                                                           completion:^{
            if(completeHandler) {
                completeHandler(@[]);
            }
        }];
    });
}

- (NSArray<NSString *> *)supportedEvents {
    return @[eventName];
}

- (void)startObserving {
  _hasListeners = YES;
}

- (void)stopObserving {
  _hasListeners = NO;
}


#pragma mark - piano delegate

-(void)showLoginWithComposer:(PianoComposer *)composer event:(XpEvent *)event params:(ShowLoginEventParams *)params {
    if (self.showLoginHandler != nil) {
        @try { // To avoid crash if experince shows login multiple times
            self.showLoginHandler(@[self.eventParameters]);
            [self setShowLoginHandler:nil]; // To avoid calling handler twice
        } @catch (NSException *exception) {}
    }
}

-(void)showTemplateWithComposer:(PianoComposer *)composer event:(XpEvent *)event params:(ShowTemplateEventParams *)params {
    
    [self.eventParameters setObject:@(params.showCloseButton) forKey:@"showCloseButton"];
    
    PianoShowTemplatePopupViewController *showTemplate = [[PianoShowTemplatePopupViewController alloc] initWithParams:params];
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad )
    {
        [showTemplate setPreferredContentSize:CGSizeMake(popViewPreferredWidth, popViewPreferredHeight)];
    }
    [self setPresentTemplateController:showTemplate];
    [showTemplate setDelegate:self];
    [showTemplate show];
    
    if (self.showTemplateHandler != nil) {
        @try {  // To avoid crash if experince shows login multiple times
            self.showTemplateHandler(@[self.eventParameters]);
            [self setShowTemplateHandler:nil]; // To avoid calling handler twice
        } @catch (NSException *exception) {}
    }
}

-(void)userSegmentTrueWithComposer:(PianoComposer *)composer event:(XpEvent *)event {
}

-(void)userSegmentFalseWithComposer:(PianoComposer *)composer event:(XpEvent *)event {
}

-(void)meterActiveWithComposer:(PianoComposer *)composer event:(XpEvent *)event params:(PageViewMeterEventParams *)params {
    
    [self.eventParameters setObject:params.meterName forKey:@"meterName"];
    [self.eventParameters setObject:@(params.views) forKey:@"views"];
    [self.eventParameters setObject:@(params.viewsLeft) forKey:@"viewsLeft"];
    [self.eventParameters setObject:@(params.maxViews) forKey:@"maxViews"];
    [self.eventParameters setObject:@(params.totalViews) forKey:@"totalViews"];
}

-(void)meterExpiredWithComposer:(PianoComposer *)composer event:(XpEvent *)event params:(PageViewMeterEventParams *)params {
    [self.eventParameters setObject:params.meterName forKey:@"meterName"];
    [self.eventParameters setObject:@(params.views) forKey:@"views"];
    [self.eventParameters setObject:@(params.viewsLeft) forKey:@"viewsLeft"];
    [self.eventParameters setObject:@(params.maxViews) forKey:@"maxViews"];
    [self.eventParameters setObject:@(params.totalViews) forKey:@"totalViews"];
}

-(void)experienceExecuteWithComposer:(PianoComposer *)composer event:(XpEvent *)event params:(ExperienceExecuteEventParams *)params {
}

-(void)experienceExecutionFailedWithComposer:(PianoComposer *)composer event:(XpEvent *)event params:(FailureEventParams *)params {
}

-(void)composerExecutionCompletedWithComposer:(PianoComposer *)composer {
}


#pragma mark - piano show template delegate

-(void)onRegisterWithEventData:(id)eventData {
    [self sendEventWithName:eventName body:@{
        @"eventName": @"templateCustomEvent",
        @"eventData": @{@"eventName": @"register"}
    }];
}

-(void)onLoginWithEventData:(id)eventData {
    [self sendEventWithName:eventName body:@{
        @"eventName": @"templateCustomEvent",
        @"eventData": @{@"eventName": @"login"}
    }];
}

-(void)onCustomEventWithEventData:(id)eventData {
    [self sendEventWithName:eventName body:@{
        @"eventName": @"templateCustomEvent",
        @"eventData": @{@"eventName": eventData[@"eventName"]}
    }];
}

@end

