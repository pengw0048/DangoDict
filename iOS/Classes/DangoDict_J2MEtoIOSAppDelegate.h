//
//  DangoDict_J2MEtoIOSAppDelegate.h
//  DangoDict J2MEtoIOS
//
//  Created by Tiancai HB on 11-5-31.
//  Copyright __MyCompanyName__ 2011. All rights reserved.
//

#import <UIKit/UIKit.h>
@class DictControl;

@interface DangoDict_J2MEtoIOSAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	DictControl *dc;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) DictControl *dc;

@end

