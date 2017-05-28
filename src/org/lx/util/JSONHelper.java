package org.lx.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JSONHelper
{
	private static ObjectMapper objectMapper = new ObjectMapper();

	static
	{
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.setFilters(new SimpleFilterProvider()
				.setFailOnUnknownId(false));
		objectMapper.setSerializationInclusion(Include.NON_NULL);

	}

	/**
	 * Object2json
	 * @param object
	 * @return
	 */
	public static String toJson(Object object)
	{
		MappingJsonFactory f = new MappingJsonFactory();
		StringWriter sw = new StringWriter();
		try
		{
			 JsonGenerator generator = f.createGenerator(sw);
			 generator.writeObject(object);
			 generator.close();
			//objectMapper.writeValue(System.out, object);
		} catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
		return sw.toString();
	}

	/**
	 * json2obj
	 * @param json
	 * @param cls
	 * @return
	 */
	public static Object toObj(String json, Class<?> cls)
	{
		if (json == null)
		{
			return null;
		}
		Object obj = null;
		try
		{
			obj = objectMapper.readValue(json, cls);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * 从文件中读取json
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public final static String getJsonFromFile(String filePath)
	{
		BufferedReader reader = null;
		StringBuffer laststr = new StringBuffer();
		try
		{
			FileInputStream fileInputStream = new FileInputStream(filePath);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while ((tempString = reader.readLine()) != null)
			{
				laststr.append(tempString.trim());//
			}
			reader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return laststr.toString();
	}
}
