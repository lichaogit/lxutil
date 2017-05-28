package org.lx.arch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;

/**
 * use the XML document to store the configuration info.
 * @author Administrator
 */
public class Dom4jConfiguration extends Dom4jNodeConfiguration
{
	SimpleNamespaceContext m_nameSpaceContext;

	public Dom4jConfiguration(Document doc)
	{
		super(doc);
	}

	public Dom4jConfiguration()
	{
		super(DocumentHelper.createDocument());
	}

	public Dom4jConfiguration(InputStream in) throws IOException
	{
		this(in, null);

	}

	protected void setNameSpace(Map nameSpaces)
	{
		m_nameSpaceContext = nameSpaces == null ? null
				: new SimpleNamespaceContext(nameSpaces);
	}

	public Dom4jConfiguration(String text, Map nameSpaces) throws IOException
	{
		Document doc = buildDocument(text);
		init(doc);
		setNameSpace(nameSpaces);
	}

	public Dom4jConfiguration(Reader reader, Map nameSpaces) throws IOException
	{
		Document doc = buildDocument(reader);
		init(doc);
		setNameSpace(nameSpaces);
	}

	public Dom4jConfiguration(InputStream in, Map nameSpaces)
			throws IOException
	{
		Document doc = buildDocument(in);
		init(doc);
		setNameSpace(nameSpaces);
	}

	public void write(OutputStream out) throws IOException
	{
		OutputFormat format = OutputFormat.createCompactFormat();

		XMLWriter xw = new XMLWriter(out, format);
		xw.write(m_node.getDocument());

		xw.flush();
		xw.close();
	}

	public void write(Writer w) throws IOException
	{
		OutputFormat format = OutputFormat.createCompactFormat();

		XMLWriter xw = new XMLWriter(w, format);
		xw.write(m_node.getDocument());

		xw.flush();
		xw.close();
	}

	public List getHandlers(Object base, String nodePath) throws Exception
	{
		Dom4jXPath dom4jxpath = new Dom4jXPath(nodePath);
		if (m_nameSpaceContext != null)
		{
			dom4jxpath.setNamespaceContext(m_nameSpaceContext);
		}
		return super.getHandlers(base, dom4jxpath);

	}

	private static String getEncoding(String text)
	{
		String result = null;

		String xml = text.trim();

		if (xml.startsWith("<?xml"))
		{
			int end = xml.indexOf("?>");
			String sub = xml.substring(0, end);
			StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

			while (tokens.hasMoreTokens())
			{
				String token = tokens.nextToken();

				if ("encoding".equals(token))
				{
					if (tokens.hasMoreTokens())
					{
						result = tokens.nextToken();
					}

					break;
				}
			}
		}

		return result;
	}

	protected Document buildDocument(Object obj) throws IOException
	{
		Document retval = null;
		try
		{
			SAXReader saxReader = new SAXReader();

			if (obj instanceof String)
			{
				retval = DocumentHelper.parseText((String) obj);
			} else if (obj instanceof Reader)
			{
				retval = saxReader.read((Reader) obj);
			} else if (obj instanceof InputStream)
			{
				retval = saxReader.read((InputStream) obj);
			}
		} catch (DocumentException e)
		{
			throw new IOException(e.getLocalizedMessage());
		}
		return retval;
	}
}
