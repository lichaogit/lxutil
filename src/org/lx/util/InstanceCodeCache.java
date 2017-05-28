package org.lx.util;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003-2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class InstanceCodeCache extends Cache
{
  public InstanceCodeCache()
  {
  }

  protected Object load(Object key)
  {
    Object retval = null;
    Class cls = null;
    if (key instanceof Object[])
    {
      Object[] info = (Object[]) key;
      cls = (Class) info[0];
      try
      {
        retval = CodeGenerator.genInterfaceInstanceCode(cls, (String) info[1]);
      } catch (Exception ex)
      {
        Log.log(Log.WARNING, null, ex);
      }

    }
    return retval;
  }

}