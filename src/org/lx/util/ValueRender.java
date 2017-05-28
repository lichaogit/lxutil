package org.lx.util;

public interface ValueRender
{
	/**
	 * return the new value for the key.
	 * @param old_value
	 * @return
	 */
	public Object get(String key);
}