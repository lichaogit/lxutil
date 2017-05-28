package org.lx.arch;

import java.util.HashMap;
import java.util.HashSet;

public class BusyPipe
{
	// grid duration->key
	HashMap m_cache = new HashMap();

	// grid duration->grid value
	HashMap m_grid = new HashMap();

	public BusyPipe()
	{

	}

	public boolean isBusy(long[] timeGrids, String key)
	{
		return isBusy(timeGrids, System.currentTimeMillis(), key);
	}

	public boolean isBusy(long[] timeGrid, long now, String key)
	{
		// 1.return true if any one in timeGrid is busy.
		// 2.if the first grid busy, will enable the next level grid.
		boolean retval = false;
		long grid = 0;

		int i = 0;
		int busyIndex = -1;
		synchronized (m_cache)
		{
			// 1. clear the expired time grid and update the time grid.
			for (i = 0; i < timeGrid.length; i++)
			{
				grid = now / timeGrid[i];
				Object wk = m_grid.get(timeGrid[i]);
				long prevGrid = wk == null ? 0 : ((Number) wk).longValue();
				if (grid != prevGrid)
				{
					m_cache.remove(timeGrid[i]);
				}
			}

			// 2.check busy and add key.
			for (i = 0; i < timeGrid.length; i++)
			{
				HashSet keys = (HashSet) m_cache.get(timeGrid[i]);
				// update each grid data.
				if (keys == null)
				{
					keys = new HashSet();
					m_cache.put(timeGrid[i], keys);
				}

				// 2. set busy flag.
				if (keys.contains(key))
				{
					// busy
					retval = true;
					busyIndex = i;
				}

				// trigger the next level grid.
				if (i <= busyIndex + 1)
				{
					keys.add(key);
					// update the grid value.
					grid = now / timeGrid[i];
					m_grid.put(timeGrid[i], grid);
				}
			}
		}
		// return true if any one is timeGrid is busy.
		if(retval)
		{
			int debug=0;
		}
		return retval;
	}
}
