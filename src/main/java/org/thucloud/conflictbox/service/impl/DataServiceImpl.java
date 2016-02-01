package org.thucloud.conflictbox.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.thucloud.conflictbox.controller.utils.ResponseUtil;
import org.thucloud.conflictbox.dao.FileInfoDao;
import org.thucloud.conflictbox.dao.UserDao;
import org.thucloud.conflictbox.service.DataService;
import org.thucloud.conflictbox.service.utils.DropboxConnection;

import com.dropbox.core.DbxException;

public class DataServiceImpl implements DataService {
	private DropboxConnection dropboxConnection;
	private ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
	private UserDao userDao;
	private FileInfoDao fileInfoDao;
	
	/**
	 * @return the dropboxConnection
	 */
	public DropboxConnection getDropboxConnection() {
		return dropboxConnection;
	}

	/**
	 * @param dropboxConnection
	 *            the dropboxConnection to set
	 */
	public void setDropboxConnection(DropboxConnection dropboxConnection) {
		this.dropboxConnection = dropboxConnection;
	}

	public Map<String, Object> isUserExist(String username, String password) {
		Map<String, Object> info = userDao.getUser(username, password);
		return info;
	}
	
	/**
	 * @return the resultList
	 */
	public ArrayList<ArrayList<String>> getResultList() {
		return resultList;
	}

	/**
	 * @param resultList
	 *            the resultList to set
	 */
	public void setResultList(ArrayList<ArrayList<String>> resultList) {
		this.resultList = resultList;
	}

	public ArrayList<String> newCompare(String dirsName,
			ArrayList<String> dirsList, ArrayList<String> filesList,
			String username, String password, int layer, String fatherPath) {
		
		Map<String, Object> user = userDao.getUser(username, password);
		int id = -1;
		if (user != null){
			id = (Integer) user.get("id");
		}else {
			id = userDao.save(username, password);
		}
		System.out.println(id);
		
		fileInfoDao.save(id, dirsName, dirsList, filesList, fatherPath, layer);
		return null;
	}

