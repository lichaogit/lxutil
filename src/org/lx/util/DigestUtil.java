package org.lx.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil
{
	MessageDigest m_md5Inst;

	public static DigestUtil getInstance()
	{
		return new DigestUtil();
	}

	protected DigestUtil()
	{
		try
		{
			m_md5Inst = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

	}

	public byte[] md5ToBytes(byte[] bytes)
	{
		return m_md5Inst.digest(bytes);
	}
	
	public String md5(byte[] bytes)
	{
		String tmp = null;
		byte[] digest = m_md5Inst.digest(bytes);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < digest.length; i++)
		{
			tmp = Integer.toHexString((0xFF & digest[i]));
			if (tmp.length() < 2)
			{
				sb.append('0');
			}
			sb.append(tmp);
		}
		return sb.toString();
	}
}
