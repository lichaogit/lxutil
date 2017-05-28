package org.lx.util;

import java.io.IOException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003-2005</p>
 * <p>Company: uilogic</p>
 * @author zhaolc
 * @version 1.0
 */

public abstract class NamedCache extends Cache
{
  public NamedCache()
  {
  }

  /**
   * convert the key to the real id.
   * @param key
   * @return
   */
  public abstract Object resolve(Object key);

  /**
   *the call back when value be loaded.
   * @param key
   * @param value
   * @return
   */
  public boolean onLoad(Object key, Object value)
  {
    return true;
  }

  public Object get(Object key)throws IOException
  {
    Object retval = null;
    retval = getFromCache(key);
    if (retval == null)
    {
      retval = load(resolve(key));
      if (retval != null && onLoad(key, retval))
      {
        add(key, retval);
      }
    }
    return retval;
  }

}