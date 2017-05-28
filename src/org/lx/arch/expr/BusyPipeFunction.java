package org.lx.arch.expr;

import java.util.ArrayList;

import org.lx.arch.BusyPipe;
import org.lx.util.StringEx;

public class BusyPipeFunction implements IExprFunction
{
	BusyPipe m_busyPipe = null;

	public BusyPipeFunction()
	{
		m_busyPipe = new BusyPipe();
	}

	public Object handle(Object[] params) throws ExprException
	{
		// 1. the firts parameter is timeGrid.
		Object tg = params[0];
		Object[] tgs = StringEx.split(tg.toString(), ";");

		long[] timeGrid = new long[tgs.length];
		for (int i = 0; i < tgs.length; i++)
		{
			timeGrid[i] = Long.parseLong(tgs[i].toString());
		}

		ArrayList paramsList = new ArrayList();
		for (int i = 1; i < params.length; i++)
		{
			paramsList.add(params[i]);
		}
		String key = String.valueOf(paramsList.hashCode());
		boolean bBusy = m_busyPipe.isBusy(timeGrid, key);
		return Boolean.toString(bBusy);
	}
}
