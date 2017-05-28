package org.lx.io;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The BufferedFilterOutputStream will buffer when the start flag matched and
 * process the buffer when end flag matched or buffer full.
 * @author zlc
 */
public abstract class BufferedFilterOutputStream extends FilterOutputStream
{
	ByteArrayOutputStream m_buf;

	int m_maxSize;

	int m_writtenBytes = 0;

	public int getBufSize()
	{
		return m_buf.size();
	}

	public BufferedFilterOutputStream(OutputStream out, int maxSize)
	{
		super(out);
		//buffer all of the content.
		if (maxSize == -1)
		{
			m_buf = new ByteArrayOutputStream();
		} else
		{
			m_buf = new ByteArrayOutputStream(maxSize);
		}
		m_maxSize = maxSize;
	}

	/* do not buffered as default */
	protected boolean isStartBufFlag(int b)
	{
		return false;
	}

	protected boolean isEndBufFlag(int b)
	{
		return true;
	}

	public void write(int b) throws IOException
	{
		// meet start buffer char
		if (isStartBufFlag(b) || m_buf.size() > 0)
		{
			m_buf.write(b);
		} else
		{
			// flush directly
			super.out.write(b);
		}

		// if buffer full or meet end.
		if ((m_maxSize != -1 && m_maxSize <= m_buf.size()) || isEndBufFlag(b))
		{
			flush();
		}
	}

	public void write(byte[] b) throws IOException
	{
		if (m_maxSize == -1)
		{
			m_buf.write(b);
			checkBuffer();
		} else
		{
			write(b, 0, b.length);
		}
	}

	public void write(byte[] b, int off, int len) throws IOException
	{
		if (m_maxSize == -1)
		{
			m_buf.write(b, off, len);
			checkBuffer();
		} else
		{
			for (int i = off; i < off + len; i++)
			{
				write(b[i]);
			}
		}
	}

	/**
	 * writer the buffer to outputStream if the buffer is full.
	 * @throws IOException
	 */
	protected void checkBuffer() throws IOException
	{
		if (m_maxSize != -1 && m_maxSize <= m_buf.size())
		{
			flush();
		}
	}

	public abstract byte[] onFilterBuffer(byte[] b) throws IOException;

	public void flush() throws IOException
	{
		if (m_buf.size() > 0)
		{
			byte[] b = onFilterBuffer(m_buf.toByteArray());
			if (b != null)
			{
				super.out.write(b);
			}
			reset();
		}
	}

	protected void reset()
	{
		m_buf.reset();
	}
}
