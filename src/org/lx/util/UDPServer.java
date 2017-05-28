package org.lx.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPServer
{
	private static DatagramSocket _ds = null;

	public UDPServer(String host, int port) throws Exception
	{
		_ds = new DatagramSocket(new InetSocketAddress(host, port));

	}

	public final byte[] receive(int len) throws IOException
	{
		byte[] buffer = new byte[len];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		_ds.receive(packet);
		return packet.getData();
	}

	public final void response(byte[] data, InetAddress target, int port)
			throws IOException
	{
		byte[] sendBuffer = new byte[1024];
		DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length,
				target, port);
		dp.setData(data);
		_ds.send(dp);
	}

}
