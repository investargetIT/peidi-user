package com.cyanrocks.boilerplate.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2024/8/14 17:18
 */
@Entity
@Table(name = "user_token")
@Data
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,  // strategy 设置使用数据库主键自增策略；
            generator = "JDBC")
    private Long id;

    @Column(length = 100, name = "username")
    @Comment("用户名")
    private String username;

    @Column(length = 100, name = "service")
    @Comment("服务")
    private String service;

    @JsonFormat
    @Column(name = "expire_time")
    @Comment("过期时间")
    private LocalDateTime expireTime;
}
