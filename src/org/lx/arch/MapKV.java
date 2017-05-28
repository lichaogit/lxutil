package org.lx.arch;

import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Tuple;

public class MapKV implements IKV
{
	Map _map;

	public MapKV(Map map)
	{
		_map = map;
	}

	@Override
	public String get(String key)
	{
		Object val = _map.get(key);
		return val == null ? null : val.toString();
	}

	@Override
	public boolean put(String key, String value)
	{
		_map.put(key, value);
		return true;
	}

	@Override
	public void remove(String key)
	{
		// TODO Auto-generated method stub
		_map.remove(key);
	}

	@Override
	public long zadd(String key, Double score, String member)
	{
		// TODO Auto-generated method stub
		long retval = 0;
		return retval;

	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end)
	{
		// TODO Auto-generated method stub
		Set<Tuple> retval = null;
		return retval;
	}

	@Override
	public Double zscore(String key, String member)
	{
		// TODO Auto-generated method stub
		Double retval = null;
		return retval;

	}

}
