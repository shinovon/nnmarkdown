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

import cc.nnproject.markdown.Markdown;
import cc.nnproject.markdown.MarkdownListener;

/**
 * Very unoptimized HTML demo for nnmarkdown library
 * @author Shinovon
 */
public class MarkdownHTMLDemo implements MarkdownListener {
	
	Stack linkMarkers = new Stack();
	Hashtable urls = new Hashtable(); // link=>url table
	
	StringBuffer result = new StringBuffer();
	
	public static void main(String[] args) {
		new MarkdownHTMLDemo().start();
	}
	
	public void start() {
		linkMarkers.removeAllElements();
		urls.clear();
		result.setLength(0);
		
		String text;
		try {
			text = readUtf("".getClass().getResourceAsStream("/a"), 0);
		} catch (Exception e) {
			text = e.toString();
		}
		
		Markdown.enableStrikethroughFont = true;
		Markdown.parse(this, null, text, urls);
		
		System.out.println(result);
	}

	public void beginMarkdown(Object ctx) {
	}

	public void endMarkdown(Object ctx) {
	}

	public void beginHref(Object ctx, Object link) {
		linkMarkers.addElement(new Object[] { new Integer(result.length()), link });
	}

	public void endHref(Object ctx) {
		Object[] link = (Object[]) linkMarkers.pop();
		result.append("</a>");
		
		int insert = ((Integer) link[0]).intValue();
		String s = "<a href=\"" + escapeArg((String) urls.get(link[1])) + "\">";
		result.insert(insert, s);
	}

	public void beginCodeBlock(Object ctx, String language) {
		result.append("<code>");
	}

	public void endCodeBlock(Object ctx) {
		result.append("</code>");
	}

	public void beginBlockQuote(Object ctx) {
		result.append("<div>");
	}

	public void endBlockQuote(Object ctx) {
		result.append("</div>");
	}

	public void append(Object ctx, String text, int font) {
		if ((font & Markdown.FONT_STYLE_BOLD) != 0) {
			result.append("<b>");
		}
		if ((font & Markdown.FONT_STYLE_ITALIC) != 0) {
			result.append("<i>");
		}
		if ((font & Markdown.FONT_STYLE_UNDERLINED) != 0) {
			result.append("<sub>");
		}
		if ((font & Markdown.FONT_STYLE_STRIKETHROUGH) != 0) {
			result.append("<s>");
		}
		result.append(escapeHTML(text));
		if ((font & Markdown.FONT_STYLE_STRIKETHROUGH) != 0) {
			result.append("</s>");
		}
		if ((font & Markdown.FONT_STYLE_UNDERLINED) != 0) {
			result.append("</sub>");
		}
		if ((font & Markdown.FONT_STYLE_ITALIC) != 0) {
			result.append("</i>");
		}
		if ((font & Markdown.FONT_STYLE_BOLD) != 0) {
			result.append("</b>");
		}
	}

	public void appendLink(Object ctx, String text, int font) {
		// TODO
		if ((font & Markdown.FONT_STYLE_BOLD) != 0) {
			result.append("<b>");
		}
		if ((font & Markdown.FONT_STYLE_ITALIC) != 0) {
			result.append("<i>");
		}
		if ((font & Markdown.FONT_STYLE_UNDERLINED) != 0) {
			result.append("<sub>");
		}
		if ((font & Markdown.FONT_STYLE_STRIKETHROUGH) != 0) {
			result.append("<s>");
		}
		result.append(escapeHTML(text));
		if ((font & Markdown.FONT_STYLE_STRIKETHROUGH) != 0) {
			result.append("</s>");
		}
		if ((font & Markdown.FONT_STYLE_UNDERLINED) != 0) {
			result.append("</sub>");
		}
		if ((font & Markdown.FONT_STYLE_ITALIC) != 0) {
			result.append("</i>");
		}
		if ((font & Markdown.FONT_STYLE_BOLD) != 0) {
			result.append("</b>");
		}
	}

	public void appendInlineSpace(Object ctx, int font) {
		result.append("&nbsp;");
	}

	public void appendImage(Object ctx, String src, String alt) {
		result.append("<img alt=\"")
		.append(escapeArg(alt))
		.append("\" src=\"")
		.append(src)
		.append("\">");
	}

	public void lineBreak(Object ctx) {
		result.append("<br>");
	}

	public void beginHeader(Object ctx, int n) {
		// TODO
	}

	public void endHeader(Object ctx, int n) {
		// TODO
		if (n <= 3) result.append("<hr>");
	}

	public void horizontalLine(Object ctx) {
		result.append("<hr>");
	}
	
	private static String escapeHTML(String s) {
		// TODO
		return replace(replace(replace(replace(s, "&", "&amp;"), "<", "&lt;"), ">", "&gt;"), "\n", "<br>");
	}
	
	private static String escapeArg(String s) {
		// TODO
		return replace(s, "\"", "\\\"");
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
	
	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}


}
