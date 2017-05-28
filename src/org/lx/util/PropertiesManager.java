package org.lx.util;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author zhao lichao
 * @version 1.0
 */

public class PropertiesManager
{

  private Collection m_vElements = new Vector();
  private Map m_storeMap;
  public PropertiesManager()
  {

  }

  public PropertiesManager(Collection pms)
  {
    m_vElements = pms;
  }

  public PropertiesManager generatePM()
  {
    PropertiesManager retval = (PropertiesManager) clone();
    return retval;
  }

  /**
   * copy the pm
   * @param pm
   */
  public Object clone()
  {
    PropertiesManager retval = new PropertiesManager();
    Iterator it = m_vElements.iterator();
    Object ele = null;
    while (it.hasNext())
    {
      ele = it.next();
      if (ele instanceof Map)
      {
        retval.addProperties( (Map) ele);
      } else if (ele instanceof PropertiesManager)
      {
        retval.addPM( (PropertiesManager) ele);
      }
    }
    return retval;
  }

  /**
   * add a Map to m_vMap
   * @param map
   */
  public void addProperties(Map map)
  {
    m_vElements.add(map);
  }

  public void addPM(PropertiesManager pm)
  {
    m_vElements.add(pm);
  }

  /**
   * return the Map result's size
   * @return
   */
  public int size()
  {
    return m_vElements.size();
  }

  /**
   * get the specific Map result.
   * @param index
   * @return
   */
//  public Map getProperties(int index)
//  {
//    Map retval = null;
//    if (index < m_vElements.size())
//    {
//      retval = (Map) m_vElements.toArray(new Map[0])[index];
//    }
//    return retval;
//  }

  /**
   * remove the specific Map
   * @param map
   */
  public void removeProperties(Map map)
  {

    Iterator it = m_vElements.iterator();
    while (it.hasNext())
    {

      if (map.equals(it.next()))
      {
        it.remove();
        break;
      }
    }

  }

  /**
   * set the properties
   * @param key
   * @param value
   * @return
   */
  public String setProperty(String key, String value)
  {
    Object retval = set(key, value);

    return retval == null ? null : retval.toString();
  }

  /**
   * return the map used to store data
   * @return
   */
  public Map getStoreMap()
  {
    Map retval = m_storeMap;
    if (retval == null)
    {

      Object[] eles = m_vElements.toArray();
      for (int i = eles.length - 1; i >= 0; i--)
      {
        if (eles[i] instanceof PropertiesManager)
        {
          retval = ( (PropertiesManager) eles[i]).getStoreMap();
        }
      }
    }
    return retval;
  }

  public void setStoreMap(Map map)
  {
    m_storeMap = map;
  }

  /**
   * set the Object properties
   * @param key
   * @param value
   * @return
   */
  public Object set(Object key, Object value)
  {
    Object retval = null;
    Map map = getStoreMap();
    if (map == null)
    {
      map = new Properties();
      setStoreMap(map);
    }
    map.put(key, value.toString());
    return retval;
  }

  /**
   * delete the key from the first properties
   * @param Key
   */
  public void resetProperty(Object Key)
  {
    Object lastEle = m_vElements.toArray()[m_vElements.size() - 1];
    if (lastEle instanceof Map)
    {
      ( (Map) lastEle).remove(Key);
    } else if (lastEle instanceof PropertiesManager)
    {
      ( (PropertiesManager) lastEle).resetProperty(Key);
    }
  }

  public String getProperty(String Key, String defaultValue)
  {
    String retval = null;
    retval = getProperty(Key);
    return retval == null ? defaultValue : retval;
  }

  public Object get(Object Key)
  {
    return get(Key, 0);
  }

  /**
   * get the value from the specific Map
   * @param Key
   * @param startIndex
   * @return
   */
  public Object get(Object Key, int startIndex)
  {
    Object retval = null;
    //Iterator it = m_vMap.iterator();
    Object[] eles = m_vElements.toArray();
    for (int i = eles.length - 1; i >= 0; i--)
    {
      //skip the Map
      if (startIndex-- > 0)
      {
        continue;
      }
      if (eles[i] instanceof Map)
      {
        retval = ( (Map) eles[i]).get(Key);
      } else if (eles[i] instanceof PropertiesManager)
      {
        retval = ( (PropertiesManager) eles[i]).get(Key);
      }
      if (retval != null)
      {
        break;
      }
      //if()
    }
    return retval;
  }

  /**
   * get the String value  from the specific startIndex.
   * @param key
   * @param startindex
   * @return
   */
  public String getProperty(String key, int startIndex)
  {
    Object retval = get(key, startIndex);
    return retval == null ? null : retval.toString();
  }

  /**
   * get the string value
   * @param Key
   * @return
   */
  public String getProperty(String key)
  {
    Object retval = get(key);
    return retval == null ? null : retval.toString();
  }
}