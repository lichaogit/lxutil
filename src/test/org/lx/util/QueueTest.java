package test.org.lx.util;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import junit.framework.TestCase;

public class QueueTest extends TestCase
{
	private String name;

	private int population;

	public QueueTest()
	{

	}

	public QueueTest(String name, int population)
	{
		this.name = name;
		this.population = population;
	}

	public String getName()
	{
		return this.name;
	}

	public int getPopulation()
	{
		return this.population;
	}

	public String toString()
	{
		return getName() + " - " + getPopulation();
	}

	public void testPriorityQueue()
	{
		Comparator OrderIsdn = new Comparator() {
			public int compare(Object o1, Object o2)
			{
				// TODO Auto-generated method stub
				int numbera = ((QueueTest) o1).getPopulation();
				int numberb = ((QueueTest) o2).getPopulation();
				if (numberb > numbera)
				{
					return 1;
				} else if (numberb < numbera)
				{
					return -1;
				} else
				{
					return 0;
				}

			}

		};
		Queue priorityQueue = new PriorityQueue(11, OrderIsdn);

		QueueTest t1 = new QueueTest("t1", 1);
		QueueTest t3 = new QueueTest("t3", 3);
		QueueTest t2 = new QueueTest("t2", 2);
		QueueTest t4 = new QueueTest("t4", 0);
		priorityQueue.add(t1);
		priorityQueue.add(t3);
		priorityQueue.add(t2);
		priorityQueue.add(t4);
		assertEquals(t3, priorityQueue.poll());
		assertEquals(t2, priorityQueue.poll());
		assertEquals(t1, priorityQueue.poll());
		assertEquals(t4, priorityQueue.poll());

	}
}
