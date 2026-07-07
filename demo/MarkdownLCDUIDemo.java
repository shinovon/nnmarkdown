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
	
	Hashtable urls = new Hashtable(); // link=>url table
	Hashtable itemsLinks = new Hashtable(); // item=>link table
	Stack links = new Stack();
	int listIndent;

	protected void destroyApp(boolean unconditional) {
	}

	protected void pauseApp() {
	}

	protected void startApp()  {
		if (started) return;
		started = true;
		
		// start parsing thread
		new Thread(this).start();
	}
	
	public void run() {
		Form form = new Form("Markdown demo");
		form.addCommand(exitCmd);
		form.setCommandListener(this);
		
		Display.getDisplay(this).setCurrent(form);
		
		// cleanup
		urls.clear();
		itemsLinks.clear();
		links.removeAllElements();
		listIndent = 0;
		
		// load source text
		String text;
		try {
			text = readUtf("".getClass().getResourceAsStream("/a.md"), 0);
		} catch (Exception e) {
			text = e.toString();
		}
		
		// parsing options
		Markdown.enableBlockquotes = false;
		Markdown.monospaceIsBold = true;
		
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
		((Form) ctx).setTicker(new Ticker("Loading..."));
	}

	public void endMarkdown(Object ctx) {
		// parsing finished, remove ticker
		((Form) ctx).setTicker(null);
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
		((Form) ctx).append(item);
		
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
		((Form) ctx).append(item);
		
		Object link = text;
		if (!links.empty()) {
			link = links.peek();
		}
		itemsLinks.put(item, link);
	}

	public void appendInlineSpace(Object ctx, int font) {
		Font f = getFont(font);
		((Form) ctx).append(new Spacer(f.charWidth(' '), f.getBaselinePosition()));
	}

	public void appendImage(Object ctx, String src, String text) {
		Image img;
		try {
			img = Image.createImage(src);
		} catch (Exception e) {
			img = null;
		}
		
		ImageItem item;
		if (!links.empty()) {
			// add as button
			item = new ImageItem(text, img, 0, null, Item.BUTTON);
			
			Object link = links.peek();
			item.setDefaultCommand(itemLinkCmd);
			item.setItemCommandListener(this);
			itemsLinks.put(item, link);
		} else {
			item = new ImageItem(text, img, 0, null);
		}
		
		((Form) ctx).append(item);
	}

	public void lineBreak(Object ctx) {
		((Form) ctx).append("\n");
	}

	public void beginHeader(Object ctx, int n) {
		Spacer spacer = new Spacer(1, 4);
		spacer.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
		((Form) ctx).append(spacer);
	}

	public void endHeader(Object ctx, int n) {
		if (n > 3) return;
		Spacer spacer = new Spacer(10, 10);
		spacer.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
		((Form) ctx).append(spacer);
	}

	public void beginList(Object ctx, boolean ordered) {
		listIndent++;
	}

	public void endList(Object ctx, boolean ordered) {
		listIndent--;
	}

	public void beginListItem(Object ctx, int n) {
		StringBuffer sb = new StringBuffer();

		for (int i = 1; i < listIndent; i++) {
			sb.append("  ");
		}
		if (n == 0) {
			sb.append("* ");
		} else {
			sb.append(n).append(". ");
		}

		((Form) ctx).append(sb.toString());
	}

	public void endListItem(Object ctx) {

	}

	public void horizontalLine(Object ctx) {
		// can't render lines on lcdui form, add spacer instead
		Spacer spacer = new Spacer(10, 10);
		spacer.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER);
		((Form) ctx).append(spacer);
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
	
	// endregion Utilities

}
