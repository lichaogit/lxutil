package org.lx.crawler;

import java.io.IOException;

public interface ISnapShotManager {
	/**
	 * create a new SnapShot Object
	 * 
	 * @param request
	 * @return
	 */
	public ISnapShot newSnapShot(Request request);

	/**
	 * build propperty for a SnapShot Object
	 * 
	 * @param snapshot
	 * @param propName
	 * @return
	 */
	public ISnapShot buildSnapShotProperty(ISnapShot snapshot, String propName)
			throws IOException;

	/**
	 * get the SnapShot info.
	 * 
	 * @param request
	 * @param versionExpress
	 * @return
	 */
	public ISnapShot getSnapShot(Request request, String versionExpress)
			throws IOException;

	/**
	 * determine whether the SnapShot need update.
	 * 
	 * @param htmlUrl
	 * @param param
	 * @return
	 */
	public boolean needUpdate(Request request) throws IOException;

	/**
	 * * set the SnapShot location
	 * 
	 * @param loc
	 */
	public void setSnapShotLocation(String loc);

	/**
	 * get the SnapShot locaction.
	 * 
	 * @return
	 */
	public String getSnapShotLocation();

}
