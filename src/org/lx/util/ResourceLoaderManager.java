package org.lx.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ResourceLoaderManager
{
	public static interface Loader
	{
		public InputStream load(String resName) throws IOException;
	}

	public static abstract class AbstractLoader implements Loader
	{
		Object m_resBase;

		public AbstractLoader(Object resBase)
		{
			m_resBase = resBase;
		}

		public Object getBase()
		{
			return m_resBase;
		}

		public void setBase(Object resBase)
		{
			m_resBase = resBase;
		}
	}

	public static class URLLoader extends AbstractLoader
	{
		public URLLoader(URL resBase)
		{
			super(resBase);
		}

		public InputStream load(String resName)
		{
			InputStream retval = null;
			do
			{
				try
				{
					retval = new URL((URL) getBase(), resName).openStream();
				} catch (MalformedURLException ex)
				{
					Log.log(Log.DEBUG, null, ex);
				} catch (IOException ex1)
				{
					Log.log(Log.DEBUG, null, ex1);
				}
			} while (false);
			return retval;
		}
	}

	public static class JarFileLoader extends AbstractLoader
	{
		public JarFileLoader(String jarFileName) throws IOException
		{
			super(new JarFile(jarFileName));
		}

		public InputStream load(String resName) throws IOException
		{
			JarFile jarFile = (JarFile) getBase();
			ZipEntry entry = jarFile.getEntry(resName);
			return jarFile.getInputStream(entry);
		}
	}

	public static class JarLoader extends AbstractLoader
	{
		ClassLoader m_cl;

		public JarLoader(String resBase)
		{
			this(null, resBase);
		}

		public JarLoader(ClassLoader cl, String resBase)
		{
			super(resBase);
			m_cl = cl;
		}

		public InputStream load(String resName) throws IOException
		{
			InputStream retval = null;
			StringBuffer jarFile = new StringBuffer();
			do
			{
				// the absolute dir.
				if (resName.startsWith("/"))
				{
					jarFile.append(resName);
					break;
				}
				// the relative dir
				jarFile.append(getBase());
				if (jarFile.charAt(jarFile.length() - 1) != '/')
				{
					jarFile.append("/");
				}
				jarFile.append(resName);
			} while (false);
			ClassLoader cl = m_cl;

			if (cl == null)
			{
				retval = ResourceLoaderManager.class
						.getResourceAsStream(jarFile.toString());
			} else
			{
				retval = cl.getResource(jarFile.toString()).openStream();
			}

			return retval;
		}
	}

	public static class ClassPathLoader extends AbstractLoader
	{
		public static final String SCHEME = "file:///";

		public ClassPathLoader()
		{
			super(null);
		}

		public InputStream load(String resName) throws IOException
		{
			InputStream retval = null;
			String classpath = System.getProperty("java.class.path");
			do
			{
				if (classpath == null)
				{
					break;
				}
				String[] paths = classpath.split(System
						.getProperty("path.separator"));
				if (paths.length == 0)
				{
					break;
				}
				String path = null;
				File file = null;
				for (int i = 0; i < paths.length; i++)
				{
					path = paths[i] + File.separator + resName;
					file = new File(path);
					if (file.exists())
					{
						try
						{
							retval = new URL(SCHEME + file).openStream();
							break;
						} catch (MalformedURLException ex1)
						{
							Log.log(Log.DEBUG, null, ex1);
						}
					}
				}

			} while (false);
			return retval;
		}
	}

	public static class FileLoader extends AbstractLoader
	{
		public static final String SCHEME = "file:///";

		public FileLoader(String resBase)
		{
			super(resBase);
		}

		public InputStream load(String resName) throws IOException
		{
			InputStream retval = null;
			do
			{
				StringBuffer fullName = new StringBuffer();
				if (getBase() != null)
				{
					fullName.append(getBase());
					fullName.append(File.separator);
				}
				if (resName != null)
				{
					fullName.append(resName);
				}
				if (fullName.length() == 0)
				{
					break;
				}
				File file = new File(fullName.toString());
				if (file.exists())
				{
					try
					{
						retval = new URL(SCHEME + file).openStream();
					} catch (MalformedURLException e1)
					{
						Log.log(Log.DEBUG, ResourceLoaderManager.class
								.getName(), e1.getMessage());
					}
				}
			} while (false);
			return retval;
		}

	}

	protected ArrayList m_loaders = new ArrayList();

	/**
	 * insert the loader to the haed.<p>
	 * @param loader:
	 *        the loader to be added.
	 */
	public void addLoader(Loader loader)
	{
		m_loaders.add(0, loader);
	}

	/**
	 * apppend the loader to the tail. <p>
	 * @param loader:
	 *        the loader to be added.
	 */
	public void appendLoader(Loader loader)
	{
		m_loaders.add(loader);
	}

	public void addLoader(Collection loaders)
	{
		m_loaders.addAll(0, loaders);
	}

	public Collection getLoader()
	{
		return m_loaders;
	}

	public Loader getLoader(int index)
	{
		Loader retval = null;
		if (index < m_loaders.size())
		{
			retval = (Loader) m_loaders.get(index);
		}
		return retval;
	}

	public Object clone()
	{
		ResourceLoaderManager retval = null;
		retval = new ResourceLoaderManager();
		retval.addLoader(getLoader());
		return retval;
	}

	/**
	 * load the specfic resource's URL by the specific loader index .
	 * @param resName
	 * @return
	 */
	public InputStream load(String resName) throws IOException
	{
		return load(resName, 0);
	}

	/**
	 * Get the resource's InputStream for the specific resource.
	 * @param resName
	 * @param startIndex
	 * @return
	 */
	public InputStream load(String resName, int startIndex) throws IOException
	{
		InputStream retval = null;
		Iterator it = m_loaders.iterator();
		Loader ele = null;
		while (it.hasNext())
		{
			ele = (Loader) it.next();
			// skip the Map
			if (startIndex-- > 0)
			{
				continue;
			}
			try
			{
				retval = ele.load(resName);
			} catch (Exception e)
			{
				continue;
			}
			if (retval != null)
			{
				break;
			}
		}
		return retval;
	}
}
