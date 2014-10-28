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

import gr.fire.browser.util.Form;
import gr.fire.browser.util.Page;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.FireScreen;
import gr.fire.ui.ImageComponent;
import gr.fire.ui.TextComponent;
import gr.fire.util.Log;
import gr.fire.util.StringUtil;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.kxml2.io.KXmlParser;

/**
 * @author padeler
 *
 */
public class BlockTag extends Tag
{
	public static final String TAG_HTML = "html";
	public static final String TAG_HEAD = "head";
	public static final String TAG_TITLE = "title";
	public static final String TAG_META = "meta";
	public static final String TAG_BODY = "body";	
	public static final String TAG_STYLE = "style";
	public static final String TAG_P = "p";
	public static final String TAG_DIV = "div";
	public static final String TAG_HR = "hr";
	public static final String TAG_TABLE = "table";
	public static final String TAG_TR = "tr";
	public static final String TAG_H1 = "h1";
	public static final String TAG_H2 = "h2";
	public static final String TAG_H3 = "h3";
	public static final String TAG_H4 = "h4";
	public static final String TAG_H5 = "h5";
	public static final String TAG_H6 = "h6";
	
	public static final String TAG_SCRIPT = "script";
	public static final String TAG_FORM = "form";
	
	protected Container elementContainer;
	protected int containerWidth; // the width of this container. It is either inherited by the parent or set by attributes
		
	public BlockTag()
	{
		elementContainer = new Container();
		font = FireScreen.getTheme().getFontProperty("font");
	}
	
	public void inherit(Tag parent, boolean softLineBreak)
	{
		if(parent!=null)
		{
			inheritStyle(parent); // inherit style information
			if(parent instanceof BlockTag) this.parentBlockTag = (BlockTag)parent;
			else this.parentBlockTag = parent.parentBlockTag;
		}
		
		if(parentBlockTag!=null)
		{
			parentBlockTag.elementContainer.add(elementContainer);
			if(softLineBreak)
				parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),false);

