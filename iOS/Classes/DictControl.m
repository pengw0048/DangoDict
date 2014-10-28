//
//  DictControl.m
//  DangoDict J2MEtoIOS
//
//  Created by Tiancai HB on 11-5-31.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "DictControl.h"
#include <stdio.h>
#include <string.h>

@implementation DictControl
@synthesize tb;
@synthesize web;
@synthesize ok;
@synthesize pok;
@synthesize tv,tv2;
@synthesize pset;
@synthesize dohide;


char head[12]="<html><body>",tail[14]="</body></html>";
int readInt(FILE *fp){
	int a=0,i;
	char c[4];
	for(i=0;i<4;i++)fscanf(fp,"%c",&c[i]);
	a=(c[3]+0x100)&0xff;
	a+=((c[2]+0x100)&0xff)<<8;
	a+=((c[1]+0x100)&0xff)<<16;
	a+=((c[0]+0x100)&0xff)<<24;
	return a;
}


-(IBAction) pgoset:(id)sender{
	[self hide:nil];
	[self.tv setHidden:NO];
	[self.pok setHidden:NO];
}

-(IBAction) hide:(id)sender{
	[self.tv2 setHidden:YES];
	[self.dohide setHidden:YES];
}

int readShort(FILE *fp){
	int a=0;
	char c[2];
	c[0]=fgetc(fp);
	if(feof(fp))return -1;
	c[1]=fgetc(fp);
	a=(c[1]+0x100)&0xff;
	a+=((c[0]+0x100)&0xff)<<8;
	return a;
}

void readUTF(FILE *fp,char* buf){
	int a=0,i;
	char c[2];
	for(i=0;i<2;i++)fscanf(fp,"%c",&c[i]);
	a=(c[1]+0x100)&0xff;
	a+=((c[0]+0x100)&0xff)<<8;
	for(i=0;i<a;i++)buf[i]=fgetc(fp);
	buf[a]=0;
}



void notFound(){
	//busy=NO;
	UIAlertView *a=[[UIAlertView alloc] initWithTitle:@"Not Found" message:@"This word does not exist." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil,nil];
	[a show];
	[a release];
}

-(IBAction) done:(id)sender{
	[self search:nil];
}

-(IBAction) tchange:(id)sender{
	if([tb.text compare:@""]==0)return;
	rs=YES;
	re=YES;
	//for(int i=0;i<tsn;i++)[tsc[i] release];
	tsn=0;
	NSString *ts=tb.text,*thisword;
	int i,s1;
	char buf1[300];
	for(int x1=0;x1<dn;x1++){
		int x=dnpx[x1];
		FILE *fp=fopen(dfn[x],"rb");
		int lp=0,rp=defTotal[x]-1,mp;
		while(lp<=rp){
			mp=(lp+rp)/2;
			fseek(fp, p1[x]+mp*4, SEEK_SET);
			s1=readInt(fp);
			fseek(fp,p2[x]+s1,SEEK_SET);
			readUTF(fp, buf1);
			thisword=[[NSString stringWithUTF8String:buf1] lowercaseString];
			i=[ts compare:thisword];
			if(i==0){break;lp=rp=mp;}
			if(i<0)rp=mp-1;
			if(i>0)lp=mp+1;
		}
		if(i==0)goto aa;
		mp=lp;
		if(mp!=defTotal[x])re=NO;
		else mp--;
		while(mp>0&&[ts compare:thisword]<0){
			mp--;
			fseek(fp, p1[x]+mp*4, SEEK_SET);
			s1=readInt(fp);
			fseek(fp,p2[x]+s1,SEEK_SET);
			readUTF(fp, buf1);
			thisword=[[NSString stringWithUTF8String:buf1] lowercaseString];
		}
		//if([ts compare:thisword])mp++;
		if(mp!=0)rs=NO;
	aa:
		ttsn[x]=0;
		for(i=0;i<50;i++){
			if(mp==defTotal[x])break;
			fseek(fp, p1[x]+mp*4, SEEK_SET);
			s1=readInt(fp);
			fseek(fp,p2[x]+s1,SEEK_SET);
			readUTF(fp, buf1);
			thisword=[[NSString stringWithUTF8String:buf1] lowercaseString];
			tts[x][i]=thisword;
			ttsn[x]++;
			mp++;
		}
		tpos[x]=0;
		fclose(fp);
nxt:;
	}
	int bi;
	for(int i=0;i<50;i++){
		NSString *tttsc=@"zzzzzzzzzzzzz";
		bi=-1;
		for(int j=0;j<dn;j++){
			if(ttsn[j]==tpos[j])continue;
			if([tts[j][tpos[j]] compare:tttsc]<0){
				tttsc=tts[j][tpos[j]];
				bi=j;
			}
		}
		if(bi==-1)break;
		tpos[bi]++;
		strcpy(tsc[i],[tttsc UTF8String]);
		tsn++;
		for(int j=0;j<dn;j++)
			while(ttsn[j]!=tpos[j]&&[tttsc compare:tts[j][tpos[j]]]==0)
				tpos[j]++;
	}
	[self.tv2 setHidden:NO];
	[self.dohide setHidden:NO];
	[self.tv2 reloadData];
}

