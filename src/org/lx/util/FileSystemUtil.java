package org.lx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright (c) 2003-2005</p>
 * <p>Company: uilogic</p>
 * @author zhaolc
 * @version 1.0
 */

public class FileSystemUtil
{
	public FileSystemUtil()
	{
	}

	/**
	 * get the files that match the ext name.
	 * @param strdir
	 * @return
	 */
	public static File[] listFilesByExt(String strdir, String str)
	{
		File dir = new File(strdir);
		// This filter only returns directories
		FileNameFileFilter fileFilter = new FileNameFileFilter();
		fileFilter.fromRegex(str);
		// list all jar files
		return dir.listFiles(fileFilter);
	}

	public static File[] listFiles(URL url)
	{
		File[] retval = null;
		try
		{
			URI uri = new URI(url.toString());
			File file = new File(uri);
			retval = file.listFiles();
		} catch (URISyntaxException ex)
		{
		}
		return retval;
	}

	public static boolean copy(File srcFile, File targetFile)
	{
		boolean retval = false;
		int byteread = 0;
		if (srcFile.exists())
		{
			FileInputStream fin;
			try
			{
				fin = new FileInputStream(srcFile);
				FileOutputStream fout = new FileOutputStream(targetFile);
				byte[] buffer = new byte[1024];
				while ((byteread = fin.read(buffer)) != -1)
				{
					fout.write(buffer, 0, byteread);
				}
				fin.close();
				fout.close();
				retval = true;
			} catch (IOException e)
			{
				// do nothing
			}
		}
		return retval;
	}
}