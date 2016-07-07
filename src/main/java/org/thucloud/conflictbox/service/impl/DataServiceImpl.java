package org.thucloud.conflictbox.service.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;

import org.thucloud.conflictbox.controller.utils.ResponseUtil;
import org.thucloud.conflictbox.dao.FileInfoDao;
import org.thucloud.conflictbox.dao.SharedFoldersDao;
import org.thucloud.conflictbox.dao.UserDao;
import org.thucloud.conflictbox.model.SharedFolderResponse;
import org.thucloud.conflictbox.model.SharedFoldersJSON;
import org.thucloud.conflictbox.model.User;
import org.thucloud.conflictbox.service.DataService;
import org.thucloud.conflictbox.service.utils.DropboxConnection;

import com.dropbox.core.DbxException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataServiceImpl implements DataService {
	private DropboxConnection dropboxConnection;
	private ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
	private UserDao userDao;
	private FileInfoDao fileInfoDao;
	private SharedFoldersDao sharedFoldersDao;
	
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
		String token = null;
		if (user != null){
			id = (Integer) user.get("id");
			token = (String)user.get("accessToken");
		}else {
			id = userDao.save(username, password);
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("user", id);
		if (token != null){
			resultMap.put("accessToken", token);
		}
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
		// 获取user信息
		Map<String, Object> info = userDao.getUser(email, password);
		int userId = (Integer)info.get("id");
		String accessToken = (String)info.get("accessToken");
		//获取文件信息
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

		// 获取Dropbox服务器上的数据信息
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
		
		//比较文件夹信息和文件信息，返回比较结果
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
		System.out.println(resultMap);
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

	public Map<String, Object> listFolders(String auth) throws Exception {
		String httpsUrl = "https://api.dropboxapi.com/2/sharing/list_folders";
		URL url = new URL(httpsUrl);
		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection(new Proxy(Type.HTTP, new InetSocketAddress("166.111.80.96", 4128)));
		conn.setRequestMethod("POST");
		
		System.out.println(auth);
		
		conn.setRequestProperty("Authorization", "Bearer "+auth);
		conn.addRequestProperty("Content-Type", "application/json");
		
		System.out.println(conn.getRequestProperties());
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		ArrayList<String> actions = new ArrayList<String>();
		paramsMap.put("limit", 100);
		paramsMap.put("actions", actions);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String params = gson.toJson(paramsMap);
		
//		String params = ResponseUtil.wrapNormalReturn(paramsMap);
		System.out.println(params);
		DataOutputStream output = new DataOutputStream(conn.getOutputStream());  
		output.writeBytes(params);
		output.close();
		
		InputStream inputStream = conn.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(reader);
		
		StringBuffer jsonStr = new StringBuffer();
		String line = null;
		
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			jsonStr.append(line);
		}
		
		br.close();
		
		Gson json = new GsonBuilder().setPrettyPrinting() .disableHtmlEscaping().create();
		System.out.println(jsonStr);
		SharedFoldersJSON shareFolders = json.fromJson(jsonStr.toString(), SharedFoldersJSON.class);
		
		ArrayList<Map<String, Object>> entries = shareFolders.getEntries();
		
//		String[] folderIds = new String[entries.size()];
//		for (int i = 0; i < entries.size(); i++){
//			Map<String, Object> map = entries.get(i);
//			folderIds[i] = (String)map.get("shared_folder_id");
//		}
		boolean flag = sharedFoldersDao.save(entries, auth);
		
		List<Map<String, Object>> revList = sharedFoldersDao.getRevsByAuthcode(auth);
		HashMap<String, Integer> revMap = new HashMap<String, Integer>();
		
		for (int i = 0; i < revList.size(); i++){
			revMap.put(revList.get(i).get("folderid").toString(), (Integer)revList.get(i).get("rev"));
		}
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if (flag) {
			resultMap.put("cursor", shareFolders.getCursor());
			List<SharedFolderResponse> list = new ArrayList<SharedFolderResponse>();
			for (int i = 0; i < entries.size(); i++){
				SharedFolderResponse item = new SharedFolderResponse();
				item.setId((String)entries.get(i).get("shared_folder_id"));
				item.setName((String)entries.get(i).get("name"));
				item.setTeamFolder((Boolean)entries.get(i).get("is_team_folder"));
				item.setPath((String)entries.get(i).get("path_lower"));
				item.setAccessType((String)((Map<String, Object>)entries.get(i).get("access_type")).get(".tag"));
				item.setRev(revMap.get(item.getId()));
				list.add(item);
			}
			resultMap.put("entries", list);
		}
		System.out.println(jsonStr);
		
		return resultMap;
	}

	public SharedFoldersDao getSharedFoldersDao() {
		return sharedFoldersDao;
	}

	public void setSharedFoldersDao(SharedFoldersDao sharedFoldersDao) {
		this.sharedFoldersDao = sharedFoldersDao;
	}

}
