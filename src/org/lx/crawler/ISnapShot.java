package org.lx.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ISnapShot
{

	final static String K_CHARSET = "charset"; // The Charset of cache

	final static String K_ETAG = "Etag";

	final static String K_LAST_MODIF = "last-modified";

	final static String K_URL = "url";

	final static String PROP_REQMETA = "RequestMetaDatas";

	final static String PROP_RSPMETA = "ResponseMetaDatas";

	final static String PROP_CONTENT = "Content";

	/**
	 * return all of the properties for the category.
	 * @param category
	 * @return
	 */
	public List getProperties(String category) throws IOException;

	/**
	 * get the value the SnapShot's property.
	 * @param category
	 * @param key
	 * @return
	 */
	public String getProperty(String category, String key) throws IOException;

	/**
	 * set the value the SnapShot's property.
	 * @param category
	 * @param key
	 * @param val
	 * @return
	 */
	public void setProperty(String category, String key, String val)
			throws IOException;

	public InputStream getInputStream() throws IOException;

	/**
	 * get the SnapShot's outputStream for the specific URL.
	 * @param html_url
	 *        The Request URL.
	 * @param param
	 *        The Http request parameters.
	 * @return
	 */
	public OutputStream getOutputStream() throws IOException;

	/**
	 * remove the specific SnapShot.
	 * @return
	 */
	public void remove() throws IOException;

	/**
	 * check the SnapShot status, the damaged file will be deleted.
	 * @return
	 */
	public boolean checkIntegrity() throws IOException;

	/**
	 * get the SnapShot file's local create time.
	 * @return
	 */
	public long getSnapShotModified() throws IOException;

	/**
	 * close the SnapShot, buffer should be flush before close the the SnapShot
	 * @throws IOException
	 */
	public void close() throws IOException;

	/**
	 * Add a new SnapShot
	 * @param uri
	 * @param param
	 * @param in
	 */
	public void save(InputStream in) throws IOException;

}
