package org.thucloud.conflictbox.dao;

import java.util.Map;

public interface UserDao {
	int save(String username, String password);
	boolean update(String username, String accessToken);
	Map<String, Object> getUser(String username, String password);
}
