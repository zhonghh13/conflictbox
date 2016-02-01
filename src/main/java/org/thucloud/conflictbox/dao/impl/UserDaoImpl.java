package org.thucloud.conflictbox.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.util.DigestUtils;
import org.thucloud.conflictbox.dao.UserDao;

public class UserDaoImpl implements UserDao {
	
	private SqlSession sqlSession;

	public int save(String username, String password) {
		String code = DigestUtils.md5DigestAsHex(password.getBytes());
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("email", username);
		parameter.put("password", code);
		int id = sqlSession.insert("org.thucloud.conflictbox.dao.UserDao.insertUser", parameter);
		return id;
	}

	public boolean update(String username, String accessToken) {
		boolean flag = false;
//		String code = DigestUtils.md5DigestAsHex(password.getBytes());
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("email", username);
		parameter.put("accessToken", accessToken);
//		parameter.put("password", code);
		int id = sqlSession.update("org.thucloud.conflictbox.dao.UserDao.updateToken", parameter);
		if (id >= 0) flag = true;
		return flag;
	}

	public Map<String, Object> getUser(String username, String password) {
		String code = DigestUtils.md5DigestAsHex(password.getBytes());
		System.out.println(password);
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("email", username);
		parameter.put("password", code);
		return sqlSession.selectOne("org.thucloud.conflictbox.dao.UserDao.getUser", parameter);
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
