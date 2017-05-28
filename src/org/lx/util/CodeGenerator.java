package org.lx.util;

import java.lang.reflect.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003-2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CodeGenerator
{

  /**
   *
   * @param type
   * @return
   */
  public static String getTypeName(Class type)
  {
    if (type.isArray())
    {
      try
      {
        Class cl = type;
        int dimensions = 0;
        while (cl.isArray())
        {
          dimensions++;
          cl = cl.getComponentType();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(cl.getName());
        for (int i = 0; i < dimensions; i++)
        {
          sb.append("[]");
        }
        return sb.toString();
      } catch (Throwable e)
      {
        /*FALLTHRU*/}
    }
    return type.getName();
  }

  /**
   * generate the specific method's implementation code<P>
   * the callback is the implementation code.
   * @param method
   * @param callback
   * @return
   */
  public static String genMethodCode(Method method, String callback)
  {
    StringBuffer sb = new StringBuffer();
    //1.modifier
    sb.append("public ");
    //2.the return value.
    sb.append(getTypeName(method.getReturnType()) + " ");
    //3.the function name
    sb.append(method.getName() + "(");
    //4. the function's parameters
    Class[] params = method.getParameterTypes();
    for (int j = 0; j < params.length; j++)
    {
      sb.append(getTypeName(params[j]));
      sb.append(" p" + j);
      if (j < (params.length - 1))
      {
        sb.append(",");
      }
    }
    sb.append("){");
    //5. the function body;
    sb.append("Object []ps=new Object[" + params.length + "];");

    for (int i = 0; i < params.length; i++)
    {
      sb.append("ps[" + i + "]=p" + i + ";");
    }
    sb.append(callback + "(\"" + method.getName() + "\",ps);");
    sb.append('}');
    return sb.toString();
  }

  /**
   * generate the interface's implementation body code for specific interface.
   * @param if_cls
   * @param callback
   * @return
   */
  public static String genInterfaceBody(Class if_cls, String callback)
  {
    StringBuffer retval = new StringBuffer();
    do
    {
      Method[] ms = if_cls.getMethods();
      if (ms == null || ms.length == 0)
      {
        break;
      }
      for (int i = 0; i < ms.length; i++)
      {
        retval.append(genMethodCode(ms[i], callback));
      }
      Class[] cls = if_cls.getInterfaces();
      if (cls == null || cls.length == 0)
      {
        break;
      }

      for (int i = 0; i < cls.length; i++)
      {
        retval.append(genInterfaceBody(cls[i], callback));
      }
    } while (false);
    return retval.toString();
  }

  /**
   * generate the implementation code for specific interface.
   * @param if_cls the interface that need to be generated code.
   * @param callback the callback function that will be invoked when <P>
   * the method of the if_cls be called.
   * @return
   */
  public static String genInterfaceCode(Class if_cls, String callback)
  {
    StringBuffer retval = new StringBuffer();
    //1. the class header
    retval.append(if_cls.getName() + "(){ ");
    retval.append(genInterfaceBody(if_cls, callback));
    retval.append("}");
    return retval.toString();
  }

  /**
   * generate instance for specific interface, the implementation for <P>
   * the implementation for the interface's method is as following:
   * cb(Object []evts);
   * @param if_cls
   * @param param
   * @param cb
   * @return
   */
  public static String genInterfaceInstanceCode(Class if_cls, String cb) throws Exception
  {
    String retval = null;
    StringBuffer code = new StringBuffer();
    //1. the class header
    code.append("{final Object cb_var=param;");
    code.append("return new " + genInterfaceCode(if_cls, "cb_var." + cb));
    code.append(";}");

    //m_interpreter.setVariable("param", param);

    //retval = m_interpreter.eval(code.toString());
    retval = code.toString();
    return retval;
  }

}