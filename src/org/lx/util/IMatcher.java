/*
 * Created on 2006-1-19
 * 
 */
package org.lx.util;

public interface IMatcher
{
    /**
     * determine whether the specific object is matched the current matcher
     * @param o
     * @return
     */
    public boolean isMatch(Object o);
}