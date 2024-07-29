#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface PianoSdk : RCTEventEmitter <RCTBridgeModule>

@property (nonatomic, strong) RCTResponseSenderBlock didSignInHandler;
@property (nonatomic, strong) RCTResponseSenderBlock didCancelSignInHandler;
@property (nonatomic, strong) RCTResponseSenderBlock didSignOutHandler;
@property (nonatomic, strong) NSMutableDictionary *eventParameters;
@property (nonatomic, strong) RCTResponseSenderBlock showLoginHandler;
@property (nonatomic, strong) RCTResponseSenderBlock showTemplateHandler;
@property (nonatomic, strong) UIViewController *presentTemplateController;

@end
