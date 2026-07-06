/*
Copyright (c) 2026 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;

import cc.nnproject.markdown.Markdown;
import cc.nnproject.markdown.MarkdownListener;

/**
 * LCDUI application demo for nnmarkdown library
 * @author Shinovon
 */
public class MarkdownLCDUIDemo extends MIDlet implements MarkdownListener, CommandListener, ItemCommandListener, Runnable {
	
	static final Command exitCmd = new Command("Exit", Command.EXIT, 0);
	static final Command itemLinkCmd = new Command("Link", Command.ITEM, 1);
	
	boolean started;
	Form form;
	
	Hashtable urls = new Hashtable(); // link=>url table
	Hashtable itemsLinks = new Hashtable(); // item=>link table
	Stack links = new Stack();
	Vector loadImages = new Vector(); // image loading queue

	protected void destroyApp(boolean unconditional) {
	}

	protected void pauseApp() {
	}

	protected void startApp()  {
		if (started) return;
		started = true;
		
		Form form = new Form("Markdown demo");
		form.addCommand(exitCmd);
		form.setCommandListener(this);
		
		Display.getDisplay(this).setCurrent(this.form = form);
		
		// start parsing thread
		new Thread(this).start();
	}
	
	public void run() {
		// cleanup
		urls.clear();
		itemsLinks.clear();
		links.removeAllElements();
		
		String text;
		try {
			text = readUtf("".getClass().getResourceAsStream("/a"), 0);
		} catch (Exception e) {
			text = e.toString();
		}
		
		Markdown.enableBlockquotes = false;
		Markdown.breakOnNewLine = true;
		Markdown.parse(this, form, text, urls);
	}
	
	// region Command listener

	public void commandAction(Command c, Item item) {
		if (c == itemLinkCmd) {
			// process link
			Object link = itemsLinks.get(item);
			if (link != null) {
				String url;
				if (link instanceof String) {
					url = (String) link;
				} else {
					url = (String) urls.get(link);
				}
				
				// open url in browser
				try {
					if (platformRequest(url)) {
						notifyDestroyed();
					}
				} catch (Exception ignored) {}
			}
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == exitCmd) {
			notifyDestroyed();
		}
	}
	
	// endregion Command listener
	
	// region Markdown listener

	public void beginMarkdown(Object ctx) {
		form.setTicker(new Ticker("Loading..."));
	}

	public void endMarkdown(Object ctx) {
		// process image loading queue
		while (!loadImages.isEmpty()) {
			Object[] o = (Object[]) loadImages.elementAt(0);
			loadImages.removeElementAt(0);
			
			ImageItem item = (ImageItem) o[0];
			String url = (String) urls.get(o[1]);
			try {
				item.setImage(Image.createImage(url));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// parsing finished, remove ticker
		form.setTicker(null);
	}

	public void beginHref(Object ctx, Object link) {
		links.push(link);
	}

	public void endHref(Object ctx) {
		links.pop();
	}

	public void beginCodeBlock(Object ctx, String language) {
	}

	public void endCodeBlock(Object ctx) {
	}

	public void beginBlockQuote(Object ctx) {
	}

	public void endBlockQuote(Object ctx) {
	}

	public void append(Object ctx, String text, int font) {
		StringItem item = new StringItem(null, text);
		item.setFont(getFont(font));
		form.append(item);
		
		if (!links.empty()) {
			Object link = links.peek();
			item.setDefaultCommand(itemLinkCmd);
			item.setItemCommandListener(this);
			itemsLinks.put(item, link);
		}
	}

	public void appendLink(Object ctx, String text, int font) {
		StringItem item = new StringItem(null, text, StringItem.HYPERLINK);
		item.setFont(getFont(font));
		item.setDefaultCommand(itemLinkCmd);
		item.setItemCommandListener(this);
		form.append(item);
		
		Object link = text;
		if (!links.empty()) {
			link = links.peek();
		}
		itemsLinks.put(item, link);
	}

	public void appendInlineSpace(Object ctx, int font) {
		Font f = getFont(font);
		form.append(new Spacer(f.charWidth(' '), f.getBaselinePosition()));
	}

	public void appendImage(Object ctx, Object srcLink, String text) {
		ImageItem item;
		if (!links.empty()) {
			// add as button
			item = new ImageItem(text, null, 0, null, Item.BUTTON);
			
			Object link = links.peek();
			item.setDefaultCommand(itemLinkCmd);
			item.setItemCommandListener(this);
			itemsLinks.put(item, link);
		} else {
			item = new ImageItem(text, null, 0, null);
		}
		
		form.append(item);
		
		// add image to loading queue
		loadImages.addElement(new Object[] { item, srcLink });
	}

	public void lineBreak(Object ctx) {
		form.append("\n");
	}

	public void header(Object ctx) {
		Spacer spacer = new Spacer(1, 4);
		spacer.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
		form.append(spacer);
	}

	public void horizontalLine(Object ctx) {
		Spacer spacer = new Spacer(10, 10);
		spacer.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
		form.append(spacer);
	}
	
	// endregion Markdown listener
	
	// region Utilities
	
	// utility for creating MIDP font
	static final Font getFont(int font) {
		return Font.getFont(font & (Markdown.MIDP_FONT_FACE_MASK),
				font & (Markdown.MIDP_FONT_STYLE_MASK),
				font & (Markdown.MIDP_FONT_SIZE_MASK)
				);
	}

	private static String readUtf(InputStream in, int i) throws IOException {
		byte[] buf = new byte[i <= 0 ? 1024 : i];
		i = 0;
		int j;
		while ((j = in.read(buf, i, buf.length - i)) != -1) {
			if ((i += j) >= buf.length) {
				System.arraycopy(buf, 0, buf = new byte[i + 2048], 0, i);
			}
		}
		return new String(buf, 0, i, "UTF-8");
	}
	
	// endregion

}
