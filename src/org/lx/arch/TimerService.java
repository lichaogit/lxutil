package org.lx.arch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.lx.arch.ThreadPool.CommandFunction;
import org.lx.arch.plugin.ActionContext;
import org.lx.arch.plugin.AppHelper;
import org.lx.util.GeneralException;
import org.lx.util.LogicUtil;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class TimerService
{
	protected SchedulerFactory m_sf = new StdSchedulerFactory();

	protected Scheduler m_sched;

	// protected final static int ITEM_INVALID = -2;

	protected final static int ITEM_NEW = -1;

	protected final static int ITEM_EQUAL = 0;

	protected final static int ITEM_CHANGED = 1;

	public static String ATTR_TIMERSERVICE_NAME = "name";

	public static String ATTR_TIMERSERVICE_GROUP = "group";

	public static String ATTR_TIMERSERVICE_VALUE = "value";

	public static String ATTR_TIMERSERVICE_CMD = "command";

	public static String ATTR_TIMERSERVICE_TRIGGER = "trigger";

	// the definition for caller.
	protected static String PARAM_TIMERSERVICE_CMD = "a";

	protected static String PARAM_TIMERSERVICE_CMD_PARAMS = "b";

	public static class JobWrapper implements Job
	{
		public void execute(JobExecutionContext context)
				throws JobExecutionException
		{
			Map jobData = context.getJobDetail().getJobDataMap();
			String command = (String) jobData.get(PARAM_TIMERSERVICE_CMD);
			Map jobParams = (Map) jobData.get(PARAM_TIMERSERVICE_CMD_PARAMS);

			try
			{
				ThreadPool.WorkFunction fun = new ThreadPool.WorkFunction() {

					@Override
					public RESULT process(Object cmd) throws GeneralException
					{
						Object[] params = (Object[]) cmd;
						return AppHelper.exec((String) params[0],
								new ActionContext((Map) params[1]));
					}

					@Override
					public void timeout(Object cmd) throws GeneralException
					{

					}
				};

				Object[] params = new Object[2];
				params[0] = command;
				params[1] = jobParams;
				// exec the command in ThreadPool synchronized.
				AppHelper.asynExec(fun, params, 3 * 60 * 1000,
						new CommandFunction() {

							@Override
							public void complete(Object cmd, Object result)
									throws GeneralException
							{
								int debug = 0;

							}
						}, 2);
			} catch (Exception e)
			{
				throw new JobExecutionException(e);
			}
		}
	}

	protected class JobComparable implements Comparable
	{
		Scheduler sched;

		public JobComparable(Scheduler sched)
		{
			this.sched = sched;
		}

		public Scheduler getScheduler()
		{
			return sched;
		}

		public int compareTo(Object o)
		{
			int retval = ITEM_EQUAL;// no change by default
			Attribute attr = null;
			String jobName = null;
			String groupName = null;
			String triggerTplName = null;
			Element item = (Element) o;

			attr = item.attribute(ATTR_TIMERSERVICE_NAME);
			jobName = attr.getText();

			attr = item.attribute(ATTR_TIMERSERVICE_GROUP);
			groupName = attr.getText();

			attr = item.attribute(ATTR_TIMERSERVICE_TRIGGER);
			triggerTplName = attr.getText();
			String triggerName = genID(genID(jobName, groupName),
					triggerTplName);
			try
			{
				JobDetail job = sched.getJobDetail(jobName, groupName);

				if (job != null)
				{
					boolean isExist = false;
					Trigger[] triggers = (Trigger[]) sched.getTriggersOfJob(
							jobName, groupName);
					for (int i = 0; i < triggers.length; i++)
					{
						if (triggers[i].getName().equals(triggerName))
						{
							isExist = true;
							break;
						}
					}

					// the job exists but the trigger info is different.
					if (!isExist)
					{
						retval = ITEM_CHANGED;
					}

				} else
				{
					retval = ITEM_NEW;
				}

			} catch (SchedulerException e)
			{
				retval = ITEM_NEW;// new item
			}
			return retval;
		}
	}

	protected class TriggerComparable implements Comparable
	{
		Scheduler sched;

		public TriggerComparable(Scheduler sched)
		{
			this.sched = sched;
		}

		public Scheduler getScheduler()
		{
			return sched;
		}

		public int compareTo(Object o)
		{
			int retval = ITEM_EQUAL;// no change by default
			Element item = (Element) o;

			Attribute attr = null;
			String tplName = null;
			String value = null;

			attr = item.attribute(ATTR_TIMERSERVICE_NAME);
			tplName = attr.getText();
			attr = item.attribute(ATTR_TIMERSERVICE_VALUE);
			value = attr.getText();

			CronTrigger trigger = null;
			try
			{
				Collection triggers = getTriggers(sched);
				// search the trigger base on the trigger template name.
				trigger = (CronTrigger) getFirstTrigger(tplName, triggers);
				if (trigger == null)
				{
					retval = ITEM_NEW;
				} else if (!trigger.getCronExpression().equals(value))
				{
					retval = ITEM_CHANGED;
				}

			} catch (SchedulerException e)
			{
				retval = ITEM_NEW;// new item
			}

			return retval;
		}
	}

	protected static Collection getJobs(Scheduler sched)
			throws SchedulerException
	{
		HashSet retval = new HashSet();

		String[] groups = sched.getJobGroupNames();
		String[] names = null;
		JobDetail job = null;

		for (int i = 0; i < groups.length; i++)
		{
			names = sched.getJobNames(groups[i]);
			for (int j = 0; j < names.length; j++)
			{
				job = sched.getJobDetail(names[j], groups[i]);
				retval.add(job);
			}
		}
		return retval;
	}

	protected static Collection getTriggers(Scheduler sched)
			throws SchedulerException
	{
		HashSet retval = new HashSet();

		String[] groups = sched.getTriggerGroupNames();
		String[] names = null;
		Trigger trigger = null;

		for (int i = 0; i < groups.length; i++)
		{
			names = sched.getTriggerNames(groups[i]);
			for (int j = 0; j < names.length; j++)
			{
				trigger = sched.getTrigger(names[j], groups[i]);
				retval.add(trigger);
			}
		}
		return retval;
	}

	protected void triggersChange(Scheduler sched, Map triggers)
			throws SchedulerException, ParseException
	{
		if (triggers.size() > 0)
		{
			Iterator it = triggers.keySet().iterator();
			Element ele = null;
			Attribute attr = null;
			String name = null;
			String group = null;
			String value = null;

			String id = null;
			String tplName = null;
			Collection tplTriggers = null;
			Collection curTriggers = null;
			Iterator itTplTrigger = null;
			CronTrigger trigger = null;
			while (it.hasNext())
			{
				id = (String) it.next();
				ele = (Element) triggers.get(id);

				attr = ele.attribute(ATTR_TIMERSERVICE_NAME);
				tplName = attr.getText();
				attr = ele.attribute(ATTR_TIMERSERVICE_GROUP);
				group = attr.getText();
				attr = ele.attribute(ATTR_TIMERSERVICE_VALUE);
				value = attr.getText();

				// all of the triggers that associated to this template should
				// be changed.
				curTriggers = getTriggers(sched);
				tplTriggers = getTriggers(tplName, curTriggers,
						curTriggers.size());
				itTplTrigger = tplTriggers.iterator();
				while (itTplTrigger.hasNext())
				{
					trigger = (CronTrigger) itTplTrigger.next();
					trigger.setCronExpression(value);
					sched.rescheduleJob(name, group, trigger);
				}
			}
		}

	}

	protected void triggersDelete(Scheduler sched, Collection triggers)
			throws SchedulerException
	{
		if (triggers.size() > 0)
		{
			Iterator it = triggers.iterator();

			String name = null;
			String group = null;

			Trigger trigger = null;
			while (it.hasNext())
			{
				trigger = (Trigger) it.next();
				name = trigger.getName();
				group = trigger.getGroup();
				sched.unscheduleJob(name, group);
			}
		}
	}

	public void apply(Scheduler sched, final Map triggersNew,
			final Map triggersChange, final Collection triggersDelete,
			final Map jobsNew, final Map jobsChange, final Collection jobsDelete)
			throws SchedulerException, ParseException
	{
		// Handle trigger delete.
		triggersDelete(sched, triggersDelete);

		// Handle job delete.
		jobsDelete(sched, jobsDelete);

		// Handle triggers change.

		triggersChange(sched, triggersChange);

		// Handle job Change.
		jobsChange(sched, jobsChange, triggersNew);

		// Handle job new
		jobsAdd(sched, jobsNew, triggersNew);
	}

	protected void jobsAdd(Scheduler sched, Map jobs, final Map triggers)
			throws SchedulerException, ParseException
	{
		if (jobs.size() > 0)
		{
			Iterator it = jobs.keySet().iterator();
			Element eleJob = null;
			Element eleTrigger = null;

			Attribute attr = null;
			String name = null;
			String group = null;
			String value = null;
			String id = null;

			ConfigurationView confView = null;
			while (it.hasNext())
			{
				id = (String) it.next();
				eleJob = (Element) jobs.get(id);

				attr = eleJob.attribute(ATTR_TIMERSERVICE_NAME);
				name = attr.getText();
				attr = eleJob.attribute(ATTR_TIMERSERVICE_GROUP);
				group = attr.getText();

				attr = eleJob.attribute(ATTR_TIMERSERVICE_TRIGGER);
				String triggerTplName = attr.getText();

				attr = eleJob.attribute(ATTR_TIMERSERVICE_CMD);
				String commandName = attr.getText();

				String triggerId = genID(genID(name, group), triggerTplName);
				// use the job's group,ignore the trigger's group.
				Trigger trigger = sched.getTrigger(triggerId, group);
				if (trigger == null)
				{
					// create a new Trigger if it doesn't exist.
					String triggerTplID = genID(triggerTplName, group);
					eleTrigger = (Element) triggers.get(triggerTplID);
					if (eleTrigger == null)
					{
						// the job's trigger does not existed.
						continue;
					}

					// build the new trigger if trigger doesn't exists.
					attr = eleTrigger.attribute(ATTR_TIMERSERVICE_VALUE);
					value = attr.getText();

					trigger = new CronTrigger(triggerId, group, value);
				}
				// just add a trigger if the job exists
				JobDetail job = null;
				job = sched.getJobDetail(name, group);
				if (job == null)
				{
					job = new JobDetail(name, group, JobWrapper.class);

					confView = new ConfigurationView(
							new Dom4jNodeConfiguration(eleJob), null);
					Map params = confView.getNameValuePaired("parameter");

					// set parameters for job.
					job.getJobDataMap().put(PARAM_TIMERSERVICE_CMD_PARAMS,
							params);
					job.getJobDataMap()
							.put(PARAM_TIMERSERVICE_CMD, commandName);

					sched.scheduleJob(job, trigger);
				} else
				{
					// add a trigger only.
					trigger.setJobName(name);
					trigger.setJobGroup(group);
					sched.scheduleJob(trigger);
				}
			}
		}

	}

	/**
	 * exec the specific command/params for the specific cronExpress.
	 * @param sched
	 * @param cronExpress
	 * @param command
	 * @param params
	 * @return
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public JobDetail addJob(String cronExpress, String command, Map params)
			throws SchedulerException, ParseException
	{
		Scheduler sched = m_sched;
		// 1.create trigger.
		StringBuffer sb = new StringBuffer();
		sb.append(command);
		sb.append(cronExpress.hashCode());
		if (params != null)
		{
			sb.append('_');
			sb.append(params.hashCode());
		}

		String id = sb.toString();
		String group = command;
		JobDetail job = null;

		job = sched.getJobDetail(id, group);
		if (job == null)
		{
			Trigger trigger = sched.getTrigger(id, group);
			if (trigger == null)
			{
				trigger = new CronTrigger(id, group, cronExpress);
			}

			// 2.add a job.
			job = new JobDetail(id, group, JobWrapper.class);
			// set parameters for job.
			job.getJobDataMap().put(PARAM_TIMERSERVICE_CMD_PARAMS, params);
			job.getJobDataMap().put(PARAM_TIMERSERVICE_CMD, command);

			sched.scheduleJob(job, trigger);
		}
		return job;
	}

	// detach from the original trigger and schedule with the new trigger.
	protected void jobsChange(Scheduler sched, Map jobs, Map triggers)
			throws SchedulerException, ParseException
	{
		jobsAdd(sched, jobs, triggers);
		// if (jobs.size() > 0)
		// {
		// Iterator it = jobs.keySet().iterator();
		// Element ele = null;
		// Attribute attr = null;
		// String name = null;
		// String group = null;
		//
		// String id = null;
		//
		// while (it.hasNext())
		// {
		// id = (String) it.next();
		// ele = (Element) jobs.get(id);
		//
		// attr = ele.attribute(ATTR_TIMERSERVICE_NAME);
		// name = attr.getText();
		// attr = ele.attribute(ATTR_TIMERSERVICE_GROUP);
		// group = attr.getText();
		// attr = ele.attribute(ATTR_TIMERSERVICE_TRIGGER);
		// String triggerTplName = attr.getText();
		//
		// String triggerId = genID(genID(name, group), triggerTplName);
		// Trigger trigger = sched.getTrigger(triggerId, group);
		// if (trigger == null)
		// {
		// // trigger = new CronTrigger(triggerId, group, value);
		//
		// }
		// JobDetail job = new JobDetail(name, group, JobWrapper.class);
		// // remove the original job before schedule it.
		// sched.deleteJob(name, group);
		//
		// trigger.setJobName(name);
		// trigger.setJobGroup(group);
		//
		// sched.scheduleJob(job, trigger);
		// }
		// }

	}

	protected void jobsDelete(Scheduler sched, Collection jobs)
			throws SchedulerException
	{
		if (jobs.size() > 0)
		{
			Iterator it = jobs.iterator();

			String name = null;
			String group = null;

			JobDetail job = null;
			while (it.hasNext())
			{
				job = (JobDetail) it.next();
				name = job.getName();
				group = job.getGroup();
				sched.deleteJob(name, group);
			}
		}
	}

	public static String genID(String name, String group)
	{
		String seperator = ".";
		StringBuffer buf = new StringBuffer(name.length() + group.length()
				+ seperator.length());
		buf.append(name);
		buf.append(seperator);
		buf.append(group);
		return buf.toString();
	}

	protected static Collection getTriggers(String tplName,
			Collection triggers, int maxNum)
	{
		Collection retval = null;

		Iterator it = triggers.iterator();
		int offset = -1;
		Trigger trigger = null;
		String name = null;
		ArrayList al = new ArrayList(triggers.size());

		while (it.hasNext())
		{
			trigger = (CronTrigger) it.next();
			name = trigger.getName();
			offset = name.lastIndexOf(tplName);
			if (offset == -1)
			{
				continue;
			}
			if (!name.substring(offset).equals(tplName))
			{
				continue;
			}
			if (maxNum-- > 0)
			{
				al.add(trigger);
			}
		}
		retval = al;
		return retval;
	}

	protected static Trigger getFirstTrigger(String tplName, Collection triggers)
	{
		Trigger retval = null;

		Collection ts = getTriggers(tplName, triggers, 1);
		if (ts.size() > 0)
		{
			retval = (Trigger) ts.toArray(new Trigger[0])[0];
		}
		return retval;
	}

	/**
	 * find the change between configuration and the current schedule.
	 * @param sched
	 * @param confView
	 * @param newJob
	 * @param ChangedJob
	 * @param toBeRemovedJob
	 * @throws SchedulerException
	 */
	protected void calculateChange(Comparable comparable,
			Collection targetItems, Map newItems, Map changeItems,
			Collection deleteItems) throws SchedulerException
	{
		Iterator itItems = targetItems.iterator();

		Element item = null;

		Attribute attr = null;
		String name;
		String group;
		String id = null;

		int result = 0;
		while (itItems.hasNext())
		{
			item = (Element) itItems.next();

			attr = item.attribute(ATTR_TIMERSERVICE_NAME);
			name = attr.getText();

			attr = item.attribute(ATTR_TIMERSERVICE_GROUP);
			group = attr.getText();
			id = genID(name, group);

			result = comparable.compareTo(item);
			switch (result)
			{
			case ITEM_NEW:
			// case ITEM_INVALID:// the invalid job's trigger may is newer.
			{
				newItems.put(id, item);
				break;
			}
			case ITEM_CHANGED:
			{
				changeItems.put(id, item);
				break;
			}
			case ITEM_EQUAL:
			{
				// do nothing
				break;
			}
			}
		}

		// calculate the delete items.
		Scheduler sched = null;

		itItems = targetItems.iterator();

		Collection workItems = null;

		if (comparable instanceof JobComparable)
		{
			JobComparable jc = (JobComparable) comparable;
			sched = jc.getScheduler();

			JobDetail job = null;

			// get all jobs.
			workItems = getJobs(sched);

			while (itItems.hasNext())
			{
				item = (Element) itItems.next();

				attr = item.attribute(ATTR_TIMERSERVICE_NAME);
				name = attr.getText();

				attr = item.attribute(ATTR_TIMERSERVICE_GROUP);
				group = attr.getText();

				job = sched.getJobDetail(name, group);
				if (job != null)
				{
					workItems.remove(job);
				}
			}

			// generate the delete items.
			deleteItems.addAll(workItems);

		} else if (comparable instanceof TriggerComparable)
		{
			TriggerComparable tc = (TriggerComparable) comparable;
			sched = tc.getScheduler();

			CronTrigger trigger = null;

			// get all triggers (class:Trigger).
			workItems = getTriggers(sched);
			String tplName = null;

			while (itItems.hasNext())
			{
				item = (Element) itItems.next();

				attr = item.attribute(ATTR_TIMERSERVICE_NAME);
				tplName = attr.getText();

				attr = item.attribute(ATTR_TIMERSERVICE_GROUP);
				group = attr.getText();

				trigger = (CronTrigger) getFirstTrigger(tplName, workItems);
				if (trigger != null)
				{
					workItems.remove(trigger);
				}
			}

			// generate the delete items.
			deleteItems.addAll(workItems);
		}
	}

	/**
	 * find the change between configuration and the current schedule.
	 * @param sched
	 * @param confView
	 * @param newJob
	 * @param ChangedJob
	 * @param toBeRemovedJob
	 * @throws SchedulerException
	 */
	public void calculateJobsChange(Scheduler sched,
			ConfigurationView confView, Map jobsNew, Map jobsChange,
			Collection jobsDelete) throws SchedulerException
	{
		List jobs = confView.queryAll("Jobs/Job[@trigger]");

		JobComparable jc = new JobComparable(sched);

		calculateChange(jc, jobs, jobsNew, jobsChange, jobsDelete);

	}

	public void calculateTriggersChange(Scheduler sched,
			ConfigurationView confView, Map triggersNew, Map triggersChange,
			Collection triggersDelete) throws SchedulerException
	{

		/*
		 * XSLT
		 * support:/configuration/frontEnd/TimerService/Triggers/Trigger[count
		 * (index
		 * -of(/configuration/frontEnd/TimerService/Jobs/Job/@name,@name))>0]
		 */
		List triggers = confView.queryAll("Triggers/Trigger[@name]");
		List triggersNames = confView.queryAll(
				"Jobs/Job[string(@trigger)]/@trigger",
				ConfigurationView.STRING_TRANSFORM);
		Object[] namesArray = triggersNames.toArray();
		// skip the unused trigger
		Iterator it = triggers.iterator();
		Element ele = null;
		Attribute attr = null;
		String name = null;
		while (it.hasNext())
		{
			ele = (Element) it.next();
			attr = ele.attribute(ATTR_TIMERSERVICE_NAME);
			name = attr.getText();
			if (!LogicUtil.isInArray(namesArray, name))
			{
				it.remove();
			}
		}

		TriggerComparable tc = new TriggerComparable(sched);

		calculateChange(tc, triggers, triggersNew, triggersChange,
				triggersDelete);

	}

	public void start(ConfigurationView confView) throws SchedulerException,
			ParseException
	{
		init(confView);
		m_sched.start();
	}

	public void stop() throws SchedulerException
	{
		if (m_sched != null)
		{
			m_sched.shutdown(true);
		}
	}

	public Scheduler init(ConfigurationView confView)
			throws SchedulerException, ParseException
	{
		m_sched = m_sf.getScheduler();

		HashMap jobsNew = new HashMap();
		HashMap jobsChange = new HashMap();
		HashSet jobsDelete = new HashSet();

		HashMap triggersNew = new HashMap();
		HashMap triggersChange = new HashMap();
		HashSet triggersDelete = new HashSet();

		ConfigurationView tsConfView = confView;
		calculateTriggersChange(m_sched, tsConfView, triggersNew,
				triggersChange, triggersDelete);
		calculateJobsChange(m_sched, tsConfView, jobsNew, jobsChange,
				jobsDelete);
		apply(m_sched, triggersNew, triggersChange, triggersDelete, jobsNew,
				jobsChange, jobsDelete);

		return m_sched;
	}
}
