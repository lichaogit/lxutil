package org.lx.util;

import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: this class extends some function of ResourceBundle, but it not extends from it</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author zhaolc
 * @version 1.0
 *
 *
 */

public class ResourceManager
{
  // Resource bundle for internationalized and accessible text
  private ResourceBundle m_bundle = null;
  private String m_resBaseName = null;
  public ResourceManager(String baseName) throws FileNotFoundException
  {
    m_resBaseName = baseName;
    m_bundle = ResourceBundle.getBundle(m_resBaseName);
    if (m_bundle == null)
    {
      throw new FileNotFoundException(m_resBaseName);
    }
  }

  /**
   * Returns the resource bundle associated with this demo. Used
   * to get accessable and internationalized strings.
   */
  public ResourceBundle getBundle() throws FileNotFoundException
  {
    if (m_resBaseName != null)
    {
      m_bundle = ResourceBundle.getBundle(m_resBaseName);
      if (m_bundle == null)
      {
        throw new FileNotFoundException(m_resBaseName);
      }
    }
    return m_bundle;
  }

  public ResourceBundle getBundle(Locale locale) throws
      FileNotFoundException
  {
    if (m_resBaseName != null)
    {
      m_bundle = ResourceBundle.getBundle(m_resBaseName, locale);
      if (m_bundle == null)
      {
        throw new FileNotFoundException(m_resBaseName);
      }
    }
    return m_bundle;
  }

  /**
   * Returns a mnemonic from the resource bundle. Typically used as
   * keyboard shortcuts in menu items.
   */
  public char getMnemonic(String key) throws MissingResourceException,
      FileNotFoundException
  {
    return (getString(key)).charAt(0);
  }

  public String getString(String key)
  {
    return getStringEx(key, (String)null);
  }

  /**
   * TODO :remove this finction, use the getString.
   * This method returns a string from the demo's resource bundle.
   */
  public String getStringEx(String key, String defaultValue)
  {
    String value = null;
    try
    {
      value = getBundle().getString(key);
    } catch (FileNotFoundException e)
    {
      Log.log(Log.ERROR, ResourceManager.class, e);
    } catch (MissingResourceException e)
    {
      Log.log(Log.WARNING, ResourceManager.class, e.getMessage());
    }
    if (value == null)
    {
      value = defaultValue;
    }
    return value;
  }

  public String getString(String key, String str_locale) throws
      MissingResourceException
  {
    Locale locale = null;
    if (str_locale != null)
    {
      locale = new Locale(str_locale);
    }
    return getString(key, locale);
  }

  /**
   * This method returns a string from the demo's resource bundle.
   */
  public String getString(String key, Locale locale) throws
      MissingResourceException
  {
    String value = null;
    try
    {
      if (locale != null)
      {
        value = getBundle(locale).getString(key);
      } else
      {
        value = getBundle().getString(key);
      }
    } catch (MissingResourceException ex)
    {
      Log.log(Log.ERROR, null, ex.getMessage());
    } catch (FileNotFoundException e)
    {
      Log.log(Log.ERROR, null, e.getMessage());
    }
    return value;
  }
}