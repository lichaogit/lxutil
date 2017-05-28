/*
 * Created on 2006-2-5
 * The interface to create a Instance by user-defined Hook.
 */
package org.lx.util;

import java.util.*;

/**
 */
public interface IObjectCreatorHook
{
    /**
     * create the ui component for the specific comid
     * 
     * @param com_cls
     * @param params the parameters for create the Object instance.
     * @return
     */
    public Object createComponent(Class com_cls,Map params);

}
