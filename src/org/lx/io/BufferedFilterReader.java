package org.lx.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public abstract class BufferedFilterReader extends FilterReader
{
	char[] m_buf;

	int m_r_begin;

	int m_r_end;

	public boolean ready() throws IOException
	{
		boolean retval = false;
		if (m_r_end - m_r_begin > 0)
		{
			retval = true;
		} else
		{
			retval = super.ready();
		}
		return retval;
	}

	public BufferedFilterReader(Reader r, int bufLen)
	{
		super(r);
		m_buf = new char[bufLen];
		m_r_begin = 0;
		m_r_end = 0;
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

	public int read() throws IOException
	{
		int c = -1;
		// read c to buf;
		do
		{
			// read from buffer if it is not empty.
			if (m_r_end - m_r_begin > 0)
			{
				c = m_buf[m_r_begin++];
				break;
			}

			// init the read index.
			m_r_begin = 0;
			m_r_end = 0;

			// read one char from reader.
			int wk = super.read();
			if (wk == -1)
			{
				break;
			}
			if (isStartBufFlag(wk))
			{
				m_buf[m_r_end++] = (char) wk;
				for (int i = m_r_end; i < m_buf.length; i++)
				{
					if (wk == -1 || isStartBufFlag(wk) || isEndBufFlag(wk))
					{
						break;
					}
					wk = super.read();
					m_buf[m_r_end++] = (char) wk;
				}
				String filter_buf;
				String buf = new String(m_buf, this.m_r_begin, m_r_end
						- m_r_begin);
				filter_buf = onFilterBuffer(buf);
				// replace the buffer info.
				if (filter_buf != null)
				{
					m_buf = filter_buf.toCharArray();
					m_r_end = filter_buf.length();
					c = m_buf[m_r_begin++];
				}
				break;
			}

			// not the start flag.

			c = wk;

		} while (false);
		return c;
	}

	/**
	 * Read characters into a portion of an array.
	 */
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		int i = 0;
		int c = -1;
		for (; i < len; i++)
		{
			c = read();
			if (c == -1)
			{
				break;
			}
			cbuf[off + i] = (char) c;
		}
		return i == 0 ? -1 : i;
	}

	protected abstract String onFilterBuffer(String buf) throws IOException;

}
