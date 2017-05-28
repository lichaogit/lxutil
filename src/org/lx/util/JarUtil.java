package org.lx.util;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarUtil
{
	public static String getJarMainAttribute(Manifest manifest, String AttrName)
			throws IOException
	{
		// Get the manifest entries
		Attributes mainAttr = manifest.getMainAttributes();
		return mainAttr == null ? null : mainAttr.getValue(AttrName);
	}

	public static String getJarMainAttribute(JarFile jarFile, String attrName)
			throws IOException
	{
		return getJarMainAttribute(jarFile.getManifest(), attrName);
	}
}
