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

import org.lx.util.GeneralException;

public interface IPlugin
{
	/**
	 * init the plugin, the commands supported by current plugin cannot be
	 * called here.
	 * @param params
	 * @throws GeneralException
	 */
	public void init(Object params) throws GeneralException;

	/**
	 * start the plugin. commands supported by current plugin can be called
	 * here.
	 */
	public void start() throws GeneralException;;

	public void stop() throws GeneralException;;

	public void destory() throws GeneralException;

	public boolean isReady();

	public PluginManager getPluginManager();

}
