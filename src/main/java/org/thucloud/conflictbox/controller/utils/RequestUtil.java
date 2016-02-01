package org.thucloud.conflictbox.controller.utils;

import java.io.*;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

	public static String getUserBaseInfo(HttpServletRequest request) {
		StringBuffer userInfoBuffer = new StringBuffer();
		userInfoBuffer.append("USER IP:" + request.getRemoteAddr() + "\t");
		userInfoBuffer.append("HOST:" + request.getRemoteHost() + "\t");
		userInfoBuffer.append("PORT:" + request.getRemotePort() + "\t");
		userInfoBuffer.append("REQUEST URL:" + request.getRequestURL() + "\t");
		userInfoBuffer
				.append("QUERY STRING:" + request.getQueryString() + "\t");
		userInfoBuffer.append("METHOD:" + request.getMethod() + "\t");
		return userInfoBuffer.toString();
	}

	/*
	 * 获取前端表单提交的json信息
	 * */
	public static String getJsonString(HttpServletRequest request) {
		StringBuffer json = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return json.toString();
	}
}
