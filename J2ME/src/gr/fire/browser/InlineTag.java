/*
 * Fire is a fast, themable UI toolkit and xHTML/CSS renderer for mobile application 
 * and game development. It is an eye-candy alternative to the standard midp2 UI 
 * components and unlike them it produces a superior UI result on all mobile devices!
 *  
 * Copyright (C) 2006,2007,2008,2009,2010 Pashalis Padeleris (padeler at users.sourceforge.net)
 * 
 * This file is part of Fire.
 *
 * Fire is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fire.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * 
 */
package gr.fire.browser;

import gr.fire.browser.util.Command;
import gr.fire.browser.util.Form;
import gr.fire.browser.util.Page;
import gr.fire.browser.util.StyleSheet;
import gr.fire.core.FireScreen;
import gr.fire.core.Theme;
import gr.fire.ui.ImageComponent;
import gr.fire.ui.InputComponent;
import gr.fire.util.Log;
import gr.fire.util.StringUtil;

import java.io.InterruptedIOException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

import org.kxml2.io.KXmlParser;

/**
 * @author padeler
 *
 */
public class InlineTag extends Tag
{	
	public static final String TAG_A = "a";
	public static final String TAG_BR = "br";
	public static final String TAG_TD = "td";
	public static final String TAG_IMG = "img";
	public static final String TAG_SPAN = "span";
	public static final String TAG_B = "b";
	public static final String TAG_STRONG = "strong";
	public static final String TAG_I = "i";
	public static final String TAG_EM = "em";
	public static final String TAG_BIG = "big";
	public static final String TAG_SMALL= "small";
	public static final String TAG_TT= "tt";
	public static final String TAG_U= "u";
	public static final String TAG_CENTER= "center";
	
	
	
	public static final String TAG_INPUT = "input";
	public static final String TAG_LABEL= "label";
	public static final String TAG_SELECT = "select";
	public static final String TAG_OPTION = "option";
	public static final String TAG_BUTTON = "button";
	public static final String TAG_TEXTAREA= "textarea";
	
	
	private String name=TAG_NOTSET;
	
	
	public InlineTag()
	{
	}
	

	public String getName()
	{
		return name;
	}
	
	public void inherit(Tag parent)
	{
		if(parent!=null)
		{
			inheritStyle(parent); // inherit style information
			if(parent instanceof BlockTag) this.parentBlockTag = (BlockTag)parent;
			else this.parentBlockTag = parent.parentBlockTag;
		}
	}

