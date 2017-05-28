package org.lx.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFactory;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.lx.io.BufferedFilterOutputStream;
import org.lx.util.XMLEscape;

public class HtmlNode2XMLOutputStream extends BufferedFilterOutputStream
{

	private Stack m_TagStack = new Stack();

	final static private Pattern m_blank_pattern = Pattern.compile("\\s*");

	final static private Pattern m_attr_pattern = Pattern.compile("^\\w+");

	final static private char[] m_xml_reserved = new char[] { '\'', '\"', '=' };

	final static private String[] m_leafTag = new String[] { "BASE", "BR",
			"META", "INPUT", "LINK", "IMG", "ISINDEX", "HR", "COL", "PARAM",
			"EMBED", "FRAME", "WBR", "BGSOUND", "SPACER", "KEYGEN", "AREA",
			"BASEFONT" };

	XMLEscape m_xmlEscape = new XMLEscape();

	protected static org.htmlparser.Parser m_parser;

	String m_charset;

	public HtmlNode2XMLOutputStream(OutputStream out)
	{
		super(out, -1);
		if (m_parser == null)
		{
			m_parser = new org.htmlparser.Parser();
			NodeFactory nf = m_parser.getNodeFactory();
			if (nf instanceof PrototypicalNodeFactory)
			{
				PrototypicalNodeFactory pnf = (PrototypicalNodeFactory) nf;
				pnf.registerTag(new QuoteScriptTag());
				pnf.registerTag(new MyMetaTag());
			}
		}

	}

	static protected boolean isXMLReserved(int c)
	{
		boolean retval = false;
		for (int i = 0; i < m_xml_reserved.length; i++)
		{
			if (m_xml_reserved[i] == c)
			{
				retval = true;
				break;
			}
		}
		return retval;
	}

	static protected boolean isValidXMLAttr(String attr)
	{
		boolean retval = true;
		char[] c = attr.toCharArray();
		if (!Character.isJavaIdentifierStart(c[0]))
		{
			retval = false;
		} else
		{
			for (int i = 0; i < c.length; i++)
			{
				if (isXMLReserved(c[i]))
				{
					retval = false;
					break;
				}
			}
		}
		return retval;
	}

	static protected boolean isBlank(String str)
	{
		Matcher m = m_blank_pattern.matcher(str);
		return m.matches() || isUnicodeBlank(str);
	}

	static protected boolean isWord(String str)
	{
		Matcher m = m_attr_pattern.matcher(str);
		return m.matches();
	}

	static protected boolean isUnicodeBlank(String str)
	{
		boolean retval = true;
		char[] c = str.toCharArray();
		for (int i = 0; i < c.length; i++)
		{
			if (!Character.isSpaceChar(c[i]))
			{
				retval = false;
				break;
			}

		}

		return retval;
	}

	boolean isLeafTag(String tag)
	{
		boolean retval = false;
		for (int i = 0; i < m_leafTag.length; i++)
		{
			if (m_leafTag[i].equalsIgnoreCase(tag))
			{
				retval = true;
				break;
			}
		}
		return retval;
	}

	public String getCharset()
	{
		return m_charset;
	}

	/**
	 * @param string
	 * @return Object[], the first element is key, the second element is
	 *         StringBuffer.
	 * @throws IOException
	 */
	protected Object[] formatAttr(String string) throws IOException
	{
		Object[] retval = new Object[2];
		int off = string.indexOf('=');
		if (off == -1)
		{
			// consider the <BR/>'s attr is 'br/'
			if (string.startsWith("/") || !isValidXMLAttr(string))
			{
				return null;
			}
			/* check whether the attr is valid */
			// process the single boolean attribute.
			retval[0] = string;
			retval[1] = new StringBuffer("true");
		} else
		{
			// contain '=' case:
			String key = string.substring(0, off);
			String value = string.substring(off + 1);
			retval[0] = key;
			if (isBlank(value))
			{
				retval[1] = new StringBuffer(' ');
			} else
			{
				// remove the ' or "
				int start = 0;
				int end = value.length();
				if (value.charAt(0) == '\'' || value.charAt(0) == '\"')
				{
					start = 1;
				}
				if (value.charAt(value.length() - 1) == '\''
						|| value.charAt(value.length() - 1) == '\"')
				{
					end = value.length() - 1;
				}
				StringWriter writer = new StringWriter();
				value = value.substring(start, end);

				writer.write(m_xmlEscape.encode(value));

				retval[1] = writer.getBuffer();
			}
		}
		if (!isWord(retval[0].toString()))
		{
			retval = null;
		}
		return retval;
	}

