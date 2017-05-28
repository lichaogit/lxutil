package org.lx.arch;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class TCPIPUtils
{

	public static int udpSend(String host, int port, byte[] data)
			throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(data.length);
		buf.clear();
		buf.put(data);
		buf.flip();

		DatagramChannel channel = DatagramChannel.open();
		return channel.send(buf, new InetSocketAddress(host, port));
	}

}
