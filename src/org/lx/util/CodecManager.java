package org.lx.util;

import java.io.IOException;
import java.util.Iterator;

import org.lx.xml.XMLElementModel;

public class CodecManager
{
	public final static String ATTR_TYPE = "type";

	public final static String ATTR_CLASS = "class";

	public static final String BLK_CODERS = "coders";

	public ObjectCache m_decoderManager;

	public ObjectCache m_coderManager;

	public CodecManager()
	{
		m_decoderManager = new ObjectCache();
		m_coderManager = new ObjectCache();
	}

	/**
	 * register a decoder info.
	 * @param name
	 *        the type
	 * @param className
	 *        the full name class.
	 */
	public void registerDecoder(String name, String className)
	{
		m_decoderManager.getClassManager().addClassInfo(name, className);
	}

	/**
	 * register the decoders in batches.
	 * @param model
	 */
	public void registerDecoder(XMLElementModel model)
	{
		Iterator it = model.getChildren().iterator();
		// 2.process the decoder
		String name = null;
		String clazz = null;
		while (it.hasNext())
		{
			model = (XMLElementModel) it.next();
			name = model.getAttribute(ATTR_TYPE);
			clazz = model.getAttribute(ATTR_CLASS);
			if (name != null && clazz != null)
			{
				m_decoderManager.getClassManager().addClassInfo(name.trim(),
						clazz);
			}
		}
	}

	/**
	 * register a coder info.
	 * @param name
	 *        the coder name
	 * @param className
	 *        the full name of class
	 */
	public void registerCoder(String name, String className)
	{
		m_coderManager.getClassManager().addClassInfo(name, className);
	}

	/**
	 * register the decoders in batches.
	 * @param model
	 */
	public void registerCoder(XMLElementModel model)
	{
		Iterator it = model.getChildren().iterator();
		// 2.process the decoder
		String name = null;
		String clazz = null;
		model = model.getChild(BLK_CODERS);
		while (it.hasNext())
		{
			model = (XMLElementModel) it.next();
			name = model.getAttribute(ATTR_TYPE);
			clazz = model.getAttribute(ATTR_CLASS);
			if (name != null && clazz != null)
			{
				m_coderManager.getClassManager().addClassInfo(name.trim(),
						clazz);
			}
		}
	}

	/**
	 * decode the specific Object to plain String.
	 * @param cls
	 * @param obj
	 * @return
	 */
	public String decode(Object obj)
	{
		String retval = null;
		Class type = obj.getClass();
		String name = type.getName();
		Object tmp;
		try
		{
			tmp = m_coderManager.get(name);
			IDecoder decoder = tmp == null ? null : (IDecoder) tmp;
			if (decoder != null)
			{
				retval = decoder.decode(obj);
			} else if (String.class.equals(type))
			{
				retval = obj.toString();
			} else if (type.isArray())
			{
				StringBuffer buf = new StringBuffer();
				// explain array with ','
				Object[] array = (Object[]) obj;
				buf.append('{');
				for (int i = 0; i < array.length; i++)
				{
					if (array[i] != null)
					{
						tmp = decode(array[i]);
						if (tmp != null)
						{
							buf.append(tmp);
						}
					}
					if (i < array.length - 1)
					{
						buf.append(',');
					}
				}
				buf.append('}');
				retval = buf.toString();
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * encode a plain string to a specific type's Object,
	 * @param type
	 * @param value
	 * @return
	 */
	public Object encode(Class type, String value)
	{
		Object retval = value;
		String name = type.getName();
		Object tmp;
		try
		{
			tmp = m_coderManager.get(name);
			IEncoder coder = tmp == null ? null : (IEncoder) tmp;
			if (coder != null)
			{
				retval = coder.encode(type, value);
			} else if (String.class.equals(type))
			{
				retval = value;
			} else if (type.isArray())
			{
				String[] array = value.split(",");
				Object[] ao = new Object[array.length];
				for (int i = 0; i < array.length; i++)
				{
					ao[i] = encode(type.getComponentType(), array[i]);
				}

				retval = (Object[]) java.lang.reflect.Array.newInstance(type
						.getComponentType(), array.length);

				System.arraycopy(ao, 0, retval, 0, array.length);

			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retval;
	}
}
