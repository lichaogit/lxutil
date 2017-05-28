/*
 * Universal framework, Make things simple.
 *
 * Copyright (c) 2015, ShangHai JiuYou Network Technology Co., Ltd.
 * 
 * IAction is designed for light-weight implementation for IPlugin.
 * CreateBy Lichao.Zhao
 *  23988702@qq.com
 * 
 *  History:
 *  
 */
package org.lx.arch.plugin;

import org.lx.arch.RESULT;
import org.lx.util.GeneralException;

public interface IAction
{
	public RESULT exec(String cmd, IActionContext context)
			throws GeneralException;

}
