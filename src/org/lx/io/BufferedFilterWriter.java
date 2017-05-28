package org.lx.io;

import java.io.CharArrayWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * The BufferedFilterWriter will buffer when the start flag matched and process
 * the buffer when end flag matched or buffer full.
 * @author zlc
 */
public abstract class BufferedFilterWriter extends FilterWriter
{
	CharArrayWriter m_buf;

	int m_maxSize;

	/**
	 * constructor of BufferedFilterWriter
	 * @param w
	 * @param buflen,
	 *        the max buffer size,-1 means no max size.
	 */
	public BufferedFilterWriter(Writer w, int bufLen)
	{
		super(w);
		// build the buffer
		m_maxSize = bufLen;
		if (m_maxSize != -1)
		{
			m_buf = new CharArrayWriter(m_maxSize);
		} else
		{
			m_buf = new CharArrayWriter();
		}
	}

	public BufferedFilterWriter(Writer w)
	{
		this(w, 1);
	}

	public void write(String str) throws IOException
	{
		write(str.toCharArray(), 0, str.length());
	}

	public void write(char[] cbuf) throws IOException
	{
		write(cbuf, 0, cbuf.length);
	}

	public void write(String str, int off, int len) throws IOException
	{
		write(str.toCharArray(), off, len);
	}

	public void write(char[] cbuf, int off, int len) throws IOException
	{
		for (int i = off; i < off + len; i++)
		{
			write(cbuf[i]);
		}
	}

	/* do not buffered as default */
	protected boolean isStartBufFlag(int c)
	{
		return false;
	}

	protected boolean isEndBufFlag(int c)
	{
		return true;
	}

	protected abstract String onFilterBuffer(String str) throws IOException;

	// Attention:all of the write method will invoke the "write(int c)"
	// interface.
	// you can do the filter function in "write(int c)".
	public void write(int c) throws IOException
	{
		// meet start buffer char
		if (isStartBufFlag(c) || m_buf.size() > 0)
		{
			m_buf.write(c);
		} else
		{
			// write to output directly
			super.out.write(c);
		}
		// if buffer full or meet end.
		if (m_maxSize == m_buf.size() || isEndBufFlag(c))
		{
			flush();
		}
	}

	/**
	 * filter the buffer before flush if buffer not empty.
	 */
	public void flush() throws IOException
	{
		if (m_buf.size() > 0)
		{
			String str = new String(m_buf.toCharArray());
			String filter_str = onFilterBuffer(str);
			super.out.write(filter_str);
			reset();
		}

		super.flush();
	}

	protected int size()
	{
		return m_buf.size();
	}

	protected void reset()
	{
		m_buf.reset();
	}
}
