package com.interest.model.entity;

import lombok.Data;

/**
 * @author wanghuan
 */
@Data
public class UserGeeEntity {
    private Long id;
    private String login;
    private String name;
    private String avatarUrl;
    private String htmlUrl;
    private Integer userId;
}
