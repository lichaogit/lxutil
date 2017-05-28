package org.lx.arch.expr;

import java.io.UnsupportedEncodingException;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class Adler32Function implements IExprFunction
{

	public Object handle(Object[] params) throws ExprException
	{
		byte[] content;
		try
		{
			content = params[0].toString().getBytes("UTF-8");
			Checksum checksumEngine = new Adler32();
			checksumEngine.update(content, 0, content.length);
			return Long.valueOf(checksumEngine.getValue());
		} catch (UnsupportedEncodingException e)
		{
			throw new ExprException(e.getLocalizedMessage(), e);
		}
	}
}
