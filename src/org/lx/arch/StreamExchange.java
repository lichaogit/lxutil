package org.lx.arch;

import java.util.Map;

public interface StreamExchange
{
	public Object fromString(String express, Class clazz);

	public String toString(Object obj, Map convertIinfo);
}
