package org.lx.util;

import java.io.FileFilter;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: FileFilter base on the file name.
 * </p>
 * <p>** match any directory, * means any file *.* .
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003-2005
 * </p>
 * <p>
 * Company: uilogic
 * </p>
 * 
 * @author zhaolc
 * @version 1.0
 */

public class FileNameFileFilter implements FileFilter
{
    Pattern m_pattern;

    public FileNameFileFilter()
    {
    }

    public void fromRegex(String str)
    {
        m_pattern = Pattern.compile(str);
    }
    /**
     * from the "**\*.*" mode.
     * 
     * @param str
     */
    public void fromPathMatcher(String str)
    {
        String regex = StringEx.pathMatcherToRegex(str);

        m_pattern = Pattern.compile(regex);
    }

    public boolean accept(File file)
    {
        return accept(file.getAbsolutePath());
    }

    public boolean accept(String fileName)
    {
        boolean retval = false;
        if (m_pattern != null)
        {
            fileName = fileName.replaceAll("\\\\", "/");
            Matcher matcher = m_pattern.matcher(fileName);
            retval = matcher.matches();
        }
        return retval;
    }

}
