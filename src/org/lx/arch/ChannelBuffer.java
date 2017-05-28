package org.lx.arch;

import java.nio.ByteBuffer;

public class ChannelBuffer
{
	ByteBuffer rBuf;

	ByteBuffer wBuf;

	public ChannelBuffer(ByteBuffer rBuf, ByteBuffer wBuf)
	{
		this.rBuf = rBuf;
		this.wBuf = wBuf;
	}

	public ByteBuffer getReadBuffer()
	{
		return rBuf;
	}

	public ByteBuffer getWriteBuffer()
	{
		return wBuf;
	}
}