package org.lx.util;

public class InheritValueRender implements ValueRender
{
	ValueRender m_parent;

	ValueRender m_vr;

	public InheritValueRender(ValueRender parent, ValueRender cur)
	{
		m_parent = parent;
		m_vr = cur;
	}

	public Object get(String key)
	{
		Object retval = null;
		if (m_parent != null)
		{
			retval = m_parent.get(key);
		}
		if (retval == null)
		{
			retval = m_vr.get(key);
		}
		return retval;
	}
}
