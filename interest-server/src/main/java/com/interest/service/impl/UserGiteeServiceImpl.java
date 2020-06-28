package com.interest.service.impl;

import com.interest.dao.UserGiteeDao;
import com.interest.model.entity.UserGeeEntity;
import com.interest.service.UserGiteeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserGiteeServiceImpl implements UserGiteeService {

	@Resource
	private UserGiteeDao userGiteeDao;
	@Override
	public void insertEntity(UserGeeEntity userGeeEntity) {
		userGiteeDao.insertEntity(userGeeEntity);
	}
}