-(IBAction) pdone:(id)sender{
	NSURL *u=[[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
	NSString *ts=[[u URLByAppendingPathComponent:@"sx.dat"] path];
	FILE *fp=fopen([ts UTF8String],"w");
	fprintf(fp,"%d\n",dn);
	for(int i=0;i<dn;i++)fprintf(fp,"%s\n",[dictName[i] UTF8String]);
	for(int i=0;i<dn;i++)fprintf(fp,"%d\n",dnpx[i]);
	fclose(fp);
	[self.tv setHidden:YES];
	[self.pok setHidden:YES];
}

-(IBAction) search:(id)sender{
	NSString *ts;
	int ol=0;
	[self hide:nil];
	for(int i=0;i<12;i++)outer[ol++]=head[i];
	[tb resignFirstResponder];
	ts=[tb.text lowercaseString];
	if([tb.text compare:@""]==0)return;
	//if(busy)return;
	int i,s1,s4;
	char def[500000],buf1[300];
	busy=true;
	for(int x1=0;x1<dn;x1++){
		int x=dnpx[x1];
		FILE *fp=fopen(dfn[x],"rb");
		int lp=0,rp=defTotal[x]-1,mp;
		while(lp<=rp){
			mp=(lp+rp)/2;
			fseek(fp, p1[x]+mp*4, SEEK_SET);
			s1=readInt(fp);
			fseek(fp,p2[x]+s1,SEEK_SET);
			readUTF(fp, buf1);
			NSString *thisword=[[NSString stringWithUTF8String:buf1] lowercaseString];
			i=[ts compare:thisword];
			if(i==0){lp=rp=mp;break;}
			if(i<0)rp=mp-1;
			if(i>0)lp=mp+1;
		}
		if(i==0){
			s1=readInt(fp);
			s4=readInt(fp);
			fseek(fp, p3[x]+s1, SEEK_SET);
			fread(def, 1, s4,fp);
			def[s4]=0;
			NSString *dt=[NSString stringWithUTF8String:def];
			NSString *dt2=[NSString stringWithFormat:@"<B>%@</B><br/>%@<br/><br/>",dictName[x],dt];
			const char *dtc=[dt2 UTF8String];
			int l=strlen(dtc);
			for(i=0;i<l;i++)outer[ol++]=dtc[i];
			fclose(fp);
			goto nxt;
		}
		fclose(fp);
	nxt:;
	}
	if(ol<=12){
		busy=NO;
		notFound();
		return;
	}
	for(int i=0;i<14;i++)outer[ol++]=tail[i];
	outer[ol]=0;
	NSData *dd=[NSData dataWithBytes:outer length:ol];
	[web loadData:dd MIMEType:@"text/html" textEncodingName:@"UTF-8" baseURL:nil];
	busy=false;
}


/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
 */
- (void)ndict:(char*)file{
	strcpy(dfn[dn],file);
	FILE *fp=fopen(file,"rb");
	char buf[100000];
	for(int i=0;i<4;i++)fscanf(fp,"%c",&buf[0]);
	defTotal[dn]=readInt(fp);
	readUTF(fp, buf);
	dictName[dn]=[NSString stringWithUTF8String:buf];
	[dictName[dn] retain];
	p1[dn]=readInt(fp);
	p2[dn]=readInt(fp);
	p3[dn]=readInt(fp);
	p4[dn]=readInt(fp);
	dnpx[dn]=dn;
	dn++;
	fclose(fp);
}
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	dn=0;
	tsn=0;
	NSFileManager *fileManager = [NSFileManager defaultManager];
	NSArray *documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentDir = [documentPaths objectAtIndex:0];
	NSError *error = nil;
	NSArray *fileList ;
	fileList = [fileManager contentsOfDirectoryAtPath:documentDir error:&error];
	for (NSString *file in fileList) {
		NSString *path = [documentDir stringByAppendingPathComponent:file];
		char *tc=(char*)[path UTF8String];
		int tcl=strlen(tc);
		if(tcl>3&&(tc[tcl-1]=='d'||tc[tcl-1]=='D')&&(tc[tcl-2]=='d'||tc[tcl-2]=='D')&&tc[tcl-3]=='.'){
			isdoc[dn]=YES;
			[self ndict:tc];
		}
	}
	documentDir=[[NSBundle mainBundle] resourcePath];
	fileList = [fileManager contentsOfDirectoryAtPath:documentDir error:&error];
	for (NSString *file in fileList) {
		NSString *path = [documentDir stringByAppendingPathComponent:file];
		char *tc=(char*)[path UTF8String];
		int tcl=strlen(tc);
		if(tcl>3&&(tc[tcl-1]=='d'||tc[tcl-1]=='D')&&(tc[tcl-2]=='d'||tc[tcl-2]=='D')&&tc[tcl-3]=='.'){
			isdoc[dn]=NO;
			[self ndict:tc];
		}
	}
	
	[tv setDelegate:self];
	[tv setDataSource:self];
	[tv2 setDelegate:self];
	[tv2 setDataSource:self];
	[tv setEditing:YES animated:YES];
	NSURL *u=[[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
	NSString *ts=[[u URLByAppendingPathComponent:@"sx.dat"] path];
	FILE *fp=fopen([ts UTF8String],"r");
	if(fp==NULL)goto ne;
	int tdn;
	char tc[1000];
	fscanf(fp,"%d\n",&tdn);
	if(tdn!=dn)goto ne;
	for(int i=0;i<dn;i++){
		fgets(tc, 1000, fp);
		if(tc[strlen(tc)-1]=='\n')tc[strlen(tc)-1]=0;
		NSLog(@"%@ %@",dictName[dnpx[i]],[NSString stringWithUTF8String:tc]);
		if([dictName[dnpx[i]] compare:[NSString stringWithUTF8String:tc]]!=0)goto ne;
	}
	for(int i=0;i<dn;i++)fscanf(fp,"%d\n",&dnpx[i]);
	fclose(fp);
	goto neout;
ne:
	[self.tv setHidden:NO];
	[self.pok setHidden:NO];
neout:
	[ok setTitle:@"Search" forState:UIControlStateNormal];
	ok.enabled=YES;
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath{
	return UITableViewCellEditingStyleNone;
}
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
	if(tableView==tv)return YES;
	return NO;
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	[tableView deselectRowAtIndexPath:indexPath animated:YES];
	tb.text=[NSString stringWithUTF8String: tsc[indexPath.row]];
	[self search:nil];
}
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)sourceIndexPath toIndexPath:(NSIndexPath *)destinationIndexPath {
	int fr=(int)[sourceIndexPath row],tr=(int)[destinationIndexPath row];
	if(fr<tr){
		int t=dnpx[fr];
		for(int i=fr;i<tr;i++)dnpx[i]=dnpx[i+1];
		dnpx[tr]=t;
	}else if(fr>tr){
		int t=dnpx[fr];
		for(int i=fr;i>tr;i--)dnpx[i]=dnpx[i-1];
		dnpx[tr]=t;
	}/*
	id object=[tableView objectAtIndex:fr];
	[list removeObjectAtIndex:fr];
	[list insertObject:objext atIndex:tr];*/
}

/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
    [super dealloc];
	[tb release];
	[web release];
	[ok release];
	for(int i=0;i<dn;i++)[dictName[i] release];
}

- (NSInteger)tableView:(UITableView *)tableview numberOfRowsInSection:(NSInteger)section {
	if(tableview==tv){
		return dn;
	}else{
		return tsn;
	}
}

- (UITableViewCell *)tableView:(UITableView *)tableview cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	if(tableview==tv){
		static NSString *sti=@"cell";
		UITableViewCell *cell=[tableview dequeueReusableCellWithIdentifier:sti];
		if(cell==nil){
			cell=[[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:sti] autorelease];
		}
		cell.textLabel.text=dictName[dnpx[indexPath.row]];
		return cell;
	}else{
		static NSString *sti=@"cell2";
		UITableViewCell *cell=[tableview dequeueReusableCellWithIdentifier:sti];
		if(cell==nil){
			cell=[[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:sti] autorelease];
		}
		cell.textLabel.text=[NSString stringWithUTF8String:tsc[indexPath.row]];
		return cell;
	}
}

@end
