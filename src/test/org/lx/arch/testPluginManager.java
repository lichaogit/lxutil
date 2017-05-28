package test.org.lx.arch;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.lx.arch.PluginCondition;
import org.lx.arch.plugin.DefaultPluginManager;

public class testPluginManager extends TestCase
{
	public void testLoadPlug()
	{
		DefaultPluginManager pm = new DefaultPluginManager();
		pm.addLoader(new DefaultPluginManager.JarPluginLoader(
				"bin\\test\\org\\lx\\arch\\data"));

		pm.load(null);

		List list = pm.query("WebContent.plugin");
		assertTrue(list != null && list.size() > 0);

		ArrayList al = new ArrayList();
		al.add("builtin");
		pm = new DefaultPluginManager();
		pm.addLoader(new DefaultPluginManager.JarPluginLoader(
				"bin\\test\\org\\lx\\arch\\data"));
		pm.load(new PluginCondition(al));

		list = pm.query("WebContent.plugin");
		assertTrue(list != null && list.size() == 1);

	}
}
