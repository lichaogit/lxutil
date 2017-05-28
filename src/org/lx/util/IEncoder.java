package org.lx.util;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003-2005</p>
 * <p>Company: uilogic</p>
 * @author zhaolc
 * @version 1.0
 */

public interface IEncoder
{
  /**
   *translate the string to object.
   * @param str
   * @return
   */
  public Object encode(Class attrType, String str);

}