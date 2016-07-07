package org.thucloud.conflictbox.service.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;

//@Scope("prototype")
public class DropboxConnection {
	private String accessToken;
	private String currentDirPath;
	private ArrayList<ArrayList<String>> lib = new ArrayList<ArrayList<String>>();

	public HashMap<String, ArrayList<String>> getResult(String code, HttpServletRequest request) throws DbxException,
			ParseException {
		
		DbxClient client = new DbxClient(new DbxRequestConfig("ConflictBox", request.getLocale().toString()), accessToken);
		lib.clear();
		return listDropboxFolders(client, currentDirPath);
	}

	public HashMap<String, ArrayList<String>> listDropboxFolders(
			DbxClient dbxClient, String folderPath) throws DbxException {
		DbxEntry.WithChildren listing = dbxClient
				.getMetadataWithChildren(folderPath);
		if (listing == null)
			return null;
//		System.out.println(listing.);
		System.out.println("get dirs/files starting");
		ArrayList<String> fileItems = new ArrayList<String>();
		ArrayList<String> dirItems = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (DbxEntry child : listing.children) {
			String name = child.name;
			if (!child.isFolder()) {
				String string = child.toString();
				System.out.println(string);
				String dropboxTime = string.substring(
						string.lastIndexOf("lastModified")).substring(14, 33);
				String lastTime = string.substring(
						string.lastIndexOf("clientMtime")).substring(13, 32);
				long delta = 0;
				try {
					delta = (sdf.parse(dropboxTime).getTime() - sdf.parse(
							lastTime).getTime()) / 1000;
				} catch (ParseException e) {
					e.printStackTrace();
				}
				fileItems.add(name + "*" + lastTime + "*" + delta + "s");
			} else {
				dirItems.add(name + "*");
			}
		}
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		map.put("files", fileItems);
		map.put("dirs", dirItems);

		return map;
	}
	
	/**
	 * @return the currentDirPath
	 */
	public String getCurrentDirPath() {
		return currentDirPath;
	}

	/**
	 * @param currentDirPath
	 *            the currentDirPath to set
	 */
	public void setCurrentDirPath(String currentDirPath) {
		this.currentDirPath = currentDirPath;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken
	 *            the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
