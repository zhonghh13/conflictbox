package org.thucloud.conflictbox.controller.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 返回json给前端
 * @author warrior
 *
 */
public class ResponseUtil {
	
	/**
	 * 正常的返回给前端
	 * @param response
	 * @param res
	 */
	public static void httpResponse(HttpServletResponse response, Object res) {
		try {
			response.addHeader("Content-type", "text/html;charset=UTF-8");
//			response.addHeader("Access-Control-Allow-Origin", "*");
//			response.addHeader("Transfer-Encoding", "chunked");
			response.setBufferSize(65536);
			System.out.println(response.getBufferSize());

			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(res.toString());
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发生异常时返回给前端
	 * @param response
	 * @param exception
	 */
	public static void httpResponseException(HttpServletResponse response, Exception exception) {
		try{
			response.addHeader("Content-type", "text/html;charset=UTF-8");
//			response.addHeader("Access-Control-Allow-Origin", "*");
//			response.addHeader("Transfer-Encoding", "chunked");
			response.setBufferSize(65536);
			System.out.println(response.getBufferSize());

			response.setCharacterEncoding("UTF-8");
			HashMap<String, Object>	responseJsonMap = new HashMap<String, Object>();
			responseJsonMap.put("status", -1);
			responseJsonMap.put("errMessage", exception.getMessage());
			Gson gson = new Gson();
			String jsonString = gson.toJson(responseJsonMap);
			response.getWriter().write(jsonString);
			response.getWriter().flush();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 对normal return map进行封装
	 * @param normalRetMap
	 * @return
	 */
	public static String wrapNormalReturn(HashMap<String, Object> normalRetMap) {
		LinkedHashMap<String, Object> responseJsonMap = new LinkedHashMap<String, Object>();
		responseJsonMap.put("status", 0);
		responseJsonMap.put("normalReturn", normalRetMap);
		
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jsonString = gson.toJson(responseJsonMap);
		return jsonString;
	}
	
	/**
	 * 对normal return map进行封装
	 * @param normalRetMap
	 * @return
	 */
	public static String wrapNormalReturn(Object normalRetObj) {
		LinkedHashMap<String, Object> responseJsonMap = new LinkedHashMap<String, Object>();
		responseJsonMap.put("status", 0);
		responseJsonMap.put("normalReturn", normalRetObj);
		
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jsonString = gson.toJson(responseJsonMap);
		return jsonString;
	}
	
	
	public static String wrapException(HashMap<String, Object> normalRetMap){
		LinkedHashMap<String, Object> responseJsonMap = new LinkedHashMap<String, Object>();
		responseJsonMap.put("status", -1);
		responseJsonMap.put("error", normalRetMap);
		
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jsonString = gson.toJson(responseJsonMap);
		return jsonString;
	}
}
