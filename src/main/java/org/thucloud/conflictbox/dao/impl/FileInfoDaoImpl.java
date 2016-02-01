package org.thucloud.conflictbox.dao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.thucloud.conflictbox.dao.FileInfoDao;

public class FileInfoDaoImpl implements FileInfoDao {
	
	private SqlSession sqlSession;
	public Map<String, String> getData(int userId, String dirName, String father) {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("userId", userId);
		parameters.put("dirsName", dirName);
		parameters.put("father", father);
		return sqlSession.selectOne("org.thucloud.conflictbox.dao.FileInfoDao.getData", parameters);
	}

	public void save(int userId, String dirsName,
			ArrayList<String> dirResult, ArrayList<String> fileResult,
			String fatherPath, int layer) {
		
		String dirstring="";
		for (int i = 0; i < dirResult.size(); i++){
			dirstring += dirResult.get(i)+"@\t@";
		}
		String filestring = "";
		for (int i = 0; i < fileResult.size(); i++){
			filestring += fileResult.get(i)+"@\t@";
		}
		
		String time = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
				.format(new Date(System.currentTimeMillis()));
		
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dirsName", dirsName);
		parameter.put("dirsText", dirstring);
		parameter.put("filesText", filestring);
		parameter.put("layer", layer);
		parameter.put("father", fatherPath);
		parameter.put("updateTime", time);
		parameter.put("userId", userId);
		
		int flag =sqlSession.insert("org.thucloud.conflictbox.dao.FileInfoDao.saveResult", parameter);
	}

	/**
	 * @return the sqlSession
	 */
	public SqlSession getSqlSession() {
		return sqlSession;
	}

	/**
	 * @param sqlSession the sqlSession to set
	 */
	public void setSqlSession(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}

}
