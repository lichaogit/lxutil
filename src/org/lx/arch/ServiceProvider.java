package org.lx.arch;

import java.util.HashMap;

/**
 * manage the global resource for whole App.
 * @author Lichao.zhao
 */
public class ServiceProvider
{
	protected HashMap m_services = new HashMap();

	private static ServiceProvider inst;

	public Object getService(String serviceId)
	{
		Object retval = null;
		synchronized (m_services)
		{
			retval = m_services.get(serviceId);
		}
		return retval;
	}

	public void registeService(String serviceId, Object service)
	{
		synchronized (m_services)
		{
			m_services.put(serviceId, service);
		}
	}

	final public static ServiceProvider getInstance()
	{
		if (inst == null)
		{
			inst = new ServiceProvider();
		}
		return inst;
	}

}
