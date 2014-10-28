//import com.saphum.midp.zip.InflaterInputStream;
import gr.fire.browser.Browser;
import gr.fire.browser.util.Page;
import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.FireScreen;
import gr.fire.core.Panel;
import gr.fire.ui.FireTheme;
import gr.fire.util.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import java.io.IOException;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class DangoDictMidlet extends MIDlet implements CommandListener
{
	private Command exit,cha,chaok,back;
	private Browser b;
        private Form f1;
        private TextBox tb;
        private FireScreen screen;
        private String dictName;
        private String[] startWord;
        private int defTotal,pages;
        private Panel panel=null;
	public DangoDictMidlet()
	{

		// initialize fire screen
		screen = FireScreen.getScreen(Display.getDisplay(this));
		screen.setFullScreenMode(true); // on full screen mode
		try
		{
			// load a theme file.
			FireScreen.setTheme(new FireTheme("file://theme.properties"));
		} catch (IOException e)
		{
		}
		// initialize a browser instance
		b = new Browser();
		exit = new Command("Exit",Command.EXIT,2);
                cha=new Command("Search",Command.OK,1);
                f1=new Form("Loading");
                f1.append("Initializing dictionary data...");
		try
		{
			// use the browser to load a page from the jar
			Page p = b.loadPage("file://help.html",HttpConnection.GET,null,null);
			// create a panel to display that page
			panel = new Panel(p.getPageContainer(),Panel.HORIZONTAL_SCROLLBAR|Panel.VERTICAL_SCROLLBAR,true);
			panel.setCommandListener(this); // listen for events on this panel
			panel.setLeftSoftKeyCommand(exit); // such as an exit softkey
			panel.setRightSoftKeyCommand(cha);
			panel.setDragScroll(true); // This enables the Drag scroll function for this Panel.
			panel.setLabel(p.getPageTitle()); // The html page has a title tag, display it as a label on the panel
			screen.setCurrent(f1);
			DataInputStream dis=new DataInputStream(this.getClass().getResourceAsStream("/1.dd0"));
			dis.skip(4);
			defTotal=dis.readInt();
			dictName=dis.readUTF();
			dis.close();
			dis=new DataInputStream(this.getClass().getResourceAsStream("/1.ddp"));
			pages=dis.readInt();
			startWord=new String[pages+1];
			dis.close();
			panel.setLabel(dictName+"("+defTotal+"Words)");
			for(int i=0;i<=pages;i++){
				dis=new DataInputStream(this.getClass().getResourceAsStream("/1.dd"+i));
				if(i==0){
					dis.skip(8);
					dis.readUTF();
				}
				dis.skip(6);
				startWord[i]=dis.readUTF();
				dis.close();
			}
		} catch (Exception e)
		{
			// Use the Log class of the fire utility classes to easily log errors.
			// Check the BrowserTest.java application and the javadoc for more info on
			// the Log class and the Logger interface.
			Log.logError("Failed to load Pane.",e);
		}
		screen.setCurrent(panel); // show the panel on the screen.
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException
	{
		FireScreen.getScreen().destroy();
	}

	protected void pauseApp()
	{
	}

	protected void startApp() throws MIDletStateChangeException
	{
            screen.setCurrent(panel);
	}

	public void commandAction(javax.microedition.lcdui.Command c, Displayable cmp){
		System.out.println(c.getLabel());
		if(c==chaok){
			if(tb.getString()==null||tb.getString()=="")return;
			int i,s1,s2,s4,lob;
			byte[] def,ob=new byte[10000];
			String s3,ts=tb.getString().toLowerCase();
			for(i=0;i<=pages;i++){
				if(ts.compareTo(startWord[i])<0)break;
			}
			i--;
			if(i!=-1){
				DataInputStream dis=new DataInputStream(this.getClass().getResourceAsStream("/1.dd"+i));
				try{
					if(i==0){
						dis.skip(8);
						dis.readUTF();
					}
				}catch(Exception e){

				}
				try{
					while(true){
					   s1=(int)dis.readShort();
					   s2=(int)dis.readShort();
					   s4=(int)dis.readShort();
					   s3=dis.readUTF().toLowerCase();
					   i=ts.compareTo(s3);
					   if(i<0){
						   notFound();
						   return;
					   }
					   if(i==0){
						   dis.close();
						   dis=new DataInputStream(this.getClass().getResourceAsStream("/"+s1+".ddf"));
						   dis.skip(s2);
						   def=new byte[s4];
						   dis.read(def, 0, s4);
						   ByteArrayInputStream bais=new ByteArrayInputStream(def,0,s4);
						   /*InflaterInputStream iis=new InflaterInputStream(bais,s4,false);
						   lob=iis.read(ob);
						   iis.close();
						   bais.close();
						   iis=null;
						   bais=null;*/
						   //String fs=new String(def,0,s4,"UTF-8");
						   //System.out.println(fs);
						   Page p = b.loadPage(bais,"UTF-8");
							panel.set(p.getPageContainer());
	//panel.setLabel(p.getPageTitle()); // The html page has a title tag, display it as a label on the panel
							screen.setCurrent(panel);
						   return;
					   }
					}
				}catch(Exception e){
					System.out.println(e);
				}
				try{
				dis.close();
				}catch(Exception e){
				}
				notFound();
				return;
			}
			notFound();
			return;
		}
	}
	public void commandAction(javax.microedition.lcdui.Command c, Component cmp)
	{
		if(c==exit)
		{
			notifyDestroyed();
			return;
		}
		if(c==cha){
			chaok=new Command("OK",Command.OK,1);
			back=new Command("Back",Command.BACK,99);
			tb=new TextBox("Input a word","",50,TextField.ANY);
			tb.setCommandListener(this);
			tb.addCommand(chaok);
			tb.addCommand(back);
			screen.setCurrent(tb);
		}
	}
        private void notFound(){
            screen.setCurrent(new Alert("Error","This word does not exist.",null,AlertType.ERROR));
        }

}
