package org.lx.util;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public abstract class MGAction extends AbstractAction
{
  //{{{ Private members
  private String m_name;
  private static HashMap m_AllActionMap = new HashMap();
  //}}}
  public static MGAction getDummyAction(String name)
  {
    return new DummyAction(name);
  }

  public boolean isEnabled()
  {
    return false;
  }

  /**
   * this action will map the action to the real action through MGAction
   * for JMenuItem.
   */
  private static class DummyAction extends MGAction
  {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6759328385020329376L;
	private String m_name;
    public boolean isEnabled()
    {
      boolean retval = false;
      MGAction action = MGAction.getAction(m_name);
      if (action != null)
      {
        retval = action.isEnabled();
      }
      return retval;
    }

    public DummyAction(String name)
    {
      super("__dummy");
      m_name = name;
    }

    public void actionPerformed(ActionEvent evt)
    {

      MGAction action = MGAction.getAction(m_name);
      if (action != null && action.isEnabled())
      {
        action.actionPerformed(evt);
      }

    }
  }

  /**
   * action for multi menuitem
   * @param name
   */
  public MGAction(String[] supportList)
  {
    this(supportList, null);
  }

  public MGAction(String[] supportList, String Name)
  {
    for (int i = 0; i < supportList.length; i++)
    {
      addAction(supportList[i], this);
    }
    this.m_name = Name;
  }

  public MGAction(String name)
  {
    this.m_name = name;
    /*
         //1. if the specific name action is existed , delete it firstly
         MGAction existedAction = getAction(name);
         if (existedAction != null)
         {
      m_AllActionMap.remove(existedAction);
         }
     */
    //2. register this action
    addAction(name, this);
  }

  public static void addAction(String Name, AbstractAction action)
  {

    m_AllActionMap.put(Name, action);
  }

  public static Iterator getAction()
  {
    Iterator retval = null;
    if (m_AllActionMap.size() != 0)
    {
      retval = m_AllActionMap.entrySet().iterator();
    }
    return retval;
  }

  public static MGAction getAction(String actionName)
  {
    return actionName == null ? null : (MGAction) m_AllActionMap.get(actionName);
  }

  public abstract void actionPerformed(ActionEvent evt);

  //{{{ getMouseOverText() method
  /**
   * Returns the text that should be shown when the mouse is placed over
   * this action's menu item or tool bar button. Currently only used by
   * the macro system.
   * @since jEdit 4.0pre5
   */
  public String getMouseOverText()
  {
    return null;
  } //}}}

  //{{{ isToggle() method
  /**
   * Returns if this edit action should be displayed as a check box
   * in menus.
   * @since jEdit 2.2pre4
   */
  public boolean isToggle()
  {
    return false;
  } //}}}

  //{{{ getName() method
  /**
   * Returns the internal name of this action.
   */
  public String getName()
  {
    return m_name;
  } //}}}

}