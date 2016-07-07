package org.thucloud.conflictbox.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thucloud.conflictbox.controller.utils.ResponseUtil;
import org.thucloud.conflictbox.model.MyDirectory;
import org.thucloud.conflictbox.model.User;
import org.thucloud.conflictbox.service.DataService;
import org.thucloud.conflictbox.service.impl.DropboxAuth;

import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxWebAuth.BadStateException;
import com.dropbox.core.DbxWebAuth.CsrfException;
import com.dropbox.core.DbxWebAuth.NotApprovedException;
import com.dropbox.core.DbxWebAuth.ProviderException;
import com.dropbox.core.util.LangUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
//@RequestMapping("/user")
public class UserController {
	
	private DataService dataService;
	
	private String APP_KEY;
	private String APP_SECRET;
	private DropboxAuth dropboxAuth;

	@RequestMapping(value="/login", method = RequestMethod.POST)
	@ResponseBody
	public String login(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String username = request.getParameter("user");
		String password = request.getParameter("password");
		System.out.println(password);
		Map<String, Object> isExist = dataService.isUserExist(username, password);
		HashMap<String , Object> map = new HashMap<String, Object>();
		map.put("isExist", false);
		map.put("user", null);
		if (isExist != null && isExist.get("accessToken") != null){
			System.out.println(isExist.get("accessToken"));
			map.put("accessToken", isExist.get("accessToken"));
			map.put("isExist", true);
		}
		if (isExist != null){
			map.put("user", username);
			request.getSession(true).setAttribute("logged-in-username", username);
		}		
		System.out.println("isExist "+ isExist);
//		response.setHeader("set-cookie", (String)isExist.get("accessToken"));
		return ResponseUtil.wrapNormalReturn(map);
	}
	
	@RequestMapping(value="/list_shared_folders", method = RequestMethod.POST)
	@ResponseBody
	public String listFolders(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String header = request.getHeader("Authorization");
		String auth = header.substring("Bearer ".length());
		
		Map<String, Object> result = dataService.listFolders(auth);
		
		Gson json = new GsonBuilder().disableHtmlEscaping().create();
		String jsonString = json.toJson(result);
		System.out.println(jsonString);
		return jsonString;
	}
	
	
	@RequestMapping(value="/setUser", method = RequestMethod.POST)
	@ResponseBody
	public String setUser(HttpServletRequest request, HttpServletResponse response) throws Exception{
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		  StringBuffer jb = new StringBuffer();
		  String line = null;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }
		String msg = jb.toString();
		//将参数转化为Directory对象
		Gson gson = new GsonBuilder().setPrettyPrinting() .disableHtmlEscaping() .create();
		System.out.println(msg);
		User user = gson.fromJson(msg, User.class);
		
		String username = user.getUsername();
		String password = user.getPassword();
		System.out.println(password);
		Map<String, Object> userResult = dataService.setUser(username, password);
		
		Gson json = new GsonBuilder().disableHtmlEscaping().create();
		String jsonString = json.toJson(userResult);
//		return ResponseUtil.wrapNormalReturn(userResult);
		return jsonString;
	}
	
	@RequestMapping(value="/dropbox-auth-start", method=RequestMethod.POST)
	@ResponseBody
	public String doStart(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String redirectUrl = "";
		try {
			URL requestUrl = new URL(request.getRequestURL().toString());
			redirectUrl = new URL(requestUrl, "/conflictbox/dropbox-auth-finish").toExternalForm();
		} catch (MalformedURLException e) {
			throw LangUtil.mkAssert("Bad URL", e);
		}
		
		HttpSession session = request.getSession(true);
		String sessionKey = "dropbox-auth-csrf-token";
		DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, sessionKey);
		String authorizeUrl = (new DbxWebAuth(new DbxRequestConfig("ConflictBox", request.getLocale().toString()), dropboxAuth.dbxAppInfo, redirectUrl, csrfTokenStore)).start();
		response.setHeader("Access-Control-Allow-Origin", "*");
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("url", authorizeUrl);
		System.out.println(authorizeUrl);
		return ResponseUtil.wrapNormalReturn(map);
	}
	
	@RequestMapping(value="/dropbox-auth-finish")
	@ResponseBody
	public void doFinish(HttpServletRequest request, HttpServletResponse response) throws IOException{
		DbxAuthFinish authFinish;
		String redirectUrl = "";
		try {			
			try {
				URL requestUrl = new URL(request.getRequestURL().toString());
				redirectUrl = new URL(requestUrl, "/conflictbox/dropbox-auth-finish").toExternalForm();
			} catch (MalformedURLException e) {
				throw LangUtil.mkAssert("Bad URL", e);
			}
			
			HttpSession session = request.getSession(true);
			String sessionKey = "dropbox-auth-csrf-token";
			DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, sessionKey);
			authFinish = (new DbxWebAuth(new DbxRequestConfig("ConflictBox", request.getLocale().toString()), dropboxAuth.dbxAppInfo, redirectUrl, csrfTokenStore)).finish(request.getParameterMap());
		} catch (DbxWebAuth.BadRequestException e) {
			System.out.println("On /dropbox-auth-finish: Bad request: " + e.getMessage());
			response.sendError(400);
			return;
		} catch (BadStateException e) {
			response.sendRedirect(redirectUrl);
			e.printStackTrace();
			return;
		} catch (CsrfException e) {
			System.out.println("On /dropbox-auth-finish: CSRF mismatch: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (NotApprovedException e) {
			System.out.println("Not approved?");
			e.printStackTrace();
			return;
		} catch (ProviderException e) {
			System.out.println("on /dropbox-auth-finish: Auth failed: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (DbxException e) {
			System.out.println("On /dropbox-auth-finish: Error getting token: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		String dropboxAccessToken = authFinish.accessToken;
		String username = (String) request.getSession().getAttribute("logged-in-username");
		System.out.println(username);
		dataService.setAccessToken(dropboxAccessToken, username);
		response.sendRedirect("/conflictbox/show.html");
	}
	
	/**
	 * @return the dataService
	 */
	public DataService getDataService() {
		return dataService;
	}

	/**
	 * @param dataService the dataService to set
	 */
	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	/**
	 * @return the aPP_KEY
	 */
	public String getAPP_KEY() {
		return APP_KEY;
	}

	/**
	 * @param aPP_KEY the aPP_KEY to set
	 */
	public void setAPP_KEY(String aPP_KEY) {
		APP_KEY = aPP_KEY;
	}

	/**
	 * @return the aPP_SECRET
	 */
	public String getAPP_SECRET() {
		return APP_SECRET;
	}

	/**
	 * @param aPP_SECRET the aPP_SECRET to set
	 */
	public void setAPP_SECRET(String aPP_SECRET) {
		APP_SECRET = aPP_SECRET;
	}

	/**
	 * @return the dropboxAuth
	 */
	public DropboxAuth getDropboxAuth() {
		return dropboxAuth;
	}

	/**
	 * @param dropboxAuth the dropboxAuth to set
	 */
	public void setDropboxAuth(DropboxAuth dropboxAuth) {
		this.dropboxAuth = dropboxAuth;
	}
}