	protected boolean isAttrExisted(Map map, String attr)
	{
		boolean retval = false;
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			if (attr.equals(it.next()))
			{
				retval = true;
				break;
			}
		}
		return retval;
	}

	void popToEndTag(TagNode endTag, Writer w) throws IOException
	{
		String work = null;
		String tagName = endTag.getTagName().toUpperCase();
		boolean find = false;
		do
		{
			if (m_TagStack.empty())
			{
				break;
			}
			Enumeration e = m_TagStack.elements();
			// write the first attribute==the tagname
			while (e.hasMoreElements())
			{
				if (tagName.equals(e.nextElement()))
				{
					find = true;
					break;
				}
			}

			if (!find)
			{
				// skip this end tag.
				break;
			}

			while (!m_TagStack.empty())
			{
				work = (String) m_TagStack.peek();
				if (work.equalsIgnoreCase(tagName))
				{
					// find the matcher
					m_TagStack.pop();
					w.write("</");
					w.write(endTag.getTagName());
					w.write(">");
					break;
				}
				// add the missing endTag
				w.write("</");
				w.write(work);
				w.write('>');
				m_TagStack.pop();
			}
		} while (false);

	}

	public class MyMetaTag extends MetaTag
	{
		/**
		 * Perform the META tag semantic action. Check for a charset directive,
		 * and if found, set the charset for the page.
		 * @exception ParserException
		 *            If setting the encoding fails.
		 */
		public void doSemanticAction() throws ParserException
		{
			String charset = getAttribute("charset");
			if (charset != null)
			{
				getPage().setEncoding(charset);
			}
			super.doSemanticAction();
		}

	}

	public void write(InputStream in, String charset) throws IOException
	{
		try
		{
			Page page = new SmartPage(in, charset);
			Lexer lexer = new Lexer(page);
			m_parser.setLexer(lexer);
			NodeList nl = null;
			try
			{
				// parse the whole node(filter nothing).
				nl = m_parser.parse(null);
			} catch (ParserException e)
			{
				// if there are CJK character occur before META's charset
				// declare, then
				// ParserException will be throwed, the content should be
				// reparser.
				m_parser.reset();
				nl = m_parser.parse(null);
			}

			// the encoding must be retrived after parser, because the
			// HTTP META TAG may have specify the charset that is different with
			// the HEADER.

			String real_charset = page.getSource().getEncoding();
			if (!real_charset.equals(charset))
			{
				m_charset = real_charset;
			}

			if (m_charset != null)
			{
				OutputStreamWriter out_writer = new OutputStreamWriter(
						super.out, m_charset);

				org.htmlparser.Node node = null;
				out_writer.write("<?xml version=\"1.0\" encoding=\"");
				out_writer.write(m_charset);
				out_writer.write("\"?><root>");
				for (SimpleNodeIterator it = nl.elements(); it.hasMoreNodes();)
				{
					node = it.nextNode();
					write(node, out_writer);
				}
				out_writer.write("</root>");
				out_writer.flush();
			}

		} catch (ParserException e)
		{
			e.printStackTrace();

		} catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Write a single character.
	 * @exception IOException
	 *            If an I/O error occurs
	 */
	public void write(Node node, Writer w) throws IOException
	{
		if (node instanceof TagNode)
		{

			TagNode tn = (TagNode) node;
			String tagName = tn.getTagName();
			// check the tagName
			if (!isWord(tagName))
			{
				return;
			}
			if (tn.isEndTag())
			{
				// pop nodes util find the matcher.
				popToEndTag(tn, w);
				// process end Tag finished.
				return;
			}

			m_TagStack.push(tagName);

			NodeList nl = node.getChildren();
			// write the start tag include the attributes.
			Vector attrs = tn.getAttributesEx();
			if (attrs != null)
			{
				Attribute attribute = null;
				// write(System.getProperty("line.separator"));
				w.write("<");
				// write the first attr (Tag Name)
				w.write(tagName);

				Iterator it = attrs.iterator();
				it.next(); // skip the first attr(Tag Name)

				// save the attrs info.
				HashMap attrMap = new HashMap();
				Vector fmt_attrs = new Vector();
				Object[] pair = null;
				String attr = null;
				StringBuffer val = null;

				boolean bEndTag = false;
				while (it.hasNext())
				{
					attribute = (Attribute) it.next();
					attr = attribute.toString();
					if (isBlank(attr))
					{
						continue;
					}
					if (attr.length() == 1 && attr.charAt(0) == '/')
					{
						bEndTag = true;
					}
					pair = formatAttr(attr);
					// skip the invalid attribute.
					if (pair == null || pair.length != 2)
					{
						continue;
					}
					// skip the default namespace info.

					if ("xmlns".equals(pair[0].toString()))
					{
						continue;
					}

					if (!isAttrExisted(attrMap, pair[0].toString()))
					{
						attrMap.put(pair[0], pair[1]);
						fmt_attrs.add(pair);
					} else
					// duplicate.
					{
						val = (StringBuffer) attrMap.get(pair[0]);
						// recalculate the value.
						val.append(',');
						val.append(pair[1].toString());
					}
				}

				// output the value to writer.
				it = fmt_attrs.iterator();
				while (it.hasNext())
				{
					pair = (Object[]) it.next();
					if (pair == null || pair.length != 2)
					{
						continue;
					}
					// add space between attributes.
					w.write(' ');

					w.write(pair[0].toString());
					w.write("=\"");
					w.write(pair[1].toString());
					w.write("\"");
				}

				// if it is the single tag like <BR> then add '/>':<BR>-><BR/>.
				if (bEndTag || isLeafTag(tagName))
				{
					m_TagStack.pop();
					w.write("/>");
				} else
				{
					w.write('>');
				}
			}

			// write the children node
			if (nl != null)
			{
				org.htmlparser.Node child_node = null;
				for (SimpleNodeIterator it = nl.elements(); it != null
						&& it.hasMoreNodes();)
				{
					child_node = it.nextNode();
					write(child_node, w);
				}
			}

			// if the endTag is exited,write it.
			TagNode tag = (TagNode) tn.getEndTag();
			if (tag != null)
			{
				popToEndTag(tag, w);
			}
			// super.write(node.toHtml());
		} else if (node instanceof TextNode)
		{
			// wrappe text with CDATA If the Text occur outside the 'html'.
			TextNode tn = (TextNode) node;
			String str = tn.toHtml();

			if (!isBlank(str))
			{
				boolean in_html = m_TagStack.contains("HTML");
				if (!in_html)
				{
					w.write("<![CDATA[");
					w.write(str);
					w.write("]]>");
				} else
				{
					// the content text cannot contain any illegal XML char.
					w.write(m_xmlEscape.encode(str));
				}
			} else
			{
				w.write(str);
			}

		}
	}

	/* do not buffered as default */
	protected boolean isStartBufFlag(int b)
	{
		return true;
	}

	protected boolean isEndBufFlag(int b)
	{
		return false;
	}

	public byte[] onFilterBuffer(byte[] b) throws IOException
	{
		// the intial charset is default(ISO-8859-1), the htmlParser will handle
		// the real charset.
		write(new ByteArrayInputStream(b), null);
		// return null because the above step have finish the write action.
		return null;
	}
}