	public Map<String, Object> setUser(String username, String password) {
		Map<String, Object> user = userDao.getUser(username, password);
		int id = -1;
		if (user != null){
			id = (Integer) user.get("id");
		}else {
			id = userDao.save(username, password);
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("user", id);
		return resultMap;
	}
	
	private ArrayList<String> fileCompare(ArrayList<String> filesList,
			ArrayList<String> dropboxList) {
		ArrayList<String> resultList = new ArrayList<String>();
		boolean[] filesFlag = new boolean[filesList.size()];
		for (int i = 0; i < filesList.size(); i++) {
			filesFlag[i] = false;
		}
		boolean[] dropboxFlag = new boolean[dropboxList.size()];
		for (int j = 0; j < dropboxList.size(); j++) {
			dropboxFlag[j] = false;
		}

		for (int i = 0; i < filesList.size(); i++) {
			String filesItem = filesList.get(i);
			int j;
			for (j = 0; j < dropboxList.size(); j++) {
				if (!dropboxFlag[j]) {
					String dropboxItem = dropboxList.get(j);
					if (dropboxItem.substring(0, dropboxItem.indexOf("*"))
							.equalsIgnoreCase(
									filesItem.substring(0,
											filesItem.indexOf("*")))) {
						filesFlag[i] = true;
						dropboxFlag[j] = true;
						if (filesItem.equalsIgnoreCase(dropboxItem.substring(0,
								dropboxItem.lastIndexOf("*")))) {
							resultList.add(filesItem
									+ dropboxItem.substring(dropboxItem
											.indexOf("*")) + "*correct");
						} else {
							resultList.add(dropboxItem.substring(0,
									dropboxItem.lastIndexOf("*"))
									+ filesItem.substring(filesItem
											.lastIndexOf("*")) + "* *wrong");
						}
						break;
					}
				}
			}
		}

		for (int i = 0; i < filesList.size(); i++) {
			if (!filesFlag[i]) {
				String fileItem = filesList.get(i);
				resultList.add(fileItem.replace("*", "*lost*") + "* *wrong");
			}
		}

		for (int j = 0; j < dropboxList.size(); j++) {
			if (!dropboxFlag[j]) {
				String dropboxItem = dropboxList.get(j);
				resultList.add(dropboxItem.substring(0,
						dropboxItem.lastIndexOf("*"))
						+ "*lost* *wrong");
			}
		}

		return resultList;
	}

	private ArrayList<String> dirCompare(ArrayList<String> dirsList,
			ArrayList<String> dropboxList) {
		ArrayList<String> resultList = new ArrayList<String>();
		boolean[] dirsFlag = new boolean[dirsList.size()];
		for (int i = 0; i < dirsList.size(); i++) {
			dirsFlag[i] = false;
		}
		boolean[] dropboxFlag = new boolean[dropboxList.size()];
		for (int j = 0; j < dropboxList.size(); j++) {
			dropboxFlag[j] = false;
		}
		for (int i = 0; i < dirsList.size(); i++) {
			String dirsItem = dirsList.get(i);
			for (int j = 0; j < dropboxList.size(); j++) {
				if (!dropboxFlag[j]) {
					String dropboxItem = dropboxList.get(j);
					if (dropboxItem.equalsIgnoreCase(dirsItem)) {
						resultList.add(dropboxItem + " * * *correct");
						dirsFlag[i] = true;
						dropboxFlag[j] = true;
					}
				}
			}
		}
		for (int i = 0; i < dirsList.size(); i++) {
			if (!dirsFlag[i]) {
				resultList.add(dirsList.get(i) + "lost* * *wrong");
			}
		}

		for (int i = 0; i < dropboxList.size(); i++) {
			if (!dropboxFlag[i]) {
				resultList.add(dropboxList.get(i) + " *lost* *wrong");
			}
		}
		return resultList;
	}
	
	public String getData(String email, String seconds, String password,
			String dirName, String father, HttpServletRequest request) {
		System.out.println(email);
		Map<String, Object> info = userDao.getUser(email, password);
		int userId = (Integer)info.get("id");
		String accessToken = (String)info.get("accessToken");
		Map<String, String> map = fileInfoDao.getData(userId, dirName, father);

		ArrayList<String> dirsList = new ArrayList<String>();
		ArrayList<String> dropboxDirs = new ArrayList<String>();
		ArrayList<String> dropboxFiles = new ArrayList<String>();
		ArrayList<String> filesList = new ArrayList<String>();
		ArrayList<String> dirResult = new ArrayList<String>();
		ArrayList<String> fileResult = new ArrayList<String>();
		String time = new String();
		if (map != null) {
			if (map.get("dirsText") != null && map.get("dirsText").length() > 0) {
				System.out.println(map.get("dirsText").toString());
				String[] dirstemp = map.get("dirsText").split("@\t@");
				for (int i = 0; i < dirstemp.length; i++) {
//					System.out.println("@");
					dirsList.add(dirstemp[i]);
				}
			}

			if (map.get("filesText") != null
					&& map.get("filesText").length() > 0) {
				String[] filesTemp = map.get("filesText").split("@\t@");
				System.out.println("files number:"+filesTemp.length);
				for (int i = 0; i < filesTemp.length; i++) {
//					System.out.println("@");
					filesList.add(filesTemp[i]);
				}
			}
		}

		dropboxConnection.setAccessToken(accessToken);
		System.out.println("dafea"+accessToken);
		
		dropboxConnection.setCurrentDirPath(dirName);
		HashMap<String, ArrayList<String>> dropboxResultMap = new HashMap<String, ArrayList<String>>();

		try {
			dropboxResultMap = dropboxConnection
					.getResult(accessToken, request);
		} catch (DbxException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (dropboxResultMap != null) {
			if (dropboxResultMap.get("dirs") != null) {
				dropboxDirs = dropboxResultMap.get("dirs");
			}

			if (dropboxResultMap.get("files") != null) {
				dropboxFiles = dropboxResultMap.get("files");
			}
		}
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if (map == null && dropboxResultMap == null){
			resultMap.put("error", "文件信息不存在");
			ResponseUtil.wrapException(resultMap);
		}

		dirResult = dirCompare(dirsList, dropboxDirs);
		fileResult = fileCompare(filesList, dropboxFiles);
		
		time = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
				.format(new Date(System.currentTimeMillis()));
		
		resultMap.put("dirs", dirResult);
		resultMap.put("files", fileResult);
		resultMap.put("dirName", dirName);
		resultMap.put("time", time);
		resultMap.put("seconds", seconds);
		resultMap.put("father", father);
		return ResponseUtil.wrapNormalReturn(resultMap);
	}

	/**
	 * @return the userDao
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	/**
	 * @param userDao the userDao to set
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * @return the fileInfoDao
	 */
	public FileInfoDao getFileInfoDao() {
		return fileInfoDao;
	}

	/**
	 * @param fileInfoDao the fileInfoDao to set
	 */
	public void setFileInfoDao(FileInfoDao fileInfoDao) {
		this.fileInfoDao = fileInfoDao;
	}

	public void setAccessToken(String accessToken, String username) {
		userDao.update(username, accessToken);
	}

}
