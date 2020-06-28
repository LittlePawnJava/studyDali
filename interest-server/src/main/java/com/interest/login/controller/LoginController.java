package com.interest.login.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interest.annotation.InterestLog;
import com.interest.login.authentication.GiteeAuthentication;
import com.interest.login.authentication.MyAuthentication;
import com.interest.login.authentication.MyAuthenticationToken;
import com.interest.login.handler.LoginSuccessHandler;
import com.interest.properties.GiteeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

	@Autowired
	private LoginSuccessHandler loginSuccessHandler;

	@Resource
	private MyAuthentication gitHubAuthentication;

	@Resource
	private GiteeAuthentication giteeAuthentication;

	@Resource
	private MyAuthentication qQAuthentication;

	@InterestLog
	@PostMapping("/authentication/github")
	public OAuth2AccessToken loginForGithHub(HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code)
			throws IOException {

		return login(request,response,code,gitHubAuthentication);
	}

	@InterestLog
	@PostMapping("/authentication/qq")
	public OAuth2AccessToken loginForQQ(HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code)
			throws IOException {

		return login(request,response,code,qQAuthentication);
	}

	@InterestLog
	@PostMapping("/authentication/gitee")
	public OAuth2AccessToken loginGitee(HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code)
			throws IOException {

		return login(request,response,code,giteeAuthentication);
	}

	public OAuth2AccessToken login(HttpServletRequest request, HttpServletResponse response,String code,MyAuthentication myAuthentication) throws IOException {
		if (code == null) {
			code = "";
		}

		code = code.trim();

		String id = myAuthentication.getUserId(code);

		MyAuthenticationToken authRequest = new MyAuthenticationToken(id);

		authRequest.setDetails(new OAuth2AuthenticationDetails(request));

		return loginSuccessHandler.getAccessToken(request, response, authRequest);
	}

}
