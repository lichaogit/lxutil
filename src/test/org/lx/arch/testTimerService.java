package test.org.lx.arch;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.lx.arch.Configuration;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.Dom4jNodeConfiguration;
import org.lx.arch.TimerService;
import org.lx.util.ResourceLoaderManager;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class testTimerService extends TestCase
{
	public static String ATTR_TIMERSERVICE_NAME = "name";

	public static String ATTR_TIMERSERVICE_GROUP = "group";

	ResourceLoaderManager m_rlm;

	public void setUp()
	{
		m_rlm = new ResourceLoaderManager();
		ResourceLoaderManager.Loader loader = new ResourceLoaderManager.JarLoader(
				"/test/org/lx/arch/data");
		m_rlm.addLoader(loader);
	}

	/**
	 * assur that the content are same.
	 * @param expect
	 * @param result
	 */
	protected void assertMap(List expect, HashMap result)
	{
		Element item = null;
		String name = null;
		String group = null;
		Attribute attr = null;
		assertEquals(expect.size(), result.size());

		Iterator it = expect.iterator();
		String ID = null;
		Element resultItem = null;
		while (it.hasNext())
		{
			item = (Element) it.next();

			attr = item.attribute(ATTR_TIMERSERVICE_NAME);
			name = attr.getText();
			attr = item.attribute(ATTR_TIMERSERVICE_GROUP);
			group = attr.getText();

			ID = TimerService.genID(name, group);

			resultItem = (Element) result.get(ID);
			assertNotNull(resultItem);
			attr = resultItem.attribute(ATTR_TIMERSERVICE_NAME);
			assertEquals(name, attr.getText());
			attr = resultItem.attribute(ATTR_TIMERSERVICE_GROUP);
			assertEquals(group, attr.getText());
		}
	}

	protected void assertCollections(List expect, Collection result)
	{
		Element item = null;
		String name = null;
		String group = null;
		Attribute attr = null;
		assertEquals(expect.size(), result.size());

		Iterator it = expect.iterator();
		String ID = null;
		Object obj = null;

		Iterator itResult = null;

		while (it.hasNext())
		{
			item = (Element) it.next();

			attr = item.attribute(ATTR_TIMERSERVICE_NAME);
			name = attr.getText();
			attr = item.attribute(ATTR_TIMERSERVICE_GROUP);
			group = attr.getText();

			ID = TimerService.genID(name, group);
			itResult = result.iterator();
			boolean found = false;
			while (itResult.hasNext())
			{
				obj = itResult.next();
				if (obj instanceof Trigger)
				{
					CronTrigger trigger = (CronTrigger) obj;
					int offset = trigger.getName().lastIndexOf(name);
					if (offset == -1)
					{
						continue;
					}
					if (!trigger.getName().substring(offset).equals(name))
					{
						continue;
					}
					if (!trigger.getGroup().equals(group))
					{
						continue;
					}
					found = true;
				} else if (obj instanceof JobDetail)
				{
					JobDetail job = (JobDetail) obj;
					if (!job.getName().equals(name))
					{
						continue;
					}
					if (!job.getGroup().equals(group))
					{
						continue;
					}
					found = true;
				}
			}
			assertTrue(name + ":" + group + " not found", found);
		}
	}

	public void testTimerServicve()
	{
		try
		{
			InputStream in = m_rlm.load("testTimerService.xml");

			Configuration cfg = new Dom4jConfiguration(in);
			ConfigurationView cfgView = new ConfigurationView(cfg, null);

			List cases = cfgView
					.queryAll("/test/*[local-name(.)!='base']/TimerService");

			HashMap jobsNew = new HashMap();
			HashMap jobsChange = new HashMap();
			HashSet jobsDelete = new HashSet();

			HashMap triggersNew = new HashMap();
			HashMap triggersChange = new HashMap();
			HashSet triggersDelete = new HashSet();
			// build the quartz timer according to the base.

			TimerService ts = new TimerService();
			ConfigurationView baseview = cfgView
					.getSubView("/test/base/TimerService");

			// compare to every case.
			Iterator it = cases.iterator();
			Element ele = null;
			Dom4jNodeConfiguration tempCfg = null;
			ConfigurationView caseView = null;
			ConfigurationView tsView = null;

			List workList = null;

			while (it.hasNext())
			{
				// reset the collections
				jobsNew.clear();
				jobsChange.clear();
				jobsDelete.clear();
				triggersNew.clear();
				triggersChange.clear();
				triggersDelete.clear();

				// 1. build the init scheduler.
				// get the init schedule
				Scheduler sched = createInitSchedule();
				ts.calculateJobsChange(sched, baseview, jobsNew, jobsChange,
						jobsDelete);
				ts.calculateTriggersChange(sched, baseview, triggersNew,
						triggersChange, triggersDelete);

				ts.apply(sched, triggersNew, triggersChange, triggersDelete,
						jobsNew, jobsChange, jobsDelete);

				// reset the collections
				jobsNew.clear();
				jobsChange.clear();
				jobsDelete.clear();
				triggersNew.clear();
				triggersChange.clear();
				triggersDelete.clear();
				// compare to the new schedule's setting.
				ele = (Element) it.next();
				tempCfg = new Dom4jNodeConfiguration(ele.getParent());
				caseView = new ConfigurationView(tempCfg, null);

				tsView = caseView.getSubView("TimerService");

				String caseName = (String) caseView.query("@name",
						caseView.STRING_TRANSFORM);
				System.out.println("start " + caseName + ":");
				if ("case_jobRemove".equals(caseName))
				{
					int i = 0;
				}

				ts.calculateJobsChange(sched, tsView, jobsNew, jobsChange,
						jobsDelete);

				ts.calculateTriggersChange(sched, tsView, triggersNew,
						triggersChange, triggersDelete);
				// check the result.
				workList = caseView.queryAll("results/jobAdd/item[@name]");
				assertMap(workList, jobsNew);

				workList = caseView.queryAll("results/jobChange/item[@name]");
				assertMap(workList, jobsChange);

				workList = caseView.queryAll("results/jobDelete/item[@name]");
				assertCollections(workList, jobsDelete);

				workList = caseView.queryAll("results/triggerAdd/item[@name]");
				assertMap(workList, triggersNew);

				workList = caseView
						.queryAll("results/triggerChange/item[@name]");
				assertMap(workList, triggersChange);

				workList = caseView
						.queryAll("results/triggerDelete/item[@name]");
				assertCollections(workList, triggersDelete);

				ts.apply(sched, triggersNew, triggersChange, triggersDelete,
						jobsNew, jobsChange, jobsDelete);
				sched.shutdown();
			}

		} catch (IOException e)
		{
			assertTrue(false);
		} catch (SchedulerException e)
		{
			assertTrue(false);
		} catch (ParseException e)
		{
			assertTrue(false);
		} catch (Exception e)
		{
			assertTrue(false);
		}

	}

	protected Scheduler createInitSchedule() throws SchedulerException
	{
		SchedulerFactory sf = new StdSchedulerFactory();
		return sf.getScheduler();
	}

}
