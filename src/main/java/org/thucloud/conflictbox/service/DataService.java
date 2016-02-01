package org.thucloud.conflictbox.service;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface DataService {
	public Map<String, Object> isUserExist(String username, String password);
	
	public void setAccessToken(String accessToken, String username);

	public String getData(String email, String seconds, String password, String dirName, String father, HttpServletRequest request);

	public ArrayList<String> newCompare(String dirsName,
			ArrayList<String> dirsList, ArrayList<String> filesList, String username, String password, int layer, String fatherPath);

	public Map<String, Object> setUser(String username, String password);
}
