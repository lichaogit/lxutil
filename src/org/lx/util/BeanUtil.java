package org.lx.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class BeanUtil
{

	/**
	 * get the value of specific component's attribute
	 * @param com
	 * @param attrName
	 * @return
	 */
	public static Object getBeanValue(Object com, String attrName)
			throws SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		Object retval = null;
		Method m = getGetterMethod(com.getClass(), attrName);
		if (m != null)
		{
			retval = m.invoke(com, null);
		}
		return retval;
	}

	/**
	 * set the bean's value, you must specify the type because in some case the
	 * value''s class is the derived from the type, such as BorderLayout and the
	 * LayoutManager
	 * @param com
	 * @param attrName
	 * @param type
	 * @param value
	 */
	public static void setBeanValue(Object com, String attrName, Class type,
			Object value) throws SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		Method setMethod = getSetterMethod(com.getClass(), attrName, type);
		if (setMethod != null)
		{
			setMethod.invoke(com, new Object[] { value });
		}
	}

	/**
	 * set the attribute's value for specific attribute with the specific value.
	 * @param com
	 * @param attrName
	 * @param value
	 */
	public static void setBeanValue(Object com, String attrName, Object value)
			throws SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		Class c = com.getClass();
		Method getMethod = BeanUtil.getGetterMethod(c, attrName);

		if (getMethod != null)
		{
			/* get the attribute's type */
			Class attrType = getMethod.getReturnType();
			setBeanValue(com, attrName, attrType, value);
		}
	}

	public static Method getGetterMethod(Class c, String attrName)
	{
		return getGetterMethod(c, attrName, null);
	}

	/**
	 * decode the specific to plain String, you may override this method if you
	 * have another object to decode
	 * @param cls
	 * @param obj
	 * @return
	 */
	public static String decode(Object obj)
	{
		String retval = null;
		// basic type.
		StringBuffer buf = new StringBuffer();
		if (obj.getClass().isPrimitive() || obj instanceof Byte
				|| obj instanceof Short || obj instanceof Integer
				|| obj instanceof Long || obj instanceof Character
				|| obj instanceof Boolean || obj instanceof Float
				|| obj instanceof Double)
		{
			retval = obj.toString();
		} else if (obj.getClass().isArray())
		{
			// explain array with ','
			Object[] array = (Object[]) obj;
			Object tmp = null;
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
			// buf.append(array[array.length - 1].toString());
			retval = buf.toString();
		} else if (obj instanceof Dimension)
		{
			buf.append(((Dimension) obj).getWidth());
			buf.append(',');
			buf.append(((Dimension) obj).getHeight());
			retval = buf.toString();
		} else if (obj instanceof Rectangle)
		{
			buf.append(((Rectangle) obj).getX());
			buf.append(',');
			buf.append(((Rectangle) obj).getY());
			buf.append(',');
			buf.append(((Rectangle) obj).getWidth());
			buf.append(',');
			buf.append(((Rectangle) obj).getHeight());
			retval = buf.toString();

		} else if (obj instanceof Color)
		{
			retval = "#"
					+ Integer.toHexString(((Color) obj).getRGB() & 0x00ffffff);
		} else if (obj instanceof Insets)
		{
			buf.append(((Insets) obj).top);
			buf.append(',');
			buf.append(((Insets) obj).left);
			buf.append(',');
			buf.append(((Insets) obj).bottom);
			buf.append(',');
			buf.append(((Insets) obj).right);
			retval = buf.toString();
		} else if (obj instanceof Font)
		{
			retval = ((Font) obj).getName();
		} else if (obj != null)
		{
			retval = obj.toString();
		}
		return retval;
	}

	/**
	 * get the component's getter Method for fetch the attribute value
	 * @param com
	 * @param attrName
	 * @return
	 */
	public static Method getGetterMethod(Class c, String attrName,
			Class[] parameterTypes)
	{
		Method retval = null;
		// Class c = com.getClass();
		// 1.get the attribute type by getXXX's return type.
		StringBuffer methodName = new StringBuffer();
		methodName.append("get");
		methodName.append(Character.toUpperCase(attrName.charAt(0)));
		methodName.append(attrName.substring(1));
		retval = getMethod(c, methodName.toString(), parameterTypes);
		if (retval == null)
		{
			retval = getMethod(c, "is" + methodName.substring(3),
					parameterTypes);
		}

		if (retval == null)
		{
			Log.log(Log.WARNING, null, "can not find the getter for attribute:"
					+ c.getName() + "."
					+ Character.toLowerCase(methodName.charAt(3))
					+ methodName.substring(4));
		}

		return retval;
	}

	/**
	 * get the component's getter Method for fetch the attribute value
	 * @param com
	 * @param attr
	 * @return
	 */
	public static Method getSetterMethod(Class c, String attrName, Class type)
	{
		Method retval = null;
		// 1.get the attribute type by getXXX's return type.
		StringBuffer methodName = new StringBuffer();
		methodName.append("set");
		methodName.append(Character.toUpperCase(attrName.charAt(0)));
		methodName.append(attrName.substring(1));
		retval = getMethod(c, methodName.toString(), new Class[] { type });
		if (retval == null)
		{
			Log.log(Log.WARNING, null, "can not find the setter for attribute:"
					+ c.getName() + "."
					+ Character.toLowerCase(methodName.charAt(3))
					+ methodName.substring(4));
		}

		return retval;
	}

	/**
	 * encode a String to a attrType's Object, you may overrider this method if
	 * you have another Type to encode.
	 * @param attrType
	 * @param attr
	 * @return
	 */
	public static Object encode(Class attrType, String attr)
	{
		Object retval = null;
		try
		{
			if (attrType.equals(String.class))
			{
				retval = attr;
			} else if (attrType.getName().equals("byte")
					|| attrType.equals(Byte.class))
			{
				retval = Byte.valueOf(attr == null || attr.length() == 0 ? "0"
						: attr);

			} else if (attrType.getName().equals("short")
					|| attrType.equals(Short.class))
			{
				retval = Short.valueOf(attr == null || attr.length() == 0 ? "0"
						: attr);
			} else if (attrType.getName().equals("int")
					|| attrType.equals(Integer.class))
			{
				retval = Integer
						.valueOf(attr == null || attr.length() == 0 ? "0"
								: attr);
			} else if (attrType.getName().equals("long")
					|| attrType.equals(Long.class))
			{
				retval = Long.valueOf(attr == null || attr.length() == 0 ? "0"
						: attr);
			} else if (attrType.getName().equals("float")
					|| attrType.equals(Float.class))
			{
				retval = Float.valueOf(attr == null || attr.length() == 0 ? "0"
						: attr);
			} else if (attrType.getName().equals("double")
					|| attrType.equals(Double.class))
			{
				retval = Double
						.valueOf(attr == null || attr.length() == 0 ? "0"
								: attr);
			} else if (attrType.getName().equals("char")
					|| attrType.equals(Character.class))
			{
				retval = new Character(attr.charAt(0));

			} else if (attrType.getName().equals("boolean")
					|| attrType.equals(Boolean.class))
			{
				retval = Boolean
						.valueOf(attr == null || attr.length() == 0 ? "false"
								: attr);
			} else if (attrType.isArray())
			{
				Object[] array = attr.split(",");
				retval = array;
			} else if (attrType.equals(Dimension.class))
			{
				String[] values = attr.split(",");
				if (values != null && values.length == 2)
				{
					retval = new Dimension(Integer.valueOf(values[0])
							.intValue(), Integer.valueOf(values[1]).intValue());
				}

			} else if (attrType.equals(Rectangle.class))
			{
				String[] values = attr.split(",");
				if (values != null && values.length == 4)
				{

					retval = new Rectangle(Integer.valueOf(values[0])
							.intValue(), Integer.valueOf(values[1]).intValue(),
							Integer.valueOf(values[2]).intValue(), Integer
									.valueOf(values[3]).intValue());

				}
			} else if (attrType.equals(Color.class))
			{
				retval = Color
						.decode(attr == null || attr.length() == 0 ? "#ffffff"
								: attr);

			} else if (attrType.equals(Insets.class))
			{
				String[] values = attr.split(",");
				if (values != null && values.length == 4)
				{
					retval = new Insets(Integer.valueOf(values[0]).intValue(),
							Integer.valueOf(values[1]).intValue(), Integer
									.valueOf(values[2]).intValue(), Integer
									.valueOf(values[3]).intValue());
				}
			} else if (attrType.equals(Icon.class) && attr != null)
			{
				if ((new File(attr)).exists())
				{
					retval = new ImageIcon(attr);
				} else
				{
					String jarAttr = attr.startsWith("/") ? attr : "/"
							.concat(attr);
					URL url = Icon.class.getResource(jarAttr);
					if (url != null)
					{
						retval = new ImageIcon(url);
					}
				}
			} else if (attrType.equals(Font.class) && attr != null)
			{
				retval = new Font(attr, 0, 20);
			}
		} catch (NumberFormatException e)
		{
			// do nothig, skip
		}
		if (retval == null && attr != null)
		{

			// search in class's field
			try
			{
				Field f = attrType.getField(attr);
				if (f != null)
				{
					retval = f.get(null);
				}
			} catch (NoSuchFieldException e)
			{

			} catch (IllegalArgumentException e)
			{

			} catch (IllegalAccessException e)
			{

			}
		}
		return retval;
	}

	/**
	 * get the specific method from the obj.
	 * @param obj
	 * @param methodName
	 */
	public static Method getMethod(Class c, String methodName,
			Class[] parameterTyps)
	{
		Method retval = null;
		try
		{
			retval = c.getMethod(methodName, parameterTyps);
		} catch (SecurityException ex)
		{
			Log.log(Log.WARNING, null, ex);
		} catch (NoSuchMethodException ex)
		{

		} catch (IllegalArgumentException ex1)
		{
			Log.log(Log.WARNING, null, ex1);
		}
		return retval;
	}

	// set the specific attribue's value, the attribute can divide by the '.'
	public static void setPropValue(Object com, String objName, Object value)
			throws SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		int offset = objName.lastIndexOf(".");
		String pObjName = offset == -1 ? "" : objName.substring(0, offset);
		String attrName = offset == -1 ? objName : objName
				.substring(offset + 1);
		Object parent = getPropValue(com, pObjName);
		if (parent != null)
		{
			setBeanValue(parent, attrName, value);
		}
	}

	// get the export's object.
	// the objName is like chart.title.text
	public static Object getPropValue(Object com, String objName)
			throws SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException

	{
		Object retval = null;
		if (retval == null)
		{
			if (objName == null || objName.trim().length() == 0)
			{
				retval = com;
			} else
			{
				// get the Object
				String[] objs = objName.split("\\.");
				Object work = com;
				for (int i = 0; i < objs.length && work != null; i++)
				{
					// skip the blank element.
					if (objs[i] == null)
					{
						continue;
					}
					work = getBeanValue(work, objs[i]);
				}

				retval = work;
			}
		}
		return retval;
	}
}
