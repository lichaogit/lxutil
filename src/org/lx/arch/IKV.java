package org.lx.arch;

import java.util.Set;

import redis.clients.jedis.Tuple;

public interface IKV
{
	public String get(String key);

	public boolean put(String key, String value);

	public void remove(String key);

	public long zadd(String key, Double score, String member);

	public Double zscore(String key, String member);

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end);
}
