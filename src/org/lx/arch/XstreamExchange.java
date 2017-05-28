package org.lx.arch;

import java.util.Map;

import com.thoughtworks.xstream.XStream;

public class XstreamExchange implements StreamExchange
{
	XStream xstream = new XStream();

	public Object fromString(String express, Class clazz)
	{
		return xstream.fromXML(express);
	}

	public String toString(Object obj, Map convertIinfo)
	{
		return xstream.toXML(obj);
	}

}
