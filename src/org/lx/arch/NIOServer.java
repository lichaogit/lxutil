package org.lx.arch;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("rawtypes")
public abstract class NIOServer
{
	Pipe m_notifyPipe;

	SourceChannel _pipeChannel;

	Selector _selector;

	// SelectionKey m_notifyKey = null;

	volatile boolean m_loop_exit;

	protected static Log m_log = LogFactory.getLog(NIOServer.class);

	protected abstract void readKey(SelectionKey key);

	/**
	 * process the write operation.
	 * @param key
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected abstract void writeKey(SelectionKey key) throws IOException,
			InterruptedException;

	public void markKeyWriteable(SelectionKey key) throws IOException
	{
		key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
		_selector.wakeup();
	}

	public void start(boolean withUDP, boolean rcvUDPBrodcast, String host,
			int port) throws IOException
	{
		nioLoop(withUDP, rcvUDPBrodcast, host, port);
	}

	public void stop() throws IOException
	{
		m_loop_exit = true;
		// notify the nioloop.
		// SinkChannel sinkChannel = m_notifyPipe.sink();
		// ByteBuffer buf = ByteBuffer.allocate(1);
		// buf.put((byte) 0);
		// buf.flip();
		// sinkChannel.write(buf);
		_selector.wakeup();
	}

	public SourceChannel getPipeChannel()
	{
		return _pipeChannel;
	}

	// REF:http://blog.csdn.net/foart/article/details/47608475
	protected void nioLoop(boolean withUDP, boolean rcvUDPBrodcast,
			String host, int port) throws IOException
	{
		_selector = Selector.open();
		InetSocketAddress srvAddr = new InetSocketAddress(host, port);

		ServerSocketChannel tcpCh = ServerSocketChannel.open();
		ServerSocket tcpSocket = tcpCh.socket();
		tcpSocket.bind(srvAddr);
		tcpCh.configureBlocking(false);
		SelectionKey key = tcpCh.register(_selector, SelectionKey.OP_ACCEPT);

		if (withUDP)
		{
			DatagramChannel udpCh = DatagramChannel.open();
			DatagramSocket udpSock = udpCh.socket();
			InetSocketAddress udpAddr = rcvUDPBrodcast ? new InetSocketAddress(
					port) : srvAddr;
			// udpSock.bind(srvAddr);
			udpSock.bind(udpAddr);
			udpCh.configureBlocking(false);
			SelectionKey udpKey = udpCh.register(_selector,
					SelectionKey.OP_READ);
			// Map is used to store buf->remoteAddr
			udpKey.attach(new HashMap());
		}

		// the pipe to notify socket operations.
		m_notifyPipe = Pipe.open();
		_pipeChannel = m_notifyPipe.source();
		_pipeChannel.configureBlocking(false);
		SelectionKey pipeKey = _pipeChannel.register(_selector,
				SelectionKey.OP_READ);
		// the buffer for read.
		ChannelBuffer pipeRWBuf = new ChannelBuffer(ByteBuffer.allocate(1024),
				ByteBuffer.allocate(1024));
		pipeKey.attach(pipeRWBuf);

		ServerSocketChannel tcpSrvCh = null;
		// the socket listener loop.
		while (!m_loop_exit)
		{
			int keysAdded = _selector.select();
			if (keysAdded == 0)
			{
				m_log.warn("no key to be precessed");
				continue;
			}

			Iterator it = _selector.selectedKeys().iterator();
			while (it.hasNext())
			{
				key = (SelectionKey) it.next();
				it.remove();

				try
				{
					// Make sure the key is still valid
					if (!key.isValid())
					{
						m_log.warn("invalid key:" + key);
						continue;
					}
					if (!key.channel().isOpen())
					{
						m_log.warn("channel is not open:" + key.channel());
						continue;
					}

					// accept the remote connection as non-blocking
					if (key.isAcceptable())
					{
						tcpSrvCh = (ServerSocketChannel) key.channel();
						SocketChannel tcpClientChannel = tcpSrvCh.accept();
						tcpClientChannel.configureBlocking(false);
						SelectionKey clientKey = tcpClientChannel.register(
								_selector, SelectionKey.OP_READ);

						// create R/W buffer for this client channel.
						ByteBuffer rBuf = ByteBuffer.allocate(1024);
						ByteBuffer wBuf = ByteBuffer.allocate(1024);
						ChannelBuffer buf = new ChannelBuffer(rBuf, wBuf);
						clientKey.attach(buf);

						m_log.info("accept socket="
								+ tcpClientChannel.socket().toString());
					} else if (key.isReadable())
					{
						// NIOReactor reactor = (NIOReactor) key.attachment();
						// reactor.execute(key);

						// SourceChannel
						SelectableChannel channel = key.channel();
						// process the pipe event
						if (channel instanceof SourceChannel)
						{
							// do nothing, just wake up the NIO loop.
							continue;
						}

						// read the whole command and then deliver to the thread
						// pool.
						readKey(key);

					} else if (key.isWritable())
					{
						writeKey(key);
						// clear the write flag.
						// key.interestOps(key.interestOps()
						// & ~SelectionKey.OP_WRITE);
					}
				} catch (Exception e)
				{
					m_log.warn(e.getLocalizedMessage(), e);
					key.cancel();
				}
			}
		}
		_selector.close();
	}
}
