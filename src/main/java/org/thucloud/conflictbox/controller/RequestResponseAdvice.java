package org.thucloud.conflictbox.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thucloud.conflictbox.controller.utils.RequestUtil;
import org.thucloud.conflictbox.controller.utils.ResponseUtil;

/**
 * AOP类
 * @author warrior
 *
 */
public class RequestResponseAdvice implements MethodInterceptor{
	private static Log rRLogger = LogFactory.getLog("requestResponse");

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		//get request and response
		System.out.println("not used???");
		HttpServletRequest request = (HttpServletRequest) methodInvocation.getArguments()[0];
		HttpServletResponse response = (HttpServletResponse) methodInvocation.getArguments()[1];	
		try {
			String result = (String) methodInvocation.proceed();
			ResponseUtil.httpResponse(response, result);
			rRLogger.info(RequestUtil.getUserBaseInfo(request) + "\n" + "RESULT:\n" + result + "\n");
		}catch(Exception e) {
//			if(e instanceof DatabaseException || e instanceof MyNumberFormatException || e instanceof ParameterErrorException || e instanceof SessionExceedException || e instanceof NotLoginException) {
//				ResponseUtil.httpResponseException(response, e);
//				e.printStackTrace();
//				rRLogger.error(RequestUtil.getUserBaseInfo(request) + "\n" + "RESULT EXCEPTION MESSAGE:\n" + e.getMessage() + "EXCEPTION CLASS:" + e.getClass() + "\n");
//			}
//			else {
//				ResponseUtil.httpResponseException(response, OtherException.getInstance());
//				e.printStackTrace();
//				rRLogger.error(RequestUtil.getUserBaseInfo(request) + "\n" + "RESULT EXCEPTION MESSAGE:\n" + e.toString() + "EXCEPTION CLASS:" + e.getMessage() + "\n");
//			}
			e.printStackTrace();
		}	
		return null;
	}

}
