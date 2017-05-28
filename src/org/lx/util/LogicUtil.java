package org.lx.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LogicUtil
{
	public static class DecenderComparator implements Comparator
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			Comparable c1 = (Comparable) o1;
			Comparable c2 = (Comparable) o2;

			return c2.compareTo(c1);
		}
	}

	public static class AsenderComparator implements Comparator
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			Comparable c1 = (Comparable) o1;
			Comparable c2 = (Comparable) o2;

			return c1.compareTo(c2);
		}
	}

	public static class DecenderValueComparator implements Comparator<Entry>
	{
		@Override
		public int compare(Map.Entry o1, Map.Entry o2)
		{
			Comparable c1 = (Comparable) o1.getValue();
			Comparable c2 = (Comparable) o2.getValue();

			return c2.compareTo(c1);
		}
	}

	public static class AscenderValueComparator implements Comparator<Entry>
	{
		@Override
		public int compare(Map.Entry o1, Map.Entry o2)
		{
			Comparable c1 = (Comparable) o1.getValue();
			Comparable c2 = (Comparable) o2.getValue();

			return c1.compareTo(c2);
		}
	}

	public static class MatchedLatestValueComparator implements
			Comparator<Entry>
	{
		Number matchedVal;

		boolean abs;

		public MatchedLatestValueComparator(Number matchedVal, boolean abs)
		{
			this.matchedVal = matchedVal;
			this.abs = abs;
		}

		@Override
		public int compare(Map.Entry o1, Map.Entry o2)
		{
			long c1 = (((Number) o1.getValue()).longValue() - matchedVal
					.longValue());
			long c2 = (((Number) o2.getValue()).longValue() - matchedVal
					.longValue());

			if (abs)
			{
				c1 = Math.abs(c1);
				c2 = Math.abs(c2);
			}

			return (int) (c1 - c2);
		}
	}

	public static interface GroupRender
	{
		public Object getGroupID(Object node, Object groupKey);
	}

	/**
	 * the group and generateTree is independent. the user should provide the
	 * groupValueRender.
	 * @param nodes
	 * @param groupKey
	 * @param ngr
	 * @return
	 */
	public static HashMap group(Collection nodes, Object groupKey,
			GroupRender ngr)
	{
		HashMap retval = new HashMap();
		if (groupKey != null)
		{
			Object node = null;
			Iterator node_it = nodes.iterator();
			Object groupName = null;
			ArrayList group = null;
			while (node_it.hasNext())
			{
				node = node_it.next();
				groupName = ngr.getGroupID(node, groupKey);
				if (groupName != null)
				{
					if (retval.containsKey(groupName))
					{
						group = (ArrayList) retval.get(groupName);
					} else
					{
						group = new ArrayList();
						retval.put(groupName, group);
					}
					group.add(node);
					node_it.remove();
				}
			}
		}
		return retval;
	}

	/**
	 * byte to ASCII.
	 * @param b
	 * @return
	 */
	public static byte[] byte2ascii(byte[] b)
	{
		byte[] retval = new byte[b.length * 2];
		byte tmp;
		byte offset;
		for (int i = 0; i < b.length; i++)
		{
			tmp = (byte) ((b[i] & 0xf0) >> 4);
			offset = tmp - 9 <= 0 ? ((byte) 48) : ((byte) 'A' - 10);
			retval[i * 2] = (byte) (tmp + offset);
			tmp = (byte) (b[i] & 0x0f);
			offset = tmp - 9 <= 0 ? ((byte) 48) : ((byte) 'A' - 10);
			retval[i * 2 + 1] = (byte) (tmp + offset);

		}
		return retval;
	}

	/**
	 * convert the ASCII to byte.
	 * @param b
	 * @return
	 */
	// public static byte[] ascii2byte(byte[] b)
	// {
	// byte[] retval = new byte[b.length];
	// for (int i = 0; i < b.length; i++)
	// {
	// retval[i * 2] = (byte) ( (b[i] & 0xf0) >> 4 + '0');
	// retval[i * 2 + 1] = (byte) (b[i] & 0x0f + '0');
	// }
	// return retval;
	// }
	/**
	 * char to byte.
	 * @param c
	 * @param radix
	 * @return
	 */
	public static byte[] char2byte(char[] c, int radix)
	{
		byte[] retval = null;
		boolean odd = c.length % 2 != 0;
		retval = new byte[(1 + c.length) / 2];

		StringBuffer work = new StringBuffer();
		if (odd)
		{
			work.append('0');
			work.append(c[0]);
			retval[0] = Short.valueOf(work.toString(), radix).byteValue();
		}
		for (int i = odd ? 1 : 0; i < c.length; i += 2)
		{
			work.delete(0, work.length());
			work.append(c[i]);
			work.append(c[i + 1]);
			retval[(i + 1) / 2] = Short.valueOf(work.toString(), radix)
					.byteValue();
		}
		return retval;
	}

	/**
	 * byte to char, this version's performance if lower than new version.
	 * @param b
	 * @return
	 */
	public static char[] byte2char_v1(byte[] b)
	{
		char[] retval = new char[b.length * 2];
		String tmp = null;
		for (int i = 0; i < b.length; i++)
		{
			tmp = Integer.toHexString(b[i]);
			retval[i * 2] = tmp.length() - 2 < 0 ? '0' : tmp.charAt(tmp
					.length() - 2);
			retval[i * 2 + 1] = tmp.charAt(tmp.length() - 1);
		}
		return retval;
	}

	/**
	 * convert the byte[] to the char[];
	 * @param bs
	 * @return
	 */
	public static char[] byte2char(byte[] bs)
	{
		char[] retval = new char[bs.length * 2];
		byte tmp;
		for (int i = 0; i < bs.length; i++)
		{
			tmp = (byte) ((bs[i] & 0xf0) >> 4);
			retval[i * 2] = tmp - 9 <= 0 ? (char) (tmp + '0')
					: (char) (tmp + 'A' - 10);
			tmp = (byte) (bs[i] & 0x0f);
			retval[i * 2 + 1] = tmp - 9 <= 0 ? (char) (tmp + '0')
					: (char) (tmp + 'A' - 10);
		}
		return retval;
	}

	/**
	 * convert the String array to a int.
	 * @param s
	 * @return
	 */
	public static int buildInt(String[] s)
	{
		int retval = 0;
		int subVer = 0;
		for (int i = 0; i < s.length && i < 4; i++)
		{
			subVer = Integer.parseInt(s[i]);
			retval |= subVer << ((3 - i) * 8);
		}
		return retval;
	}

	/**
	 * fetch the license.
	 * @param reader
	 * @param start
	 * @param end
	 * @return
	 */

	public static Reader getLicense(Reader reader, String start, String end)
	{
		Reader retval = null;
		try
		{
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			// search for the License.
			do
			{
				line = br.readLine();
			} while (line == null || line.trim().equals(start.trim()) == false);

			StringBuffer sb = new StringBuffer();

			do
			{
				line = br.readLine();
				if (line != null && line.trim().equals(end.trim()) == false)
				{
					sb.append(line);
				}
			} while (line != null);
			retval = new StringReader(sb.toString());
		} catch (IOException e)
		{
		}
		return retval;

	}

	public static Reader getLicense(Reader reader, long start, long end)
	{
		Reader retval = null;
		try
		{
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			// search for the License.

			Checksum checksumEngine = new Adler32();
			byte[] bytes = null;
			long checksum = 0;
			do
			{
				line = br.readLine();
				if (line == null)
				{
					break;
				}
				bytes = line.getBytes();
				checksumEngine.reset();
				checksumEngine.update(bytes, 0, bytes.length);
				checksum = checksumEngine.getValue();

			} while (checksum != start);

			StringBuffer sb = new StringBuffer();

			do
			{
				line = br.readLine();

				if (line == null)
				{
					break;
				}
				bytes = line.getBytes();
				checksumEngine.reset();
				checksumEngine.update(bytes, 0, bytes.length);
				checksum = checksumEngine.getValue();
				if (checksum == end)
				{
					break;
				}
				sb.append(line);
			} while (true);
			retval = new StringReader(sb.toString());
		} catch (IOException e)
		{
		}
		return retval;

	}

	public static boolean isInArray(Object[] array, Object id)
	{
		return getArrayIndex(array, id) != -1;
	}

	public static int getArrayIndex(Object[] array, Object obj)
	{
		int retval = -1;
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(obj))
			{
				retval = i;
				break;
			}
		}
		return retval;
	}

	public static int getArrayIndex(int[] array, int obj)
	{
		int retval = -1;
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				retval = i;
				break;
			}
		}
		return retval;
	}

	public static int getArrayIndex(long[] array, long obj)
	{
		int retval = -1;
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				retval = i;
				break;
			}
		}
		return retval;
	}

	public static interface MatchCondition
	{
		/**
		 * if the obj2's value is match the node, return true;
		 * @param node
		 * @return
		 */
		public boolean isMatch(SimpleTreeModel node);

		/**
		 * determine whether to search the node.
		 * @return
		 */
		public boolean searchContinue();

	}

	public static void shallowSearch(SimpleTreeModel model, Collection result,
			MatchCondition mm)
	{
		shallowSearch(model, result, mm, 0);
	}

	/**
	 * find the mode that match with the MatchCondition. shallowSearch search :
	 * the shallow model locate the front
	 * @param mm
	 * @return
	 */
	public static void shallowSearch(SimpleTreeModel model, Collection result,
			MatchCondition mm, int layer_count)
	{
		// search the child.
		Collection c = model.getChildren();
		if (c != null && c.size() > 0)
		{
			Iterator it = c.iterator();
			SimpleTreeModel node = null;
			while (it.hasNext())
			{
				node = (SimpleTreeModel) it.next();

				if (mm.isMatch(node))
				{
					result.add(node);
					// search the first match model if the searchContinue is
					// false
					if (mm.searchContinue() == false)
					{
						return;
					}
				} else if (layer_count > 0)
				{
					shallowSearch(node, result, mm, layer_count - 1);
				}
			}
		}
	}

	/**
	 * find the mode that match with the MatchCondition. deepSearch search : the
	 * deep model locate the front
	 * @param mm
	 * @return
	 */
	public static void deepSearch(SimpleTreeModel model, Collection result,
			MatchCondition mm)
	{
		// search the child.
		Collection c = model.getChildren();
		if (c != null && c.size() > 0)
		{
			Iterator it = model.getChildren().iterator();
			SimpleTreeModel node = null;
			Collection child_c = null;
			while (it.hasNext())
			{
				node = (SimpleTreeModel) it.next();
				child_c = node.getChildren();
				if (child_c != null && child_c.size() > 0) // search the child
				{
					deepSearch(node, result, mm);
				}

				// add the deeper before the shallower model.
				if (mm.isMatch(node))
				{
					result.add(node);
					// search the first match model if the searchContinue is
					// false
					if (mm.searchContinue() == false)
					{
						return;
					}
				}
			}
		}
	}

	public static void main(String[] argv)
	{
		try
		{
			FileReader reader = new FileReader(argv[0]);
			Reader lr = getLicense(reader, 864289971L, 3053193431L);

		} catch (IOException e)
		{
		}
	}

	public static interface Task
	{
		public Task[] getDepends() throws Exception;

		public void doTask() throws Exception;

		public boolean isMandatory();
	}

	protected static void doTask(Task task, HashSet inited) throws Exception
	{
		try
		{
			if (inited.contains(task))
			{
				return;
			}

			Task[] depends = task.getDepends();
			if (depends != null && depends.length > 0)
			{
				for (int i = 0; i < depends.length; i++)
				{
					doTask(depends[i], inited);
					inited.add(depends[i]);
				}
			}

			task.doTask();
		} catch (Exception e)
		{
			if (task.isMandatory())
			{
				throw e;
			}
		}
	}

	public static void doDependsTask(Map tasks) throws Exception
	{
		HashSet inited = new HashSet(tasks.size());
		Iterator it = tasks.values().iterator();
		Task task = null;
		while (it.hasNext())
		{
			task = (Task) it.next();
			doTask(task, inited);
		}
	}

	public static int arrayCompare(byte[] b1, byte[] b2)
	{
		int retval = 0;
		do
		{
			// 1.length
			if (b1.length != b2.length)
			{
				retval = b1.length - b2.length > 0 ? 1 : -1;
				break;
			}
			// 2.compare value when length are same.
			for (int i = 0; i < b1.length; i++)
			{
				if (b1[i] == b2[i])
				{
					continue;
				}
				retval = b1[i] - b2[i] > 0 ? 1 : -1;
				break;
			}
		} while (false);
		return retval;
	}

	static public int getCurPhaseIndex(float[] times, float passTime)
	{
		int retval = -1;
		float workSec = 0;
		for (int i = 0; i < times.length; i++)
		{
			workSec += times[i];
			if (workSec >= passTime)
			{
				retval = i;
				break;
			}
		}
		return retval;
	}

	static public int getCurPhaseIndex(Object[] times, float passTime)
	{
		int retval = -1;
		float workSec = 0;
		for (int i = 0; i < times.length; i++)
		{
			workSec += ((Number) times[i]).floatValue();
			if (workSec >= passTime)
			{
				retval = i;
				break;
			}
		}
		return retval;
	}

	static public float fmod(float total, float f)
	{
		/*
		 * float retval = total; while (retval >= f) { retval -= f; } return
		 * retval;
		 */
		return total % f;
	}

	public static void mapAddLong(Map target, Map src)
	{
		Iterator it = src.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			long val = ((Number) src.get(key)).longValue();
			mapAdd(target, key, val);
		}
	}

	public static final boolean MAP_OP_KEY_INCLUDE = true;

	public static final boolean MAP_OP_KEY_EXCLUDE = false;

	public static List mapGetMatchedKeys(Map target, boolean include,
			Collection keys, int count, long matchVal)
	{
		return mapGetTopKeys(target, include, keys, count,
				new MatchedLatestValueComparator(matchVal, true));
	}

	public static List mapGetMinKeys(Map target, boolean include,
			Collection keys, int count)
	{
		return mapGetTopKeys(target, include, keys, count,
				new AscenderValueComparator());
	}

	public static List mapGetMaxKeys(Map target, boolean include,
			Collection keys, int count)
	{
		return mapGetTopKeys(target, include, keys, count,
				new DecenderValueComparator());
	}

	public static List mapGetKeys(Map target, long val1)
	{
		ArrayList results = new ArrayList();
		Iterator it = target.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			Number val = ((Number) target.get(key));
			if (val1 == val.longValue())
			{
				results.add(key);
			}
		}
		return results;
	}

	protected static List mapGetTopKeys(Map target, boolean include,
			Collection keys, int count, Comparator comp)
	{
		HashMap<Object, Comparable> wkMap = new HashMap<Object, Comparable>();
		wkMap.putAll(target);

		// 1.process the include&keys
		if (keys != null)
		{
			Set remainKeys = wkMap.keySet();
			if (include)
			{
				remainKeys.retainAll(keys);
			} else
			{
				remainKeys.removeAll(keys);
			}
		}
		// 2.process the sort by value.
		List<Map.Entry> sortList = new ArrayList<Map.Entry>(wkMap.entrySet());
		Collections.sort(sortList, comp);

		// 3.process the results.
		ArrayList results = new ArrayList();
		// for
		int index = 0;
		Comparable prev = null;
		for (Entry en : sortList)
		{
			Comparable c = (Comparable) en.getValue();
			if (prev == null || !c.equals(prev))
			{
				index++;
				prev = c;
			}
			if (index > count)
			{
				break;
			}
			results.add(en.getKey());
		}

		return results;
	}

	public static long mapSum(Map map, boolean abs, boolean include,
			Collection keys)
	{
		long retval = 0;
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			if (keys != null)
			{
				if (include && (!keys.contains(key)))
				{
					continue;
				}

				if (!include && keys.contains(key))
				{
					continue;
				}
			}
			long val = ((Number) map.get(key)).longValue();
			if (abs)
			{
				val = Math.abs(val);
			}
			retval += val;
		}
		return retval;
	}

	public static void mapAdd(Map map, ValueRender vr)
	{
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			long val = ((Number) map.get(key)).longValue();
			Number ratio = (Number) vr.get(key.toString());
			if (ratio != null)
			{
				long val2 = ((Number) ratio).longValue();
				map.put(key, val + val2);
			}
		}
	}

	public static void mapAdd(Map map, Object key, long val)
	{
		if (map.containsKey(key))
		{
			map.put(key, ((Number) map.get(key)).longValue() + val);
		} else
		{
			map.put(key, val);
		}
	}

	public static void mapMul(Map map, long mulVal)
	{
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			long val = ((Number) map.get(key)).longValue();
			map.put(key, val * mulVal);
		}
	}

	public static void mapMul(Map map, Map ratios)
	{
		Iterator it = ratios.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			if (!map.containsKey(key))
			{
				continue;
			}
			long val = ((Number) map.get(key)).longValue();
			long mulVal = ((Number) ratios.get(key)).longValue();
			map.put(key, val * mulVal);
		}
	}

	public static void mapMul(Map map, ValueRender vr)
	{
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			long val = ((Number) map.get(key)).longValue();
			Number ratio = (Number) vr.get(key.toString());
			if (ratio != null)
			{
				long mulVal = ((Number) ratio).longValue();
				map.put(key, val * mulVal);
			}
		}
	}

	public static void mapAbs(Map map)
	{

		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			long val = ((Number) map.get(key)).longValue();
			map.put(key, Math.abs(val));
		}
	}

	public static void mapNeg(Map map)
	{
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			long val = ((Number) map.get(key)).longValue();
			map.put(key, -val);
		}
	}

	public static Map mapDiff(Map o, Map n)
	{
		Map retval = new HashMap();
		if (o != null && n != null)
		{
			Iterator it = n.keySet().iterator();
			Object key;
			while (it.hasNext())
			{
				key = it.next();
				Object val = n.get(key);
				if (o.containsKey(key) && val.equals(o.get(key)))
				{
					// skip the same item.
					continue;
				}
				retval.put(key, val);
			}
		} else
		{
			if (o != null)
			{
				retval.putAll(o);
			}

			if (n != null)
			{
				retval.putAll(n);
			}
		}
		return retval;
	}

	static java.util.Random m_seed = new java.util.Random();

	/*
	 * min:include max:include
	 */
	public static long random(long min, long max)
	{
		long rMax = m_seed.nextLong();
		if (rMax < 0)
		{
			rMax = -rMax;
		}
		return min >= max ? max : (rMax % max) % (max - min + 1) + min;
	}

	/*
	 * min:include max:include
	 */
	public static int random(int min, int max)
	{
		return min >= max ? max : m_seed.nextInt(max) % (max - min + 1) + min;
	}

	public static Object randomObj(Object[] list)
	{
		Object retval = null;
		do
		{
			if (list == null || list.length == 0)
			{
				break;
			}
			if (list.length == 1)
			{
				retval = list[0];
				break;
			}
			retval = list[m_seed.nextInt(list.length)];
		} while (false);
		return retval;
	}

	// support >,>=,<,<=,!=,==
	public static Object getConfMatchItem(Map map, String id)
	{
		// 1.match exactly firstly.
		Object retval = map.get(id);
		if (retval == null)
		{
			// 2. match by regex.
			Iterator it = map.keySet().iterator();
			while (it.hasNext())
			{
				String key = String.valueOf(it.next());
				// handle the >,>=,<,<=
				char startChar = key.charAt(0);
				boolean match = false;
				switch (startChar)
				{
				case '>':
				case '<':
				{
					String expr = (key.length() > 2 && key.charAt(1) == '=') ? key
							.substring(2) : key.substring(1);

					boolean containEquals = key.length() - expr.length() > 1;
					int matchResult = id.compareTo(expr);
					switch (startChar)
					{
					case '>':
					{
						match = containEquals ? matchResult >= 0
								: matchResult > 0;
						break;
					}
					case '<':
					{
						match = containEquals ? matchResult <= 0
								: matchResult < 0;
						break;
					}
					}
					break;
				}
				default:
				{
					if (startChar == '=' && key.charAt(1) == '=')
					{
						match = id.compareTo(key.substring(2)) == 0;
					} else if (startChar == '!' && key.charAt(1) == '=')
					{
						match = id.compareTo(key.substring(2)) != 0;
					} else
					{
						// regex
						match = id.matches(key);
					}
					break;
				}

				}

				if (match)
				{
					retval = map.get(key);
					break;
				}
			}
		}
		return retval;
	}

	// or for same levels
	// and for inherit levels.
	public static Object getConfMatchItem(final Map model, final Map input)
	{
		List filterModel = (List) model.get("filter");
		Map entries = (Map) model.get("entries");

		Object matchEntry = getConfMatchEntry(filterModel, input);
		return matchEntry != null ? JsonUtil.parseJpath(entries,
				matchEntry.toString(), false) : null;
	}

	public static Object getConfMatchItem(final Map model, final Map input,
			String filterKey, String entriesKey)
	{
		List filterModel = (List) model.get(filterKey);
		Map entries = (Map) model.get(entriesKey);

		Object matchEntry = getConfMatchEntry(filterModel, input);
		return matchEntry != null ? JsonUtil.parseJpath(entries,
				matchEntry.toString(), false) : null;
	}

	public static Object getConfMatchEntry(final List filterModel,
			final Map input)
	{
		String filterModelStr = filterModel.toString();
		String filterModelRawStr = StringEx.replaceAll(filterModelStr,
				"\\$\\(([\\w/]+)\\)", new StringValueRender() {
					@Override
					public String get(String key)
					{
						Object obj = JsonUtil.parseJpath(input, key, false);
						return obj == null ? "" : obj.toString();
					}

				});

		List filterRawModel = (List) JsonUtil.parseJpath(filterModelRawStr,
				null);
		return getConfMatchEntry(filterRawModel);
	}

	// the model is plain without var.
	protected static Object getConfMatchEntry(final List filterModel)
	{
		Object retval = null;
		Iterator it = filterModel.iterator();
		while (it.hasNext())
		{
			retval = getConfMatchEntry((Map) it.next());
			if (retval != null)
			{
				break;
			}
		}
		return retval;
	}

	protected static Object getConfMatchEntry(final Map filterModel)
	{
		// 1.match exactly firstly.
		Object retval = null;

		Iterator it = filterModel.keySet().iterator();
		while (it.hasNext())
		{
			String attrKey = (String) it.next();
			Map attrModel = (Map) filterModel.get(attrKey);
			Object nextFilter = getConfMatchItem(attrModel, attrKey);
			if (nextFilter == null)
			{
				continue;
			}
			// string
			if (nextFilter instanceof String)
			{
				retval = nextFilter.toString();
				break;
			} else if (nextFilter instanceof Map)
			{
				// Map
				retval = getConfMatchEntry((Map) nextFilter);
			}
		}
		return retval;
	}

	/**
	 * get the level base on the attr value.
	 * @param levels
	 * @param attr
	 * @param num
	 * @return 0-levels.size()-1.
	 */
	public static Object getLevelObj(List levels, String attr, long num)
	{
		int level = 0;
		Iterator it = levels.iterator();
		while (it.hasNext())
		{
			Map levelObj = (Map) it.next();
			long attrVal = ((Number) levelObj.get(attr)).longValue();
			if (num < attrVal)
			{
				break;
			}
			level++;
		}
		level = level >= levels.size() ? levels.size() - 1 : level;
		return levels.get(level);
	}

	public static int[] clone(int[] a)
	{
		int[] retval = new int[a.length];
		System.arraycopy(a, 0, retval, 0, a.length);
		return retval;
	}

	/**
	 * the array length should be same.
	 * @param a
	 * @return
	 */
	public static int[][] clone(int[][] a)
	{
		int[][] retval = new int[a.length][];
		for (int i = 0; i < a.length; i++)
		{
			retval[i] = new int[a[i].length];
			System.arraycopy(a[i][0], 0, retval[i][0], 0, a[i].length);
		}
		return retval;
	}

	/**
	 * Òì»ò
	 * @param src
	 * @return
	 */
	public static String xor(String src, int code)
	{
		if (src == null)
		{
			return null;
		}
		char[] charArray = src.toCharArray();
		for (int i = 0; i < charArray.length; i++)
		{
			charArray[i] = (char) (charArray[i] ^ code);
		}
		return new String(charArray);
	}

	public static int swap32(int i)
	{
		return (i & 0xFF) << 24 | (0xFF & i >> 8) << 16 | (0xFF & i >> 16) << 8
				| (0xFF & i >> 24);
	}
}
