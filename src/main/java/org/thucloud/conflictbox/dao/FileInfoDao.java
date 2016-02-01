package org.thucloud.conflictbox.dao;

import java.util.ArrayList;
import java.util.Map;

public interface FileInfoDao {
	Map<String, String> getData(int userId, String dirName, String father);
	void save(int id, String dirsName, ArrayList<String> dirResult, ArrayList<String> fileResult, String fatherPath, int layer);
}
