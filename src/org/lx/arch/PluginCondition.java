package org.lx.arch;

import java.io.IOException;
import java.util.Collection;
import java.util.jar.Manifest;

import org.lx.arch.plugin.DefaultPluginManager;
import org.lx.util.JarUtil;

public class PluginCondition implements Comparable
{

	Collection plugins;

	public PluginCondition(Collection plugins)
	{
		this.plugins = plugins;
	}

	public int compareTo(Object o)
	{
		int retval = -1;
		do
		{
			if (o instanceof Manifest == false)
			{
				break;
			}

			Manifest pluginManifest = (Manifest) o;

			String plugClsName;
			try
			{
				plugClsName = JarUtil.getJarMainAttribute(pluginManifest,
						DefaultPluginManager.PLUG_MAINCLASS);
				if (plugClsName == null)
				{
					break;
				}
				String ifStr = JarUtil.getJarMainAttribute(pluginManifest,
						DefaultPluginManager.PLUG_INTERFACES);
				if (ifStr == null)
				{
					break;
				}
				String idStr = JarUtil.getJarMainAttribute(pluginManifest,
						DefaultPluginManager.PLUG_ID);

				if (plugins.contains(idStr))
				{
					retval = 0;
				}
			} catch (IOException e)
			{
				// do nothing
			}
		} while (false);

		return retval;
	}

}