			elementContainer.setX(parentBlockTag.getPointerX());
			elementContainer.setY(parentBlockTag.getBaseLine()-parentBlockTag.getLineHeight());
			containerWidth = parentBlockTag.getContainerWidth()-parentBlockTag.getPointerX();
		}
	}
	
	public void handleTagStart(Browser browser,Page page, KXmlParser parser)
	{
		String name = parser.getName().toLowerCase(); // the name of the tag
		setName(name);
		
		Tag parentElement = browser.topTag(); // may be a block element or an inline element
		
		if(TAG_P.equals(name))
		{
			/*
			 * This is a P Element. 
			 * As a container i will have a new line before and a new line after my content
			 */
			inherit(parentElement,true); // first inherit from parents
			handleCommonAttributes(parser); // then get extra style from attributes

			if(parentBlockTag!=null) parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),true); // paragraph starts with line break.
			
		}
		else if(TAG_DIV.equals(name))
		{
			inherit(parentElement,true); // first inherit from parents
			handleCommonAttributes(parser); // then get extra style from attributes
			handleAlignAttributes(parser);
		}
		else if(TAG_FORM.equals(name))
		{
			inherit(parentElement,true); // first inherit from parents
			handleCommonAttributes(parser); // then get extra style from attributes
			lineBreak(font.getHeight(),false);			

			String action = parser.getAttributeValue(null,"action");
			String method = parser.getAttributeValue(null,"method");
			String enctype = parser.getAttributeValue(null,"enctype");
			if(method==null || method.toLowerCase().toLowerCase().equals("get")) method = HttpConnection.GET;
			else method = HttpConnection.POST;
			
			Form newForm = new Form(browser,action,method,enctype);
			if(browser.listener!=null) newForm.setFormListener(browser.listener);
			// set the new form to the parent.
			page.setOpenForm(newForm);
			
		}
		else if(TAG_TABLE.equals(name))
		{
			inherit(parentElement,true); // first inherit from parents
			handleCommonAttributes(parser); // then get extra style from attributes
			handleAlignAttributes(parser);
			lineBreak(font.getHeight(),false);			
//			elementContainer.setLayoutManager(new BoxLayout(BoxLayout.Y_AXIS));
		}
		else if(TAG_TR.equals(name))
		{
			inherit(parentElement,true); // first inherit from parents
			handleCommonAttributes(parser); // then get extra style from attributes
			handleAlignAttributes(parser);
			lineBreak(font.getHeight(),true);			
//			elementContainer.setLayoutManager(new BoxLayout(BoxLayout.X_AXIS));
		}
		
		else if(TAG_H1.equals(name))
		{
			handleHeaderTagStart(parentElement,parser,Font.getFont(font.getFace(),Font.STYLE_BOLD,Font.SIZE_LARGE));
		}
		else if(TAG_H2.equals(name))
		{
			handleHeaderTagStart(parentElement,parser,Font.getFont(font.getFace(),Font.STYLE_BOLD|Font.STYLE_ITALIC,Font.SIZE_LARGE));
		}		
		else if(TAG_H3.equals(name))
		{
			handleHeaderTagStart(parentElement,parser,Font.getFont(font.getFace(),Font.STYLE_BOLD,Font.SIZE_MEDIUM));
		}
		else if(TAG_H4.equals(name))
		{
			handleHeaderTagStart(parentElement,parser,Font.getFont(font.getFace(),Font.STYLE_BOLD,Font.SIZE_SMALL));
		}
		else if(TAG_H5.equals(name) || TAG_H6.equals(name))
		{
			handleHeaderTagStart(parentElement,parser,Font.getFont(font.getFace(),Font.STYLE_PLAIN,Font.SIZE_MEDIUM));
		}
		else if(TAG_HR.equals(name))
		{
			/*
			 * This is a HR (Horizontal ruler) Element. 
			 * It will have a new line before and a new line after it.
			 */
			inherit(parentElement,true); // first inherit from parents
			handleCommonAttributes(parser); // then get extra style from attributes
			if(parentBlockTag!=null)
				parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),false);
			
			lineBreak(font.getHeight()/2,true); // insert a line break.			
		}
		else if(TAG_HTML.equals(name) || TAG_BODY.equals(name))
		{
			// body starts at the top left corner of the page.
			handleCommonAttributes(parser); // then get extra style from attributes
			handleColorAttributes(parser);
			
			containerWidth = browser.getViewportWidth();
			lineBreak(font.getHeight(),false);
			copyStyle(elementContainer);
			elementContainer.setId(tagId);
			//elementContainer.setBackgroundColor(Component.); // overide default transparent color. (should change when default CSS is implemented)
			page.setPageContainer(elementContainer);
			return;
		}
		else if(TAG_SCRIPT.equals(name) || TAG_STYLE.equals(name))
		{ // consume 
			Log.logDebug("<"+name+">");
			//Log.logDebug(parser.getText());
			Log.logDebug(".....Skipped.....");
			Log.logDebug("</"+name+">");
			return;
		}
		else if(TAG_META.equals(name))
		{
			page.parseMetaTag(parser);
			return;
		}
		else if(TAG_TITLE.equals(name))
		{
			try{
				int type = parser.next();
				if(type==KXmlParser.TEXT)
				{
					page.setPageTitle(parser.getText().trim());
					parser.next(); // progress the parsing once more since TEXT was handled here.
				}
			}catch(Exception e){
				Log.logWarn("Failed to parse page title.",e);
			}
			return;
		}
		
		copyStyle(elementContainer);
		elementContainer.setId(tagId);
	}

	public void handleTagEnd(Browser browser,Page page, KXmlParser parser)
	{
		String name = getName();

		if(TAG_P.equals(name))
		{
			
			int baseLine =getBaseLine();
			/*
			 * This the end of a P Element. 
			 * As a container i will have a new line before and a new line after my content
			 */
			if(parentBlockTag!=null)
			{// update pointer position of parent
				parentBlockTag.increaseBaseLine(baseLine-parentBlockTag.getLineHeight());
				parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),true);
			}
			
			elementContainer.setPrefSize(containerWidth,baseLine);
			
			return;
		}
		
		if( TAG_H1.equals(name) || TAG_H2.equals(name) || TAG_H3.equals(name) || TAG_H4.equals(name) || TAG_H5.equals(name) || TAG_H6.equals(name))
		{
			lineBreak(font.getHeight(),true);
			if(parentBlockTag!=null)
			{// update pointer position of parent
				parentBlockTag.increaseBaseLine(getBaseLine()-parentBlockTag.getLineHeight());
				parentBlockTag.pointerX += containerWidth;
			}
			elementContainer.setPrefSize(containerWidth,getBaseLine());
			return;
		}
		
		if(TAG_DIV.equals(name))
		{
			int baseLine = getBaseLine();
			if(parentBlockTag!=null)
			{// update pointer position of parent
				int lineHeight = getLineHeight();
				if(pointerX==0 && lineHeight>0) baseLine -= lineHeight; // ignore last empty line break.
				parentBlockTag.increaseBaseLine(baseLine-parentBlockTag.getLineHeight());
				parentBlockTag.pointerX += containerWidth;
			}
			elementContainer.setPrefSize(containerWidth,baseLine);
			return;			
		}
		
		if(TAG_TABLE.equals(name) || TAG_TR.equals(name))
		{
			if(parentBlockTag!=null)
			{// update pointer position of parent
				parentBlockTag.increaseBaseLine(getBaseLine()-parentBlockTag.getLineHeight());
				parentBlockTag.pointerX += containerWidth;
			}
			elementContainer.setPrefSize(containerWidth,getBaseLine());
			return;
		}
		
		if(TAG_FORM.equals(name))
		{
			int baseLine = getBaseLine();			
			if(parentBlockTag!=null)
			{// update pointer position of parent
				parentBlockTag.increaseBaseLine(baseLine-parentBlockTag.getLineHeight());
				parentBlockTag.pointerX += containerWidth;
			}
			elementContainer.setPrefSize(containerWidth,baseLine);
			page.setOpenForm(null);
			return;
		}
		
		if(TAG_HR.equals(name))
		{
			// prepare the hr image
			if(containerWidth>3) //if container width is less than 3 pixels. skip hr.
			{
				Image hrImage = Image.createImage(containerWidth,2);
				Graphics g = hrImage.getGraphics();
				g.setColor(foregroundColor);
				g.drawLine(0,0,containerWidth-2,0);
				ImageComponent imCmp = new ImageComponent(hrImage,containerWidth,2,this.font,"");
				copyStyle(imCmp);
				handleComponent(this,imCmp); // add the hr image in the container.
				lineBreak(2,true);
		
				int baseLine = getBaseLine();
				if(parentBlockTag!=null)
				{// update pointer position of parent
					
					parentBlockTag.increaseBaseLine(baseLine-parentBlockTag.getLineHeight());
					parentBlockTag.pointerX += containerWidth;
				}
				elementContainer.setPrefSize(containerWidth,baseLine);
			}
			return;
		}
		
		if(TAG_BODY.equals(name) || TAG_HTML.equals(name))
		{
			elementContainer.setPrefSize(containerWidth,getBaseLine());
			return;
		}
	}
	
	
	private void handleHeaderTagStart(Tag parentElement,KXmlParser parser, Font font)
	{
		inherit(parentElement,true); // first inherit from parents
		handleCommonAttributes(parser); // then get extra style from attributes
		handleAlignAttributes(parser);
		this.font = font;
		lineBreak(font.getHeight(),false);
		lineBreak(font.getHeight(),true);
	}
	
	public void handleText(Tag topLevelTag, String txt)
	{
		
		// add a text element 
		if(pointerX==0 || !(this.getName().equals(TAG_P) || this.getName().equals(TAG_TR)))// || this.getName().equals(TAG_DIV)|| this.getName().equals(TAG_FORM)) // start of the line for this element. 
			txt = StringUtil.trimStart(txt);
		
		if(txt.length()==0) // ignore empty strings outside a paragraph.
			return;
		
		int vw = containerWidth;
		int sw = FireScreen.getScreen().getWidth();
		if(vw>sw) vw=sw; 
		
		TextComponent el = new TextComponent(txt,vw,pointerX);
		topLevelTag.copyStyle(el);
		el.validate();

		int lineCount = el.getFormatedText().size();
		if(lineCount==0) return;

		int fontHeight = topLevelTag.font.getHeight();
		// update other primitives on the same line to have the correct height.
		updatePrimitivesInLineForPrimitiveHeight(fontHeight);

		
		el.setX(0); // start from the left side of the block element
		el.setY(getBaseLine()-fontHeight);

		int lastLineWidth = el.getLastLineWidth();
		int contentHeight = el.getContentHeight();

//		el.setPrefSize(vw,contentHeight);
		elementContainer.add(el);
		
		if(lineCount==1)
		{
			// baseline is the same.
			pointerX += lastLineWidth;
		}
		else
		{
			setBaseLine(getBaseLine()+(contentHeight-el.getLastLineHeight()));
			setLineHeight(el.getLastLineHeight());
			pointerX = lastLineWidth;
		}
		
		el.setId(topLevelTag.tagId);
	}
	
	public void handleComponent(Tag topLevelTag, Component el)
	{
		el.validate();

		int d[] = el.getPrefSize();
		int w = d[0];
		int h = d[1];
		
		if(pointerX>0 && pointerX+w>containerWidth)
		{
			lineBreak(topLevelTag.font.getHeight(),false);
		}
		
		if(w>containerWidth)
		{// the components is larger than the with of this container. The only way to display it is by resizing the container.
			increaseContainerWidthBy(w-containerWidth);
		}
		
		// update other primitives on the same line to have the correct height.
		updatePrimitivesInLineForPrimitiveHeight(h);

		el.setX(pointerX); 
		el.setY(getBaseLine()-h);
		elementContainer.add(el);
		// update the pointetX,pointerY.
		pointerX += w;
		
		el.setId(topLevelTag.tagId);
	}
	
	/**
	 * Recursively increase the width of this block tag and all its parents
	 * @param diff
	 */
	private void increaseContainerWidthBy(int diff)
	{
		BlockTag bt = this;
		while(bt!=null && diff>0)
		{
			diff -= bt.pointerX;
			if(diff>0)
			{
				bt.containerWidth +=diff;
				bt= bt.parentBlockTag;
			}
		}
	}
	
	private void updatePrimitivesInLineForPrimitiveHeight(int primitiveHeight)
	{
		int baseLine = getBaseLine();

		int lineHeight = getLineHeight();
		if(primitiveHeight>lineHeight) // there may be a component on my left that need rearangement
		{ // the new lineHeight will be primitiveHeight and the base line will move lower.
			int diff = (primitiveHeight-lineHeight);
			for(int i=elementContainer.countComponents()-1;i>=0;--i) // yes there is at least one.
			{
				Component primitive = (Component)elementContainer.getComponent(i);
				
				if((primitive.getY()+primitive.getContentHeight())!=baseLine) 
				{ // done, this primitive ends above current location.
					break;
				}
				
				if(primitive instanceof TextComponent)
				{
					TextComponent tp = (TextComponent)primitive;
					int lastLineExtraHeight = tp.getLastLineExtraHeight() + diff;
					tp.setLastLineExtraHeight(lastLineExtraHeight);
					int []d = tp.getPrefSize();
					if(d==null) d = tp.getMinSize();
					tp.setPrefSize(d[0],d[1]+diff);
					tp.validate();
				}
				else  // if(primitive instanceof ImageComponent || instanceof InputComponent
				{
					primitive.setY(primitive.getY()+ diff);
				}
			}
			setBaseLine(baseLine +diff);
			setLineHeight(primitiveHeight);
		}
	}
	

	public int getContainerWidth()
	{
		return containerWidth;
	}
}
