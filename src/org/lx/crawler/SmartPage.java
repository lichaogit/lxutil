package org.lx.crawler;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

public class SmartPage extends Page
{
	static Charset m_cn_cs = Charset.forName("GB18030");

	final static String m_raw_cs = "ISO-8859-1";

	String m_latest_cs = null;

	public String getCharset()
	{
		return m_latest_cs == null ? m_raw_cs : m_latest_cs;
	}

	/**
	 * override the Page's method.
	 */
	public String getCharset(String content)
	{
		String retval = null;
		final String CHARSET_STRING = "charset";
		int index;

		if (null == mSource)
			retval = DEFAULT_CHARSET;
		else
			// use existing (possibly supplied) character set:
			// bug #1322686 when illegal charset specified
			retval = mSource.getEncoding();

		if (null != content)
		{
			index = content.indexOf(CHARSET_STRING);

			if (index != -1)
			{
				content = content.substring(index + CHARSET_STRING.length())
						.trim();
				if (content.startsWith("="))
				{
					content = content.substring(1).trim();
					index = content.indexOf(";");
					if (index != -1)
						content = content.substring(0, index);

					// remove any double quotes from around charset string
					if (content.startsWith("\"") && content.endsWith("\"")
							&& (1 < content.length()))
						content = content.substring(1, content.length() - 1);

					// remove any single quote from around charset string
					if (content.startsWith("'") && content.endsWith("'")
							&& (1 < content.length()))
						content = content.substring(1, content.length() - 1);

					retval = content;
				}
			}
		}
		// save the latest charset info.
		m_latest_cs = retval;

		return retval;
	}

	public static String getBigestCharset(String charset)
	{
		return m_cn_cs.contains(Charset.forName(charset)) ? m_cn_cs.name()
				: charset;
	}

	/**
	 * Set the URLConnection to be used by this page. Starts reading from the
	 * given connection. This also resets the current url.
	 * @param connection
	 *        The connection to use. It will be connected by this method.
	 * @exception ParserException
	 *            If the <code>connect()</code> method fails, or an I/O error
	 *            occurs opening the input stream or the character set
	 *            designated in the HTTP header is unsupported.
	 */
	// public void setConnection(URLConnection connection) throws
	// ParserException
	// {
	// Stream stream;
	// String type;
	// String charset;
	// String contentEncoding;
	//
	// mConnection = connection;
	// try
	// {
	// getConnection().connect();
	// } catch (UnknownHostException uhe)
	// {
	// throw new ParserException("Connect to "
	// + mConnection.getURL().toExternalForm() + " failed.", uhe);
	// } catch (IOException ioe)
	// {
	// throw new ParserException("Exception connecting to "
	// + mConnection.getURL().toExternalForm() + " ("
	// + ioe.getMessage() + ").", ioe);
	// }
	// type = getContentType();
	// // use the biggest charset that compatible charset.
	// charset = getCharset(type);
	// charset = getBigestCharset(charset);
	// try
	// {
	// contentEncoding = connection.getContentEncoding();
	// if ((null != contentEncoding)
	// && (-1 != contentEncoding.indexOf("gzip")))
	// {
	// stream = new Stream(new GZIPInputStream(getConnection()
	// .getInputStream()));
	// } else if ((null != contentEncoding)
	// && (-1 != contentEncoding.indexOf("deflate")))
	// {
	// stream = new Stream(new InflaterInputStream(getConnection()
	// .getInputStream(), new Inflater(true)));
	// } else
	// {
	// stream = new Stream(getConnection().getInputStream());
	// }
	//
	// try
	// {
	// mSource = new InputStreamSource(stream, charset);
	// } catch (UnsupportedEncodingException uee)
	// {
	// // StringBuffer msg;
	// //
	// // msg = new StringBuffer (1024);
	// // msg.append (getConnection ().getURL ().toExternalForm ());
	// // msg.append (" has an encoding (");
	// // msg.append (charset);
	// // msg.append (") which is not supported, using ");
	// // msg.append (DEFAULT_CHARSET);
	// // System.out.println (msg.toString ());
	// charset = DEFAULT_CHARSET;
	// mSource = new InputStreamSource(stream, charset);
	// }
	// } catch (IOException ioe)
	// {
	// throw new ParserException("Exception getting input stream from "
	// + mConnection.getURL().toExternalForm() + " ("
	// + ioe.getMessage() + ").", ioe);
	// }
	// mUrl = connection.getURL().toExternalForm();
	// mIndex = new PageIndex(this);
	// }
	public SmartPage(InputStream stream, String charset)
			throws UnsupportedEncodingException
	{
		super(stream, charset);
	}

	public SmartPage(URLConnection connection) throws ParserException
	{
		super(connection);
	}

	/**
	 * Begins reading from the source with the given character set. If the
	 * current encoding is the same as the requested encoding, this method is a
	 * no-op. Otherwise any subsequent characters read from this page will have
	 * been decoded using the given character set.<p> Some magic happens here to
	 * obtain this result if characters have already been consumed from this
	 * page. Since a Reader cannot be dynamically altered to use a different
	 * character set, the underlying stream is reset, a new Source is
	 * constructed and a comparison made of the characters read so far with the
	 * newly read characters up to the current position. If a difference is
	 * encountered, or some other problem occurs, an exception is thrown.
	 * @param character_set
	 *        The character set to use to convert bytes into characters.
	 * @exception ParserException
	 *            If a character mismatch occurs between characters already
	 *            provided and those that would have been returned had the new
	 *            character set been in effect from the beginning. An exception
	 *            is also thrown if the underlying stream won't put up with
	 *            these shenanigans.
	 */
	// public void setEncoding(String character_set)
	// {
	// try
	// {
	// getSource().setEncoding(character_set);
	// } catch (ParserException e)
	// {
	// //ignore the parser exception because of the charset changed.
	// }
	// int i=0;
	// }
}
