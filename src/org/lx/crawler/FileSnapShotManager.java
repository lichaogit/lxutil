package org.lx.crawler;

import java.io.File;
import java.io.IOException;

import org.lx.arch.UrlRule;
import org.lx.http.HttpRequest;

public class FileSnapShotManager extends AbstractSnapShotManager
{
	public FileSnapShotManager(UrlRule urlRule)
	{
		super(urlRule, "./cache/");
	}

	/**
	 * create a new SnapShot Object
	 * @param htmlUrl
	 * @param param
	 * @return
	 */
	public ISnapShot newSnapShot(Request request)
	{
		return new FileSnapShot(this, request);
	}

	public void setSnapShotLocation(String cache_dir)
	{
		super.setSnapShotLocation(cache_dir);

		File f = new File(cache_dir);
		if (!f.exists())
		{
			f.mkdirs();
		}
	}

	/**
	 * get the cache info id, that will be used in the other methods of
	 * ISnapShot.
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public ISnapShot loadCache(Request request, Object[] params)
			throws IOException
	{
		FileSnapShot dc = new FileSnapShot(this, request);
		dc.reloadSnapShotInfo();
		return dc;
	}

	/**
	 * determine whether the cache need update.
	 * @param htmlUrl
	 * @param param
	 * @return
	 */
	public boolean needUpdate(Request request) throws IOException

	{
		boolean retval = true;
		do
		{
			if (super.needUpdate(request))
			{
				break;
			}

			// TODO:
			// determine by etag.
			// if(etag!=local_etag)
			// String local_etag = cache.getProperty(ISnapShot.PROP_RSPMETA,
			// ISnapShot.K_ETAG);
			// if (true)
			// {
			// break;
			// }
			retval = false;
		} while (false);
		return retval;
	}

}
