package test.org.lx.io;

import java.util.Date;

import junit.framework.TestCase;

public class testMultiThread extends TestCase
{
	public void testSynchronized()
	{
		Integer obj = new Integer(0);
		long d1 = 0;
		int t = 10000000;
		synchronized (obj)
		{
		}
		
		d1 = new Date().getTime();
		for (int i = 0; i < t; i++)
		{
		}
		long d_none = new Date().getTime() - d1;

		d1 = new Date().getTime();
		for (int i = 0; i < t; i++)
		{
			synchronized (obj)
			{
				obj.notify();
			}
		}
		long d_notify = new Date().getTime() - d1;

		d1 = new Date().getTime();
		for (int i = 0; i < t; i++)
		{
			synchronized (obj)
			{
				obj.notifyAll();
			}
		}
		long d_notifyAll = new Date().getTime() - d1;

		d1 = new Date().getTime();
		for (int i = 0; i < t; i++)
		{
			synchronized (obj)
			{
			}
		}
		long d_synchronized = new Date().getTime() - d1;


		System.out.println("d_none=" + d_none);
		System.out.println("d_synchronized=" + d_synchronized);
		System.out.println("d_notify=" + d_notify);
		System.out.println("d_notifyAll=" + d_notifyAll);
		assertTrue(d_notify > d_synchronized);
		assertTrue(d_notifyAll > d_synchronized);
		assertTrue(d_synchronized > d_none);
		
	}
}
