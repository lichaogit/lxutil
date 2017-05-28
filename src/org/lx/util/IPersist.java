package org.lx.util;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IPersist extends Serializable
{
  /**
   * store the logic instance to the outputStream.
   * @param out
   * @throws IOException
   */
  public void store(OutputStream out) throws IOException;

  /**
   * load the logic instance from inputStream.
   * @param in
   */
  public void load(InputStream in);

  /**
   * get the logic data
   * @return
   */
  public Object getData();
}