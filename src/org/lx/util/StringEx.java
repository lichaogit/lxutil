package org.lx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StringEx
{
	String[][] m_strProperties;

	/**
	 * (string,",","(",")")
	 * @param parameters
	 * @param sep
	 * @param excludeStart
	 * @param excludeEnd
	 * @return
	 * @throws Exception
	 */
	public static String[] parseParameters(String parameters, char sep,
			char excludeStart, char excludeEnd) throws Exception
	{
		String[] retval = null;
		ArrayList al = new ArrayList();
		parameters = parameters.trim();
		int len = parameters.length();

		// if excludeStart,++ if excludeEnd,--
		int stack = 0;

		int start = 0;
		int end = 0;
		char c;
		String param;
		for (int i = 0; i < len; i++)
		{
			c = parameters.charAt(i);
			if (c == excludeStart)
			{
				stack++;
			} else if (c == excludeEnd)
			{
				stack--;
			}
			// the stack must >0.
			if (stack < 0)
			{
				throw new Exception("'" + excludeStart + "' must match '"
						+ excludeEnd + "' at offset:" + i);
			}
			if (stack > 0 || c != sep)
			{
				end++;
				continue;
			}

			if (start <= end)
			{
				// if meet the sep that not contains in the startChar&endChar.
				param = parameters.substring(start, end);
				al.add(param);
			}
			// start from the next char.
			start = ++end;
		}

		if (stack != 0)
		{
			throw new Exception("'" + excludeStart + "' must match '"
					+ excludeEnd + "'");
		}

		if (start < end)
		{
			param = parameters.substring(start, end);
			al.add(param);
		}
		retval = (String[]) al.toArray(new String[0]);

		return retval;
	}

	public static String[] parseCmdLineParameters(String parameters)
	{
		String[] retval = null;
		ArrayList al = new ArrayList();
		parameters = parameters.trim();
		int len = parameters.length();

		// state:0:blank, 1:words
		int state = 0;
		// switch the flag if meet ' or "
		boolean flag = false;

		int start = 0;
		int end = 0;
		char c, tmp;
		String param;
		for (int i = 0; i < len; i++)
		{
			c = parameters.charAt(i);
			// switch status
			if (state == 0)
			{
				// start a parameter
				if (!Character.isSpaceChar(c))
				{
					start = end = i;
					state = 1;
				}
			} else if (state == 1)
			{
				if (flag == false && Character.isSpaceChar(c))
				{
					state = 0;
					if (start != end)
					{
						tmp = parameters.charAt(start);
						// parse a parameter.
						if (tmp == '\'' || tmp == '\"')
						{
							start++;
						}
						tmp = parameters.charAt(end - 1);
						if (tmp == '\'' || tmp == '\"')
						{
							end--;
						}
						if (start <= end)
						{
							param = parameters.substring(start, end);
							al.add(param);
						}
						start = end;
					}
				}
			}

			// switch the flag if meet \' or \".
			if (('\'' == c || '\"' == c)
					&& (i == 0 || parameters.charAt(i - 1) != '\\'))
			{
				flag = !flag;
			}
			end++;
		}

		if (start != end)
		{
			// parse a parameter.
			tmp = parameters.charAt(start);
			// parse a parameter.
			if (tmp == '\'' || tmp == '\"')
			{
				start++;
			}
			tmp = parameters.charAt(end - 1);
			if (tmp == '\'' || tmp == '\"')
			{
				end--;
			}
			if (start <= end)
			{
				param = parameters.substring(start, end);
				al.add(param);
			}
		}
		retval = (String[]) al.toArray(new String[0]);

		return retval;
	}

	public final static String DEFAULT_TEXT_CONTENTTYPE_MATCHER = "text/[\\w\\+\\-\\.]+|application/(xml|xhtml\\+xml|x-httpd-php|x-javascript|x-tcl|x-sh|x-csh|vnd\\.wap\\.wmlscriptc|vnd\\.wap\\.wmlc)+$";

	public final static String DEFAULT_BIN_CONTENTTYPE_MATCHER = "(image|audio|video|audio)/[\\w\\+\\-\\.]+$";

	public static String contentType2dataType(String param)
	{
		String retval = "binary";
		final String[][] types = new String[][] {
				{ "text", DEFAULT_TEXT_CONTENTTYPE_MATCHER },
				{ "binary", DEFAULT_BIN_CONTENTTYPE_MATCHER } };

		String[] item = null;
		String group = null;
		for (int i = 0; i < types.length; i++)
		{
			item = types[i];
			group = StringEx.regexFind(param, item[1], 0);
			if (group != null && param.startsWith(group))
			{
				retval = item[0];
				break;
			}
		}
		return retval;
	}

	public StringEx(String strProperties)
	{
		if (strProperties != null)
		{
			String[] Properties = split(strProperties, ";");
			m_strProperties = new String[Properties.length][];
			String[] Property = null;
			for (int i = 0; i < m_strProperties.length; i++)
			{
				Property = split(Properties[i], "=");
				m_strProperties[i] = new String[2];
				if (Property != null)
				{
					if (Property.length >= 1)
					{
						m_strProperties[i][0] = Property[0];
					}
					if (Property.length >= 2)
					{
						m_strProperties[i][1] = Property[1];
					}
				}
			}
		}
	}

	/**
	 * mask the regex character
	 * @param str
	 * @return
	 */
	public static String regexMask(String str)
	{
		String retval = str;
		retval = StringEx.replace(retval, "\\", "\\\\");
		retval = StringEx.replace(retval, ".", "\\.");
		retval = StringEx.replace(retval, "$", "\\$");
		retval = StringEx.replace(retval, "^", "\\^");
		retval = StringEx.replace(retval, "{", "\\{");
		retval = StringEx.replace(retval, "}", "\\}");
		retval = StringEx.replace(retval, "[", "\\[");
		retval = StringEx.replace(retval, "]", "\\]");
		retval = StringEx.replace(retval, "+", "\\+");
		return retval;
	}

	public final static String DEFAULT_VAR_MATCHER = "\\$\\((\\w+)\\)";// '\$\((\w+\)'

	public final static String DEFAULT_XPATH_NODETEST_MATCHER = "/*(\\w+)?(\\[[^\\[\\]]+\\]*)?";

	public final static String DEFAULT_DOMAIN_MATCHER = "(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}\\.?";

	public final static String DEFAULT_IP_MATCHER = "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3}";

	/**
	 * three groups: 1. protocol. 2. domain. 3. port.
	 */
	public final static String DEFAULT_URLDOMAIN_MATCHER = "(?:(https?|ftp|file)\\:\\/\\/){0,1}("
			+ DEFAULT_DOMAIN_MATCHER + ")(?:\\:(\\d{1,5})){0,1}";

	public final static String DEFAULT_PROXYSERVER_MATCHER = "(?:(http[s]?|socks[4|5])\\:\\/\\/){1}(?:(\\S+)\\:(\\S+)@)?("
			+ DEFAULT_DOMAIN_MATCHER
			+ "|"
			+ DEFAULT_IP_MATCHER
			+ ")(?:\\:(\\d{1,5}))?";

	// '(\w+):\/\/([^/:]+)(:\d*)?([^# ]*)'
	// http://msdn.microsoft.com:80/scripting/default.htm
	// http
	// msdn.microsoft.com
	// 80
	// scripting/default.htm

	public static String replaceAll(String srcStr, StringValueRender rr)
	{
		return replaceAll(srcStr, DEFAULT_VAR_MATCHER, rr);
	}

	public static String regexFind(String str, String regex, int groupIndex)
	{
		String retval = null;
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(str);
		if (matcher.find())
		{
			retval = matcher.group(groupIndex);
		}
		return retval;
	}

	public static String[] regexFind(String str, String regex, int[] groupIndexs)
	{
		String[] retval = new String[groupIndexs.length];
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(str);
		if (matcher.find())
		{
			for (int i = 0; i < groupIndexs.length; i++)
			{
				retval[i] = matcher.group(groupIndexs[i]);
			}
		}
		return retval;
	}

	/**
	 * replace the matcher to replace in srcStr with the rr by the first group.
	 * @param srcStr
	 * @param matcher
	 * @param rr
	 *        the ReplaceRender interface that can provide new string to replace
	 *        old string.
	 * @return
	 */
	public static String replaceAll(String srcStr, String matcherStr,
			StringValueRender rr)
	{
		Pattern p = Pattern.compile(matcherStr);
		StringBuffer buf = new StringBuffer();
		buf.append(srcStr);
		String wkStr = null;
		do
		{
			wkStr = buf.toString();
			// clear the buf.
			buf.delete(0, buf.length());
			Matcher matcher = p.matcher(wkStr);
			String[] group = new String[matcher.groupCount()];
			String rep = null;
			while (matcher.find())
			{
				for (int i = 1; i <= group.length; i++)
				{
					group[i - 1] = matcher.group(i);
				}
				rep = rr.get(group[0]);
				// if the return value is null, doen't not replace
				// it---continue.
				if (rep == null)
				{
					continue;
				}
				matcher.appendReplacement(buf, rep);
			}
			matcher.appendTail(buf);
		} while (!buf.toString().equals(wkStr));
		return buf.toString();
	}

	/**
	 * the difference between parseVar and replaceAll is£º parseVar return Object
	 * if the srcStr is a var. replaceAll return String if the srcStr is a var.
	 * @param srcStr
	 * @param varsMap
	 * @return
	 */
	public static Object parseVar(String srcStr, ValueRender vr)
	{
		return parseVar(srcStr, DEFAULT_VAR_MATCHER, vr);
	}

	public static Object parseVar(String srcStr, String matcherStr,
			ValueRender vr)
	{
		Object retval = null;
		Pattern p = Pattern.compile(matcherStr);
		Matcher matcher = p.matcher(srcStr);
		if (matcher.matches())
		{
			// the params is just a var.
			Object val = vr == null ? null : vr.get(matcher.group(1));
			retval = val == null ? srcStr : val;
		} else
		{
			StringBuffer buf = new StringBuffer();
			String[] group = new String[matcher.groupCount()];
			Object varObj = null;
			// avoid the matcher.matches.
			matcher.reset();
			while (matcher.find())
			{
				for (int i = 1; i <= group.length; i++)
				{
					group[i - 1] = matcher.group(i);
				}
				varObj = vr == null ? null : vr.get(group[0]);
				// if the return value is null, doen't not replace
				// it---continue.
				if (varObj == null)
				{
					continue;
				}
				matcher.appendReplacement(buf, String.valueOf(varObj));
			}
			matcher.appendTail(buf);
			retval = buf.toString();
		}
		return retval;
	}

	/**
	 * replace the matcher to replace in srcStr, it defferent from the
	 * String.replaceAll, String.replaceAll use the reguler express, and can not
	 * replace such as "$(AS)" to as
	 * @param srcStr
	 * @param matcher
	 * @param replace
	 * @return
	 */
	public static String replace(String srcStr, String matcher, String replace)
	{
		String retval = null;
		if (srcStr != null)
		{
			StringBuffer buf = new StringBuffer();
			int endIndex = 0;
			int beginIndex = 0;
			String workStr = srcStr;
			do
			{
				endIndex = workStr.indexOf(matcher);
				if (endIndex == -1)
				{
					break;
				}

				buf.append(workStr.substring(beginIndex, endIndex));
				if (replace != null)
				{
					buf.append(replace);
				}
				workStr = workStr.substring(endIndex + matcher.length());
			} while (true);
			// append the rest
			buf.append(workStr);
			retval = buf.toString();

		}
		return retval;
	}

	public static void main(String[] argv)
	{
		String ssss = "$(abc)".replaceAll("\\$\\((\\w+)\\)", "$1");
		String path = getPackagePath(StringEx.class);
		String src = "er\nrt\n\n\n\nfg\n\n\n";
		int i = StringEx.count(src, "\n\n");

		StringBuffer sd = new StringBuffer(
				"\tAddLanguage es .es hello   \t   world! !yy");
		boolean retval = StringEx.isBeginIgnoreWhiteCharCase(sd.toString(),
				"   AddLanguage es \t .es ");
		deleteIgnoreWhiteCharCase(sd, "hello worlD!");
		System.out.println(sd);
	}

	public static boolean isContainIgnoreWhiteCharCase(String srcStr, String str)
	{
		boolean retval = false;
		String[] array_srcStr = StringEx.split(srcStr.toLowerCase());
		String[] array_Str = StringEx.split(str.toLowerCase());
		for (int i = 0; i < array_srcStr.length; i++)
		{
			if (array_srcStr[i].equals(array_Str[0]) == false)
			{
				continue;
			}
			retval = true;
			for (int j = 0; j < array_Str.length; j++)
			{
				if (array_Str[j].equals(array_srcStr[i + j]) == false)
				{

					retval = false;
					break;

				}
			}
			// find
			if (retval == true)
			{
				break;
			}
		}
		return retval;
	}

	public static int count(String src, String countStr)
	{
		int retval = 0;

		String workStr = src;
		int iOffset = src.indexOf(countStr);
		int tmpOffset = 0;
		while (iOffset != -1)
		{
			retval++;
			tmpOffset = iOffset + countStr.length() - 1;
			if (tmpOffset > workStr.length())
			{
				iOffset = -1;
			} else
			{
				workStr = workStr.substring(tmpOffset + 1);
				iOffset = workStr.indexOf(countStr);
			}
		}
		return retval;
	}

	public static boolean isBeginIgnoreWhiteCharCase(String srcStr, String str)
	{
		boolean retval = true;
		String[] array_srcStr = StringEx.split(srcStr.toLowerCase());
		String[] array_Str = StringEx.split(str.toLowerCase());
		for (int i = 0; i < array_Str.length; i++)
		{
			if (array_srcStr[i].equals(array_Str[i]) == false)
			{
				retval = false;
				break;
			}
		}
		return retval;
	}

	/**
	 * this method will delete the content from srcbuffer according to the
	 * delStr, it will ignore the white space and low/Upper case.
	 * @param srcBuffer
	 *        : the target String Buffer
	 * @param delStr
	 *        : the content that will be deleted
	 */
	public static void deleteIgnoreWhiteCharCase(StringBuffer srcBuffer,
			String delStr)
	{
		// 1. convert all of the string to lowerCase words array

		String[] array_srcBuffer = StringEx.split(srcBuffer.toString()
				.toLowerCase());
		String[] array_delStr = StringEx.split(delStr.toLowerCase());
		// 2. search in dt params
		boolean bFind = false;
		int iIndex = 0;
		for (int i = 0; i < array_srcBuffer.length; i++)
		{
			if (array_srcBuffer[i].equals(array_delStr[0]))
			{
				// if found search the next attr
				int j = 1;
				for (; j < array_delStr.length
						&& j + i < array_srcBuffer.length; j++)
				{
					if (array_delStr[j].equals(array_srcBuffer[i + j]) == false)
					{
						break;
					}
				}

				if (j == array_delStr.length)
				{
					iIndex = i;
					bFind = true;
					break;
				}

			}
		}

		if (bFind == true)
		{
			int istartIndex = srcBuffer.indexOf(array_delStr[0]);
			int iendIndex = 0;
			// delete the array_delStr.length words from the srcBuffer.
			int iworkNum = 0;
			for (int i = istartIndex; i < srcBuffer.length(); i++)
			{
				// count the words

				while (i < srcBuffer.length()
						&& Character.isWhitespace(srcBuffer.charAt(i++)) == false)
				{
				}
				// find a word
				iworkNum++;
				// skip the whitechar.
				while (i < srcBuffer.length()
						&& Character.isWhitespace(srcBuffer.charAt(i++)))
				{
					;
				}
				if (iworkNum == array_delStr.length)
				{
					iendIndex = i - 1;
					if (i == srcBuffer.length())
					{
						iendIndex++;
					}
					break;
				}

			}
			// if the words reach the srcBuffer's tail, delete all of the tail.
			if (iendIndex == 0)
			{
				iendIndex = srcBuffer.length();
			}
			// omit the pre blank
			while (istartIndex - 1 >= 0
					&& Character
							.isWhitespace(srcBuffer.charAt(istartIndex - 1)))
			{
				istartIndex--;
			}
			srcBuffer.delete(istartIndex, iendIndex);
		}
	}

	public static String[] split(String str)
	{
		String retval[] = null;
		StringTokenizer token = new StringTokenizer(str);
		retval = new String[token.countTokens()];
		for (int i = 0; token.hasMoreTokens(); i++)
		{
			retval[i] = token.nextToken();
		}
		return retval;

	}

	public static String[] split(String str, String regx)
	{
		// 1. check the
		String retval[] = null;
		StringTokenizer token = new StringTokenizer(str, regx);
		retval = new String[token.countTokens()];
		for (int i = 0; token.hasMoreTokens(); i++)
		{
			retval[i] = token.nextToken();
		}
		return retval;
	}

	public Iterator iterator()
	{
		return new Itr();
	}

	public int getSize()
	{
		return m_strProperties == null ? 0 : m_strProperties.length;
	}

	public void setValue(int iIndex, String strValue)
	{
		if (m_strProperties != null && iIndex < m_strProperties.length)
		{
			m_strProperties[iIndex][1] = strValue;
		}
	}

	public void setValue(String strKey, String strValue)
	{
		if (m_strProperties == null)
		{
			return;
		}
		for (int i = 0; i < m_strProperties.length; i++)
		{
			if (m_strProperties[i].length == 2
					&& m_strProperties[i][0].equals(strKey))
			{
				// set the valur
				m_strProperties[i][1] = strValue;
				break;
			}
		}
	}

	/**
	 * get the Class's String
	 * @param clazz
	 * @return
	 */
	public static String getClassPath(Class clazz)
	{
		String retval = null;
		retval = clazz.getName();
		retval = "/" + retval.replaceAll("\\.", "/");
		return retval;
	}

	/**
	 * get the clazz's package's path.
	 * @param clazz
	 * @return
	 */
	public static String getPackagePath(Class clazz)
	{
		String retval = null;
		retval = clazz.getPackage().getName();
		retval = "/" + retval.replaceAll("\\.", "/");
		return retval;
	}

	/**
	 * parse the base64 string array. name value pair. Format: name:value,
	 * @param param
	 * @param cs
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String[][] parseBase64Param(String param, String cs)
			throws UnsupportedEncodingException
	{
		String[][] retval = null;
		do
		{
			if (param == null)
			{
				break;
			}
			String[] item = param.split(",");
			if (item == null || item.length == 0)
			{
				break;
			}
			ArrayList al = new ArrayList(item.length);
			String[] wk = null;
			for (int i = 0; i < item.length; i++)
			{
				wk = item[i].split(":");
				if (wk.length == 2)
				{
					wk[0] = Base64.decodeString(wk[0], cs);
					wk[1] = Base64.decodeString(wk[1], cs);
					al.add(wk);
				}
			}
			retval = (String[][]) al.toArray(new String[0][0]);
		} while (false);

		return retval;
	}

	public String toSting()
	{
		String retval = null;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_strProperties.length; i++)
		{
			buf.append(m_strProperties[i][0]);
			buf.append("=");
			if (m_strProperties[i].length == 2)
			{
				buf.append(m_strProperties[i][1]);
			}
			buf.append(";");
		}
		retval = buf.toString();
		return retval;
	}

	public String getKey(int iIndex)
	{
		String retval = null;
		if (m_strProperties != null && iIndex < m_strProperties.length
				&& m_strProperties[iIndex].length >= 1)
		{
			retval = m_strProperties[iIndex][0];
		}
		return retval;
	}

	public static int getMatchIndex(String str, String r1, String r2)
	{
		int retval = -1;
		int count = 0;

		for (int i = 0; i < str.length(); i++)
		{
			if (str.substring(i).startsWith(r1))
			{
				count++;
			}
			if (str.substring(i).startsWith(r2))
			{
				count--;
			}
			if (count == 0)
			{
				retval = i;
				break;
			}
		}

		return retval;
	}

	public String getValue(int iIndex)
	{
		String retval = null;
		if (m_strProperties != null && iIndex < m_strProperties.length
				&& m_strProperties[iIndex].length == 2)
		{
			retval = m_strProperties[iIndex][1];
		}
		return retval;
	}

	public String getValue(String strKey)
	{
		String retval = null;
		if (m_strProperties != null)
		{
			for (int i = 0; i < m_strProperties.length; i++)
			{
				if (m_strProperties[i].length == 2
						&& m_strProperties[i][0].equals(strKey))
				{
					retval = m_strProperties[i][1];
					break;
				}
			}
		}
		return retval;
	}

	final static char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String byteToHex(byte b)
	{
		// Returns hex String representation of byte b
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	static String charToHex(int c)
	{
		// Returns hex String representation of char c
		byte hi = (byte) ((c >>> 8) & 0xff);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}

	private class Itr implements Iterator
	{
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		int cursor = 0;

		/**
		 * Index of element returned by most recent call to next or previous.
		 * Reset to -1 if this element is deleted by a call to remove.
		 */
		int lastRet = -1;

		public boolean hasNext()
		{
			return m_strProperties == null ? false
					: cursor < m_strProperties.length;
		}

		public Object next()
		{
			Object retval = null;
			retval = m_strProperties == null ? null
					: m_strProperties[cursor++][1];
			return retval;
		}

		public void remove()
		{
		}
	}

	// use the ::: to repleace the ?, the :: to repleace the *

	protected static final String FILE_ELE = "[^\\\\\\\\/:\\\\?\"<>\\\\|\\\\::]";

	// (?:[:]*[/|\\]+)
	protected static final String DIR_SEP = "(?:[:]::[/|\\\\\\\\]\\+)";

	// (?:FILE_ELE::[:]::[/|\\]::):: ->(?:FILE_ELE*(?:[:]*[/|\\]+)+)*
	protected static final String DIR_EXP = "(?:" + FILE_ELE + "::" + DIR_SEP
			+ ")::";

	/**
	 * from the "**\*.*" mode.
	 * @param str
	 */
	public static String pathMatcherToRegex(String easy_str)
	{
		String regex = easy_str;
		// support ?
		regex = regex.replaceAll("\\?", ":::");

		// support ** (directory)
		regex = regex.replaceAll("[/|\\\\]*\\*\\*[/|\\\\]*", DIR_EXP);

		// support *
		regex = regex.replaceAll("\\*", "" + FILE_ELE + "::");

		// keep .
		regex = regex.replaceAll("\\.", "\\\\.");

		// convert for the ? to .
		regex = regex.replaceAll(":::", "\\.");

		// convert for the * to \*
		regex = regex.replaceAll("::", "\\*");

		return regex;
	}

	/**
	 * Common routine to rotate all the ASCII alpha chars in the given
	 * CharBuffer by 13. Note that this code explicitly compares for upper and
	 * lower case ASCII chars rather than using the methods
	 * Character.isLowerCase and Character.isUpperCase. This is because the
	 * rotate-by-13 scheme only works properly for the alphabetic characters of
	 * the ASCII charset and those methods can return true for non-ASCII Unicode
	 * chars.
	 */
	public static void rot13(CharBuffer cb)
	{
		for (int pos = cb.position(); pos < cb.limit(); pos++)
		{
			cb.put(pos, rot13(cb.get(pos)));
		}
	}

	public static char rot13(char c)
	{
		char retval = c;
		char a = '\u0000';

		// Is it lower case alpha?
		if ((c >= 'a') && (c <= 'z'))
		{
			a = 'a';
		}

		// Is it upper case alpha?
		if ((c >= 'A') && (c <= 'Z'))
		{
			a = 'A';
		}

		// If either, roll it by 13
		if (a != '\u0000')
		{
			c = (char) ((((c - a) + 13) % 26) + a);
			retval = c;
		}
		return retval;
	}

	public static void rot13(char[] cb)
	{
		for (int pos = 0; pos < cb.length; pos++)
		{
			cb[pos] = rot13(cb[pos]);
		}
	}

	// https://gist.github.com/jdcrensh/4670128
	public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static final int BASE = ALPHABET.length();

	public static String toBase62String(long i)
	{
		StringBuilder sb = new StringBuilder("");
		while (i > 0)
		{
			i = toBase62String(i, sb);
		}
		return sb.reverse().toString();
	}

	private static long toBase62String(long i, final StringBuilder sb)
	{
		long rem = i % BASE;
		sb.append(ALPHABET.charAt((int) rem));
		return i / BASE;
	}

	public static long toBase10(String str)
	{
		return toBase10(new StringBuilder(str).reverse().toString()
				.toCharArray());
	}

	private static long toBase10(char[] chars)
	{
		long n = 0;
		for (int i = chars.length - 1; i >= 0; i--)
		{
			n += toBase10(ALPHABET.indexOf(chars[i]), i);
		}
		return n;
	}

	private static long toBase10(int n, int pow)
	{
		return n * (long) Math.pow(BASE, pow);
	}

	public static String reader2String(Reader reader) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		int count = 0;
		char[] buf = new char[1024];
		while ((count = reader.read(buf)) != -1)
		{
			sb.append(buf, 0, count);
		}

		return sb.toString();
	}

	public static String toString(int[] a)
	{
		if (a == null)
			return "null";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++)
		{
			b.append(Integer.toHexString(a[i]));
			if (i == iMax)
				return b.append(']').toString();
			b.append(", ");
		}
	}

	public static String streamToString(InputStream is, String charset)
	{
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		StringBuilder sb = new StringBuilder();
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, charset));

			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				is.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
}
