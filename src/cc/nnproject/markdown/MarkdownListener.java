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
package cc.nnproject.markdown;

public interface MarkdownListener {
	
	/**
	 * @param ctx Context passed by application
	 */
	void beginMarkdown(Object ctx);
	/**
	 * @param ctx Context passed by application
	 */
	void endMarkdown(Object ctx);
	
	/**
	 * @param ctx Context passed by application
	 * @param link
	 */
	void beginHref(Object ctx, Object link);
	/**
	 * @param ctx Context passed by application
	 */
	void endHref(Object ctx);
	
	/**
	 * @param ctx Context passed by application
	 * @param language Code language, may be null
	 */
	void beginCodeBlock(Object ctx, String language);
	/**
	 * @param ctx Context passed by application
	 */
	void endCodeBlock(Object ctx);

	/**
	 * @param ctx Context passed by application
	 * @param text Text
	 * @param font bitwise combination of font styles
	 */
	void append(Object ctx, String text, int font);

	/**
	 * @param ctx Context passed by application
	 * @param text Text
	 * @param font bitwise combination of font styles
	 */
	void appendLink(Object ctx, String text, int font);

	/**
	 * @param ctx Context passed by application
	 * @param font bitwise combination of font styles
	 */
	void appendInlineSpace(Object ctx, int font);
	
	/**
	 * @param ctx Context passed by application
	 * @param srcLink Link to the source of image
	 * @param alt Image caption, may be null
	 */
	void appendImage(Object ctx, Object srcLink, String alt);

	/**
	 * @param ctx Context passed by application
	 */
	void lineBreak(Object ctx);
	
	/**
	 * @param ctx Context passed by application
	 */
	void lineBreak2(Object ctx);

}
