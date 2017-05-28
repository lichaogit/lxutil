package org.lx.arch;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FastJsonExchange implements StreamExchange
{
	ObjectMapper mapper = new ObjectMapper();

	public Object fromString(String express, Class clazz)
	{
		Object retval = null;
		JsonParser jp;
		try
		{
			retval = mapper.readValue(express, clazz);

		} catch (JsonParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	public String toString(Object obj, Map convertIinfo)
	{
		String retval = null;
		try
		{
			retval = mapper.writeValueAsString(obj);
		} catch (JsonGenerationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

}
