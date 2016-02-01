package org.thucloud.conflictbox.model;

import java.util.ArrayList;

public class MyDirectory {
	private String username;
	private String password;
	private ArrayList<String> dirsList;
	private ArrayList<String> filesList;
	private String dirsName;
	private String fatherPath;
	private int layer;
	private String updateTime;
//	private boolean isChecked;
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the dirsList
	 */
	public ArrayList<String> getDirsList() {
		return dirsList;
	}
	/**
	 * @param dirsList the dirsList to set
	 */
	public void setDirsList(ArrayList<String> dirsList) {
		this.dirsList = dirsList;
	}
	/**
	 * @return the filesList
	 */
	public ArrayList<String> getFilesList() {
		return filesList;
	}
	/**
	 * @param filesList the filesList to set
	 */
	public void setFilesList(ArrayList<String> filesList) {
		this.filesList = filesList;
	}
	/**
	 * @return the dirsName
	 */
	public String getDirsName() {
		return dirsName;
	}
	/**
	 * @param dirsName the dirsName to set
	 */
	public void setDirsName(String dirsName) {
		this.dirsName = dirsName;
	}
	/**
	 * @return the fatherPath
	 */
	public String getFatherPath() {
		return fatherPath;
	}
	/**
	 * @param fatherPath the fatherPath to set
	 */
	public void setFatherPath(String fatherPath) {
		this.fatherPath = fatherPath;
	}
	/**
	 * @return the layer
	 */
	public int getLayer() {
		return layer;
	}
	/**
	 * @param layer the layer to set
	 */
	public void setLayer(int layer) {
		this.layer = layer;
	}
	/**
	 * @return the updateTime
	 */
	public String getUpdateTime() {
		return updateTime;
	}
	/**
	 * @param updateTime the updateTime to set
	 */
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

}