	public void handleTagStart(Browser browser,Page page, KXmlParser parser)
	{
		name = parser.getName().toLowerCase(); // the name of the tag

		Tag parentElement = browser.topTag(); // may be a block element or an inline element
		
		inherit(parentElement); // first inherit from parents
		handleCommonAttributes(parser); // then get extra style from attributes

		if(TAG_A.equals(name))
		{
			// this is a link.
			String hrefLocation = parser.getAttributeValue(null,"href");
			if(hrefLocation!=null)
			{
				Theme th = FireScreen.getTheme();
				if(foregroundColor==th.getIntProperty("xhtml.fg.color")) // no change from style attribute, hack until DOM is implemented.
					foregroundColor = th.getIntProperty("link.fg.color");

				Font linkFont = th.getFontProperty("link.font");
				try{
					font = Font.getFont(font.getFace()|linkFont.getFace(),font.getStyle()|linkFont.getStyle(),font.getSize());
				}catch(Exception e)// failed to inherit font attributes.
				{font = linkFont;}

				listener = browser.listener;
				href = new Command("Link",Command.OK,1,hrefLocation);
			}
			return;
		}

		if(TAG_IMG.equals(name))
		{
			String imgLocation = parser.getAttributeValue(null,"src");
			String alt = parser.getAttributeValue(null,"alt");
			
			if(imgLocation!=null && parentBlockTag!=null)
			{
				int width= StyleSheet.calculateLenght(parser.getAttributeValue(null,"width"),parentBlockTag.containerWidth);
				int height=StyleSheet.calculateLenght(parser.getAttributeValue(null,"height"),FireScreen.getScreen().getHeight());
				
				Log.logDebug("Image Element ["+imgLocation+"]/["+alt+"]: "+width+","+height);
				
				imgLocation = browser.httpClient.getAbsolutEncodedURL(imgLocation);
				
				Image image = page.getCachedImage(imgLocation);
				if(image==null){
					image = browser.getCachedImage(imgLocation); // try last page cache
					if(image!=null) page.cacheImage(imgLocation, image);
				}
				
				ImageComponent primitive= new ImageComponent(image,width,height,this.font,alt);
				if(image==null && browser.imageLoadingPolicy!=Browser.NO_IMAGES)
				{
					if(browser.imageLoadingPolicy==Browser.LOAD_IMAGES_ASYNC && width!=-1 && height!=-1)
					{
						page.registerAsyncImageRequest(primitive,imgLocation);
					}
					else // LOAD_IMAGES or width,height are not preset.
					{
						try{
							image = browser.loadImage(imgLocation);
							if(image!=null) 
							{
								page.cacheImage(imgLocation,image);
								primitive.setImage(image);
							}
						}catch(InterruptedIOException e){
							Log.logInfo("User canceled synchronous image load from "+imgLocation);
						}
					}
				}
				copyStyle(primitive);
				parentBlockTag.handleComponent(this, primitive);
			}
			return;
		}
		
		if(TAG_BR.equals(name))
		{
			if(parentBlockTag!=null)
			{
				parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),true);
			}
			return;
		}
		
		if(TAG_SPAN.equals(name))
		{
			handleCommonAttributes(parser); // then get extra style from attributes
			handleAlignAttributes(parser);
			return;
		}
		if(TAG_TD.equals(name))
		{
			handleCommonAttributes(parser); // then get extra style from attributes
			//handleAlignAttributes(parser);
			return;
		}
		
		
		if(TAG_B.equals(name) || TAG_STRONG.equals(name))
		{
			/*
			 * This is a B Inline Element. 
			 * As an inline Element i am inside a container. 
			 */
			font = Font.getFont(font.getFace(),font.getStyle()|Font.STYLE_BOLD,font.getSize());
			return;
		}
		
		if(TAG_I.equals(name) || TAG_EM.equals(name))
		{
			/*
			 * This is a I Inline Element. 
			 * As an inline Element i am inside a container. 
			 */
			font = Font.getFont(font.getFace(),font.getStyle()|Font.STYLE_ITALIC,font.getSize());
			return;
		}
		
		
		if(TAG_LABEL.equals(name))
		{
			
			// nothing to do for labels (ignore "for" attribute until proper DOM is implemented).
			return;
		}
		
		if(TAG_INPUT.equals(name) || TAG_BUTTON.equals(name))
		{
			Form openForm = page.getOpenForm();  
			if(openForm==null || parentBlockTag==null) // ignore input elements outside forms
			{
				return;
			}
			
			handleInputComponent(openForm,parser);
			
			return;
		}
		
		if(TAG_SELECT.equals(name))
		{
			Form openForm = page.getOpenForm();  
			if(openForm==null || parentBlockTag==null) // ignore input elements outside forms
			{
				return;
			}
						
			boolean multiple = parser.getAttributeValue(null,"multiple")!=null;
			boolean enabled = (parser.getAttributeValue(null,"disabled")==null);
			String name = parser.getAttributeValue(null,"name");
			gr.fire.browser.util.Command menuCommand = new gr.fire.browser.util.Command(name);
			menuCommand.setEnabled(enabled);
			menuCommand.setMultiple(multiple);
			
			String size = parser.getAttributeValue(null,"size");
			if(size!=null)
			{
				try{
					menuCommand.setSize(Integer.parseInt(size));
				}catch(NumberFormatException e)
				{
					Log.logWarn("Failed to parse size attribute",e);
				}
			}
			openForm.setMenuCommand(menuCommand);
			return;
		}
		
		if(TAG_OPTION.equals(name))
		{
			Form openForm = page.getOpenForm();  
			if(openForm==null || parentBlockTag==null) // ignore input elements outside forms
			{
				return;
			}
			
			gr.fire.browser.util.Command menu = openForm.getMenuCommand();
			String name=null;
			if(menu!=null) name = menu.getName();
			
			boolean enabled = (parser.getAttributeValue(null,"disabled")==null);
			boolean checked = (parser.getAttributeValue(null,"selected")!=null);
			
			String value= parser.getAttributeValue(null,"value");  
			
			// get the text of the option element
			int type;
			String text= "";
			try
			{
				type = parser.next();
				if(type==KXmlParser.TEXT) // text of option element
				{
					text = parser.getText();
					parser.next(); //progress the parsing once more since TEXT was handled here.
				}
			} catch (Exception e)
			{
				Log.logWarn("Failed to get text for tag "+name+".",e);
				return;
			}

			InputComponent option = new InputComponent(InputComponent.SWITCH);
			option.setLayout(FireScreen.VCENTER|FireScreen.CENTER);
			option.setChecked(checked);
			option.setEnabled(enabled);
			option.setName(name);
			option.setValue(value);
			option.setInitialValue(checked?"":null);
			option.setText(text);
			option.setMaxWidth(parentBlockTag.getContainerWidth());
			
			copyStyle(option);
			int []minSize = option.getMinSize();
			option.setPrefSize(minSize[0],minSize[1]);
			
			openForm.addInputComponent(option);
			return;
		}
		
		
		if(TAG_TEXTAREA.equals(name))
		{
			Form openForm = page.getOpenForm();  
			if(openForm==null || parentBlockTag==null) // ignore input elements outside forms
			{
				return;
			}
			
			String name = parser.getAttributeValue(null,"name");
			boolean enabled = (parser.getAttributeValue(null,"disabled")==null) && (parser.getAttributeValue(null,"readonly")==null);

			String rowsStr = parser.getAttributeValue(null,"rows");
			String colsStr = parser.getAttributeValue(null,"cols");
			String sizeStr = parser.getAttributeValue(null,"size");
			
			// get the text of this area
			int type;
			String value= "";
			try
			{
				type = parser.next();
				if(type==KXmlParser.TEXT) // text of text area
				{
					value = StringUtil.proccessUrl(parser.getText(),true);
					parser.next(); //progress the parsing once more since TEXT was handled here.
				}
			} catch (Exception e)
			{
				Log.logWarn("Failed to get text for tag "+name+".",e);
				return;
			}
			
			// now add the textarea to the container.
			InputComponent textarea = new InputComponent(InputComponent.TEXT);
			
			if(sizeStr!=null)
			{
				try{
					int s = Integer.parseInt(sizeStr);
					textarea.setMaxLen(s);
				}catch(Exception e){}
			}
			
			textarea.setName(name);
			textarea.setValue(value);
			textarea.setInitialValue(value);
			textarea.setEnabled(enabled);
			

			textarea.setMaxWidth(parentBlockTag.getContainerWidth());

			if(rowsStr!=null)
			{
				try{textarea.setRows(Integer.parseInt(rowsStr.trim()));}catch(NumberFormatException e){
					Log.logWarn("Failed to parse textarea rows number "+rowsStr,e);
				}
			}
			else textarea.setRows(5);// default.

			
			
			if(colsStr!=null)
			{
				try{textarea.setSize(Integer.parseInt(colsStr.trim()));}catch(NumberFormatException e){
					Log.logWarn("Failed to parse textarea cols number "+colsStr,e);
				}
			}
			textarea.setSize(2000);// well its a phone... 2k characters should be enough :)
			
			copyStyle(textarea);
			textarea.setBackgroundColor(FireScreen.getTheme().getIntProperty("bg.color"));
			int []minSize = textarea.getMinSize();
			textarea.setPrefSize(minSize[0],minSize[1]);

			
			openForm.addInputComponent(textarea);
			
			parentBlockTag.handleComponent(this,textarea);
			
			return;
		}
		
		if(TAG_BIG.equals(name))
		{
			int s = font.getSize();
			if(s==Font.SIZE_MEDIUM) s = Font.SIZE_LARGE;
			else if(s==Font.SIZE_SMALL) s = Font.SIZE_MEDIUM;
			
			font = Font.getFont(font.getFace(),font.getStyle(),s);
			return;
		}
		
		if(TAG_CENTER.equals(name))
		{
			layout |= FireScreen.CENTER;
			return;
		}

		if(TAG_SMALL.equals(name))
		{
			int s = font.getSize();
			if(s==Font.SIZE_MEDIUM) s = Font.SIZE_SMALL;
			else if(s==Font.SIZE_LARGE) s = Font.SIZE_MEDIUM;
			font = Font.getFont(font.getFace(),font.getStyle(),s);
			return;
		}
		
		if(TAG_TT.equals(name))
		{
			font = Font.getFont(Font.FACE_MONOSPACE,font.getStyle(),font.getSize());
			return;
		}
		
		if(TAG_U.equals(name))
		{
			font = Font.getFont(font.getFace(),font.getStyle()|Font.STYLE_UNDERLINED,font.getSize());
			return;
		}
	}
	
	public void handleTagEnd(Browser browser,Page page, KXmlParser parser)
	{
		
		if(TAG_SELECT.equals(name))
		{
			Form openForm = page.getOpenForm();  
			if(openForm==null || parentBlockTag==null) // ignore input elements outside forms
			{
				return;
			}
			gr.fire.browser.util.Command menu = openForm.getMenuCommand();
			if(menu==null) // error
			{
				Log.logWarn("Found closing </select> tag but i dont remember one opening.");
				return;
			}
			
			InputComponent menuSwitch = new InputComponent(InputComponent.MENU);
			String menuName = menu.getName();
			menuSwitch.setName(menuName);
			menuSwitch.setEnabled(menu.isEnabled());
			menuSwitch.setLayout(FireScreen.VCENTER|FireScreen.CENTER);
			menuSwitch.setMaxWidth(parentBlockTag.getContainerWidth());
			copyStyle(menuSwitch);

			String text = " ... ";
			int menuWidth=font.stringWidth(text);
			int menuHeight=font.getHeight();
			if(menuName!=null)
			{
				Vector primitivesVector = openForm.getPrimitivesVector();
				
				for(int i=0;i<primitivesVector.size();++i)
				{
					InputComponent p = (InputComponent)primitivesVector.elementAt(i);
					if(menuName.equals(p.getName()))
					{ // add to panel
						int[] ms  = p.getPrefSize();
						if(ms[0]>menuWidth) menuWidth=ms[0];
						if(ms[1]>menuHeight) menuHeight=ms[1];					
						if(p.isChecked() && menu.isMultiple()==false) text = p.getText(); // set the value of the selected item
					}
				}				
			}
			
			menuSwitch.setText(text);
			menuSwitch.setPrefSize(menuWidth,menuHeight);
			openForm.addInputComponent(menuSwitch);
			
			openForm.setMenuCommand(null);
			parentBlockTag.handleComponent(this,menuSwitch);
			return;
		}
	}

	private void handleInputComponent(Form form,KXmlParser parser)
	{
		/*
		 * Input primitive can be
		 * "text" | "password" | "checkbox" | "radio" | "submit" | "reset" | "hidden" | 
		 * "button" | "phonenumber" | "url" | "numeric" | "decimal" | "email" |
		 */
		String type = parser.getAttributeValue(null,"type");
		String name = parser.getAttributeValue(null,"name");
		String value= parser.getAttributeValue(null,"value");
		boolean checked = (parser.getAttributeValue(null,"checked")!=null);
		boolean enabled = (parser.getAttributeValue(null,"disabled")==null) && (parser.getAttributeValue(null,"readonly")==null);
		
		if(value!=null) value = StringUtil.proccessUrl(value,true);
		
		InputComponent input = null;
		boolean setBackgroundColor = false;
		
		if(type==null) type="text"; // default
		else type = type.toLowerCase();
		
		if(type.equals("text"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}else if(type.equals("password"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setTextConstraints(TextField.PASSWORD);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}
		else if(type.equals("checkbox"))
		{
			input=new InputComponent(InputComponent.CHECKBOX);
			input.setInitialValue(checked?"":null);
			setBackgroundColor=true;
			input.setChecked(checked);
		}
		else if(type.equals("radio"))	
		{
			input=new InputComponent(InputComponent.RADIO);
			input.setInitialValue(checked?"":null);
			setBackgroundColor=true;
			input.setChecked(checked);
		}
		else if(type.equals("submit"))
		{
			input=new InputComponent(InputComponent.SUBMIT);
			input.setLayout(FireScreen.VCENTER|FireScreen.CENTER);
			if(value==null) value="submit";
		}
		else if(type.equals("reset"))
		{
			input=new InputComponent(InputComponent.RESET);
			input.setLayout(FireScreen.VCENTER|FireScreen.CENTER);
			if(value==null) value="reset";

		}
		else if(type.equals("button"))
		{
			input=new InputComponent(InputComponent.BUTTON);
			input.setLayout(FireScreen.VCENTER|FireScreen.CENTER);
			if(value==null) value="";

		}
		else if(type.equals("hidden"))
		{
			input=new InputComponent(InputComponent.HIDDEN);
		}
		else if(type.equals("phonenumber"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setTextConstraints(TextField.PHONENUMBER);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}
		else if(type.equals("url"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setTextConstraints(TextField.URL);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}
		else if(type.equals("email"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setTextConstraints(TextField.EMAILADDR);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}
		else if(type.equals("numeric"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setTextConstraints(TextField.NUMERIC);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}
		else if(type.equals("decimal"))
		{
			input=new InputComponent(InputComponent.TEXT);
			input.setTextConstraints(TextField.DECIMAL);
			input.setInitialValue(value);
			setBackgroundColor=true;// set textfield to default bg color
		}
		else // any other types are not supported
		{
			return;
		}

		input.setEnabled(enabled);
		input.setMaxWidth(parentBlockTag.getContainerWidth());

		String size = parser.getAttributeValue(null,"size");
		if(size!=null)
		{
			try{
				input.setSize(Integer.parseInt(size));
			}catch(NumberFormatException e)
			{
				Log.logWarn("Failed to parse size attribute",e);
			}
		}
		String maxLen = parser.getAttributeValue(null,"maxlength");
		if(maxLen!=null)
		{
			try{
				input.setMaxLen(Integer.parseInt(maxLen));
			}catch(NumberFormatException e)
			{
				Log.logWarn("Failed to parse maxlength attribute",e);
			}
		}
		
		input.setName(name);
		input.setValue(value);
		
		copyStyle(input);

		if(setBackgroundColor)
			input.setBackgroundColor(FireScreen.getTheme().getIntProperty("bg.alt1.color"));

		int []minSize = input.getMinSize();
		input.setPrefSize(minSize[0],minSize[1]);
		
		form.addInputComponent(input);
		// add the input primitive to the container.
		if(input.getType()!=InputComponent.HIDDEN)
		{ 
			parentBlockTag.handleComponent(this,input);
		}
	}

	public void handleText(Tag topLevelTag, String txt)
	{
		// The text will be handled by my parent block element.
		if(parentBlockTag==null)
		{
			Log.logWarn("Cannot handle text outside a Block element");
			Log.logWarn("Ignoring: "+txt);
			return;
		}
		parentBlockTag.handleText(topLevelTag,txt);
	}
}