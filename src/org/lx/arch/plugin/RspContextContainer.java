package org.lx.arch.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RspContextContainer implements IRspContext
{
	List<IRspContext> _ctx = new ArrayList<IRspContext>();

	public RspContextContainer()
	{

	}

	public void AddRspContext(IRspContext ctx)
	{
		_ctx.add(ctx);
	}

	@Override
	public Object write(Object rd) throws IOException
	{
		Object retval = rd;
		for (IRspContext item : _ctx)
		{
			retval = item.write(retval);
		}
		return retval;
	}
}
