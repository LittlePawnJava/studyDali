package com.interest.login.authentication;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONConverter;
import cn.hutool.json.JSONUtil;
import com.interest.dao.UserDao;
import com.interest.dao.UserDetailDao;
import com.interest.login.exception.LoginFailureExcepiton;
import com.interest.model.entity.UserDetailEntity;
import com.interest.model.entity.UserEntity;
import com.interest.model.entity.UserGeeEntity;
import com.interest.model.entity.UserQQEntity;
import com.interest.picture.PictureService;
import com.interest.properties.GiteeProperties;
import com.interest.service.UserGiteeService;
import com.interest.service.UserService;
import com.interest.utils.DateUtil;
import io.netty.util.internal.StringUtil;
import jdk.nashorn.internal.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Service
public class GiteeAuthentication implements MyAuthentication {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private UserDao userDao;

    @Resource
    private UserDetailDao userDetailDao;

    @Resource
    private UserGiteeService userGiteeService;

    @Autowired
    private GiteeProperties giteeProperties;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();

    private static final String QQ_ACCESSS_TOKEN_URL =
            "https://gitee.com/oauth/token?grant_type=authorization_code&code={}&client_id={}&redirect_uri={}&client_secret={}";


    private static final String GEE_USER_URL = "https://gitee.com/api/v5/user?access_token={}";

    @Override
    @Transactional
    public String getUserId(String code) {

        String clientId = giteeProperties.getClientId();
        String clientSecret = giteeProperties.getClientSecret();
        String redirectUrl = giteeProperties.getRedirectUrl();
        String tokenUrl = cn.hutool.core.util.StrUtil.format(QQ_ACCESSS_TOKEN_URL, code, clientId, redirectUrl, clientSecret);
        String respData = HttpUtil.post(tokenUrl, new HashMap<>());
        String accessToken = JSONUtil.parseObj(respData).get("access_token", java.lang.String.class);
        String userInfoStr = HttpUtil.get(StrUtil.format(GEE_USER_URL, accessToken));
        UserEntity userEntity = null;

        try {
            Map<String, String> userInfoMap = JSONUtil.toBean(userInfoStr, Map.class);
            UserGeeEntity geeEntity = new UserGeeEntity();
            geeEntity.setId(Long.valueOf(String.valueOf(userInfoMap.get("id"))));
            geeEntity.setAvatarUrl(userInfoMap.get("avatar_url"));
            geeEntity.setHtmlUrl(userInfoMap.get("html_url"));
            geeEntity.setLogin(userInfoMap.get("login"));
            geeEntity.setName(userInfoMap.get("name"));
            if (geeEntity.getId() == null) {
                throw new LoginFailureExcepiton(userInfoStr.toString());
            }

            userEntity = userDao.getEntityByGeeid(geeEntity.getId());
            if (userEntity == null) {
                return insertUser(geeEntity, geeEntity.getId());
            } else {
                return String.valueOf(userEntity.getId());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String insertUser(UserGeeEntity geeEntity, Long openid) throws JSONException {
        UserEntity userEntity = new UserEntity();
        userEntity.setHeadimg(geeEntity.getAvatarUrl());
        userEntity.setName(geeEntity.getName());
        userEntity.setGiteeId(openid);
        userEntity.setUsertype(0);
        userEntity.setCreateTime(DateUtil.currentTimestamp());
        userDao.insertUserByQq(userEntity);

        geeEntity.setUserId(userEntity.getId());
        userGiteeService.insertEntity(geeEntity);

        UserDetailEntity userDetailEntity = new UserDetailEntity();
        userDetailEntity.setUserid(userEntity.getId());
        userDetailDao.insert(userDetailEntity);

        // 异步将网络资源下载到本地，并且更新数据库
        /*threadPoolTaskExecutor.execute(() -> {
            userService.updateUserHeadImg(userEntity.getId(), pictureService.saveImage(headImg, "/head", "jpg"));
            //userService.updateUserUrl(userEntity.getId());
        });*/
        return String.valueOf(userEntity.getId());
    }

}
