package test.org.lx.arch;

import junit.framework.TestCase;

import org.lx.arch.BusyPipe;

public class BusyPipeTest extends TestCase
{

	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testBusyPipe()
	{
		long[] grid = new long[] { 1000, 4000 };
		BusyPipe busyPipe = new BusyPipe();
		String key1 = "key1";
		String key2 = "key2";

		// 1.check the first level grid(1000)
		assertFalse(busyPipe.isBusy(grid, 10, key1));// grid 0,0
		assertFalse(busyPipe.isBusy(grid, 1011, key1));// grid 1,0
		assertTrue(busyPipe.isBusy(grid, 1999, key1));// grid 1,0

		// 2.check the second level should be triggerred.
		assertTrue(busyPipe.isBusy(grid, 3999, key1));// grid 3,0
		assertFalse(busyPipe.isBusy(grid, 4001, key1));// grid 4,1
		assertFalse(busyPipe.isBusy(grid, 5001, key1));// grid 5,1
		assertTrue(busyPipe.isBusy(grid, 5999, key1));// grid 5,1
		assertTrue(busyPipe.isBusy(grid, 7000, key1));// grid 7,1
		assertFalse(busyPipe.isBusy(grid, 8000, key1));// grid 8,2

		grid = new long[] { 1000 };
		busyPipe = new BusyPipe();
		assertFalse(busyPipe.isBusy(grid, 10, key1));
		assertTrue(busyPipe.isBusy(grid, 20, key1));
		assertFalse(busyPipe.isBusy(grid, 10, key2));
		assertFalse(busyPipe.isBusy(grid, 1020, key2));
		assertTrue(busyPipe.isBusy(grid, 1030, key2));

	}
}
