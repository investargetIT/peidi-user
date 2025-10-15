package com.cyanrocks.boilerplate.dao.entity;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author wjq
 * @Date 2025/4/28 14:03
 */
@Entity
@Table(name = "user_site")
@Data
public class UserSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name")
    @Comment("基地名称")
    private String siteName;

    @Column(name = "site_region")
    @Comment("所在地区，如 浙江·杭州")
    private String siteRegion;

    @Column(name = "active")
    @Comment("是否启用")
    private Boolean active;

}