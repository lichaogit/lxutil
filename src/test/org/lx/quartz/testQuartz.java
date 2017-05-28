package test.org.lx.quartz;

import java.text.ParseException;

import junit.framework.TestCase;

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

public class testQuartz extends TestCase
{

	// 要进行动态的修改调度时间，需要在Job任务里，动态改变当前线程的调度计划
	private static SchedulerFactory sf = new StdSchedulerFactory();

	private static String JOB_GROUP_NAME = "group";

	private static String TRIGGER_GROUP_NAME = "trigger";

	protected static long m_tick = 0;

	static int index = 0;

	public static class JobWrapper implements Job
	{

		public void execute(JobExecutionContext context)
				throws JobExecutionException
		{

			index += 1;

			if (index == 40)
			{
//				try
//				{
//					modifyJobTime(context.getJobDetail(), "0/4 * * * * ?");
//				} catch (SchedulerException e)
//				{
//					assertTrue(false);
//				} catch (ParseException e)
//				{
//					assertTrue(false);
//				}
			}

			long curTick = System.currentTimeMillis();
			System.out.println("index=" + index + ":" + (curTick - m_tick));
			m_tick = curTick;

		}
	}

	public static void startJob(String jobName, Job job, String time)
			throws SchedulerException, ParseException
	{
		Scheduler sched = sf.getScheduler();

		JobDetail jobDetail = new JobDetail(jobName, JOB_GROUP_NAME, job
				.getClass());

		CronTrigger trigger = new CronTrigger(jobName, TRIGGER_GROUP_NAME, time);

		sched.scheduleJob(jobDetail, trigger);

		if (!sched.isShutdown())
		{
			sched.start();
		}
	}

	public static void modifyJobTime(JobDetail jobDetail, String time)
			throws SchedulerException, ParseException
	{
		Scheduler sched = sf.getScheduler();
		Trigger trigger = sched.getTrigger(jobDetail.getName(),
				TRIGGER_GROUP_NAME);
		if (trigger != null)
		{
			CronTrigger ct = (CronTrigger) trigger;

			// 移除当前进程的Job
			sched.deleteJob(jobDetail.getName(), jobDetail.getGroup());
			// 修改Trigger
			ct.setCronExpression(time);

			System.out.println("CronTrigger " + ct.getJobName()
					+ " change to :" + time);
			// 重新调度jobDetail
			sched.scheduleJob(jobDetail, ct);
		}
	}

	public static void modifyJobTime1(JobDetail jobDetail, String time)
			throws SchedulerException, ParseException
	{
		Scheduler sched = sf.getScheduler();
		Trigger trigger = sched.getTrigger(jobDetail.getName(),
				TRIGGER_GROUP_NAME);
		if (trigger != null)
		{
			CronTrigger ct = (CronTrigger) trigger;

			ct.setCronExpression(time);

			System.out.println("CronTrigger " + ct.getJobName()
					+ " change to :" + time);
			sched.rescheduleJob(jobDetail.getName(), TRIGGER_GROUP_NAME, ct);
		}
	}

	public void testChangeTrigger()
	{
		try
		{
			String jobName = "ming";
			JobWrapper job = new JobWrapper();
			m_tick = System.currentTimeMillis();
			startJob(jobName, job, "0/2 * * * * ?");
			synchronized (job)
			{
				while (true)
				{
					if (index == 15)
					{
						break;
					} else if (index == 8)
					{
						Scheduler sched = sf.getScheduler();
						JobDetail job1 = sched.getJobDetail(jobName,
								JOB_GROUP_NAME);

						modifyJobTime1(job1, "0/6 * * * * ?");
					}
					job.wait(500);
				}
			}
		} catch (SchedulerException e)
		{
			assertTrue(false);
		} catch (ParseException e)
		{
			assertTrue(false);
		} catch (InterruptedException e)
		{
			assertTrue(false);
		}
	}
}
