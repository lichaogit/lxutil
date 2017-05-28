package org.lx.arch.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NettyServer
{
	protected static Log m_log = LogFactory.getLog(NettyServer.class);

	private ChannelFuture channel;

	private final EventLoopGroup masterGroup;

	private final EventLoopGroup slaveGroup;

	public NettyServer(int srvNum, int clientNum)
	{
		masterGroup = new NioEventLoopGroup(srvNum);
		slaveGroup = new NioEventLoopGroup(clientNum);
	}

	public static SslContext getSSLContext() throws SSLException,
			CertificateException
	{
		boolean SSL = System.getProperty("ssl") != null;
		final SslContext sslCtx;
		if (SSL)
		{
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(),
					ssc.privateKey()).build();
		} else
		{
			sslCtx = null;
		}
		return sslCtx;
	}

	public static SslContext getHttpSSLContext() throws SSLException,
			CertificateException
	{
		boolean SSL = System.getProperty("ssl") != null;
		// Configure SSL.
		SslContext sslCtx = null;
		if (SSL)
		{
			SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL
					: SslProvider.JDK;
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder
					.forServer(ssc.certificate(), ssc.privateKey())
					.sslProvider(provider)
					/*
					 * NOTE: the cipher filter may not include all ciphers
					 * required by the HTTP/2 specification. Please refer to the
					 * HTTP/2 specification for cipher requirements.
					 */
					.ciphers(Http2SecurityUtil.CIPHERS,
							SupportedCipherSuiteFilter.INSTANCE)
					.applicationProtocolConfig(
							new ApplicationProtocolConfig(
									Protocol.ALPN,
									// NO_ADVERTISE is currently the only mode
									// supported by both OpenSsl and JDK
									// providers.
									SelectorFailureBehavior.NO_ADVERTISE,
									// ACCEPT is currently the only mode
									// supported by both OpenSsl and JDK
									// providers.
									SelectedListenerFailureBehavior.ACCEPT,
									ApplicationProtocolNames.HTTP_2,
									ApplicationProtocolNames.HTTP_1_1)).build();
		}
		return sslCtx;
	}

	public void start(String inetHost, int port,
			ChannelInitializer<SocketChannel> chInitializer,
			boolean waitUtilExit) // #1
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				shutdown();
			}
		});

		try
		{
			// #3
			final ServerBootstrap bootstrap = new ServerBootstrap()
					.group(masterGroup, slaveGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childHandler(chInitializer);

			// Start the server.
			channel = inetHost == null ? bootstrap.bind(port).sync()
					: bootstrap.bind(inetHost, port).sync();
			// ReferenceCountUtil.release(delimiter);

			// Wait until the server socket is closed.
			if (waitUtilExit)
			{
				channel.channel().closeFuture().sync();
			}
		} catch (InterruptedException e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
	}

	public void shutdown() // #2
	{
		slaveGroup.shutdownGracefully();
		masterGroup.shutdownGracefully();

		try
		{
			channel.channel().closeFuture().sync();
		} catch (InterruptedException e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
	}
}
