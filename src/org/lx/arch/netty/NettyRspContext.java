package org.lx.arch.netty;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import org.lx.arch.plugin.IRspContext;

public class NettyRspContext implements IRspContext
{
	ChannelHandlerContext _ctx;

	public NettyRspContext(ChannelHandlerContext ctx)
	{
		_ctx = ctx;
	}

	@Override
	public Object write(Object rd) throws IOException
	{
		_ctx.write(rd);
		return null;
	}
}
