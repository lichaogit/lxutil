package org.lx.util;

import java.io.IOException;
import java.util.Map;

/*
 * Created on 2006-2-5
 * 
 * create the specific Object by object class's ClassManager <P> and object
 * class's Hook Object, some object can not be created <P> by the reflect base
 * on the object class,because them have no <P> non-parameter constructor, so
 * IObjectCreatorHook interface is <P> used to create a such a object.
 *  
 */
public class ObjectCreator extends ObjectCache
{
	// store the component class info.
	// protected ObjectCache m_cm_com;

	// store the hook class.
	// protected ObjectCache m_cm_hook;

	protected boolean m_tagCase;

	public boolean isTagCase()
	{
		return m_tagCase;
	}

	public void setTagCase(boolean bCase)
	{
		m_tagCase = bCase;
	}

	public ObjectCreator()
	{
		super(null);
	}

	/**
	 * register a component wrapper for a tag to ComFactory dynamically.
	 * @param tagName
	 *        the tag name.
	 * @param wrapper
	 *        Class.
	 */
	public void registerComClass(String name, String clazz)
	{
		registerClass(getClassManager(), name, clazz);
	}

	/**
	 * register a ComHook to ComFactory dynamically.
	 * @param tagName
	 *        the tag name.
	 * @param wrapper
	 *        Class.
	 */
	public void registerHookClass(String name, String clazz)
	{
		name = isTagCase() ? name : name.toLowerCase();
		getClassManager().addClassInfo(name, clazz);
	}

	/**
	 * add a class information to the specific classManager.
	 * @param cm
	 * @param name
	 * @param clazz
	 */
	public void registerClass(ClassManager cm, String name, String clazz)
	{
		name = isTagCase() ? name : name.toLowerCase();
		cm.addClassInfo(name, clazz);
	}

	/**
	 * create the ui component for the specific comid
	 * @param comId
	 * @return
	 * @throws IOException
	 */
	public Object createComponent(String comId) throws IOException
	{
		return createComponent(comId, null);
	}

	/**
	 * create the ui component for the specific comid
	 * @param comId
	 * @param params
	 *        the parameters for create the Object instance.
	 * @return
	 * @throws IOException
	 */
	public Object createComponent(String comId, Map params) throws IOException
	{
		Object retval = null;
		String id = isTagCase() ? comId : comId.toLowerCase();
		IObjectCreatorHook ch = getHook(id);
		if (ch != null)
		{
			retval = createComponent(ch, id, params);
		}
		// if the ComHook have not provide component, try to
		// create the ui component with non-parameter constructor.
		if (retval == null)
		{
			retval = get(id);
		}
		return retval;
	}

	/**
	 * create the object with the specific component hook.
	 * @param hook
	 * @param comId
	 * @param params
	 * @return
	 * @throws IOException
	 */
	protected Object createComponent(IObjectCreatorHook hook, String comId,
			Map params) throws IOException
	{
		ClassManager cm = getClassManager();
		return hook.createComponent(cm.getClass(comId), params);
	}

	/**
	 * get the specific component's hook interface.
	 * @param comId
	 * @return
	 */
	public IObjectCreatorHook getHook(String comId)
	{
		IObjectCreatorHook retval = null;
		try
		{
			comId = isTagCase() ? comId : comId.toLowerCase();
			Object obj = get(comId);
			retval = obj instanceof IObjectCreatorHook ? (IObjectCreatorHook) obj
					: null;
		} catch (IOException e)
		{

		}
		return retval;
	}
}