/*
 * Universal framework, Make things simple.
 *
 * Copyright (c) 2015, ShangHai JiuYou Network Technology Co., Ltd.
 * 
 * CreateBy Lichao.Zhao
 *  23988702@qq.com
 * 
 *  History:
 *  
 */
package org.lx.arch.plugin;

import java.util.Map;

public interface IActionContext
{
	public Object getParameter(String attrname);

	public void setParameter(String attrname, Object val);

	public Map getParameters();

	public void setReturnResult(Object val);

	public Object getReturnResult();

}
