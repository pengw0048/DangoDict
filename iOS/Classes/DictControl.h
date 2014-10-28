//
//  DictControl.h
//  DangoDict J2MEtoIOS
//
//  Created by Tiancai HB on 11-5-31.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface DictControl : UIViewController <UITableViewDelegate,UITableViewDataSource> {
	UITextField *tb;
	UIWebView *web;
	UIButton *ok,*pok,*dohide;
	UITableView *tv,*tv2;
	NSString *dictName[100],*tts[100][50];
	int defTotal[100],p1[100],p2[100],p3[100],p4[100],dn,dnpx[100],tsn,ttsn[100],tpos[100];
	BOOL busy,isdoc[100],rs,re;
	char outer[1000000],dfn[100][500],tsc[50][100];
}
@property (nonatomic, retain) IBOutlet UITextField *tb;
@property (nonatomic, retain) IBOutlet UIWebView *web;
@property (nonatomic, retain) IBOutlet UIButton *ok;
@property (nonatomic, retain) IBOutlet UIButton *pok;
@property (nonatomic, retain) IBOutlet UIButton *pset;
@property (nonatomic, retain) IBOutlet UIButton *dohide;
@property (nonatomic, retain) IBOutlet UITableView *tv;
@property (nonatomic, retain) IBOutlet UITableView *tv2;
-(IBAction) search:(id)sender;
-(IBAction) done:(id)sender;
-(IBAction) pdone:(id)sender;
-(IBAction) pgoset:(id)sender;
-(IBAction) hide:(id)sender;
-(IBAction) tchange:(id)sender;
@end
