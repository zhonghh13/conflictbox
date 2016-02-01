package org.thucloud.conflictbox.controller;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thucloud.conflictbox.model.MyDirectory;
import org.thucloud.conflictbox.service.DataService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping("/data")
@Scope("prototype")
public class DataController {
	private DataService dataService;
	
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

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces="text/html;utf-8")
	@ResponseBody
	public String saveData(HttpServletRequest request, HttpServletResponse response){
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
//		System.out.println(msg);
		MyDirectory directory = gson.fromJson(msg, MyDirectory.class);
		
		String username = directory.getUsername();
		String password = directory.getPassword();
		ArrayList<String> dirsList = directory.getDirsList();
		ArrayList<String> filesList = directory.getFilesList();
		String dirsName = directory.getDirsName();
		String updateTime = directory.getUpdateTime();
		int layer = directory.getLayer();
		String fatherPath = directory.getFatherPath();
		
		dirsList = dataService.newCompare(dirsName, dirsList, filesList, username, password, layer, fatherPath);

		return "true";
	}
	
	@RequestMapping(value="/getdata", method=RequestMethod.POST, produces="text/html;charset=utf-8")
	@ResponseBody
	public String getData(HttpServletRequest request, HttpServletResponse response) {
		System.out.println(System.currentTimeMillis());
		String email =  request.getParameter("user");
		String password = request.getParameter("password");
		System.out.println(password);
		String dirName = request.getParameter("dirName");
		String father = request.getParameter("father");
		String seconds = request.getParameter("seconds");
		System.out.println(seconds);
		String result = dataService.getData(email, seconds, password, dirName, father, request);
		return result;
	}
}
