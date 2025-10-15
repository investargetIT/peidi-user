package com.cyanrocks.boilerplate.dao.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Author wjq
 * @Date 2025/8/27 10:13
 */
@Entity
@Table(name = "user_oa")
@Data
public class UserOa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 新增主键字段，用于JPA实体标识

    @Column(name = "companystartdate", length = 255)
    private String companystartdate;

    @Column(name = "tempresidentnumber", length = 255)
    private String tempresidentnumber;

    @Column(name = "createdate", length = 255)
    private String createdate;

    @Column(name = "language", length = 255)
    private String language;

    @Column(name = "workstartdate", length = 255)
    private String workstartdate;

    @Column(name = "subcompanyid1", length = 255)
    private String subcompanyid1;

    @Column(name = "subcompanyname", length = 255)
    private String subcompanyname;

    @Column(name = "joblevel", length = 255)
    private String joblevel;

    @Column(name = "startdate", length = 255)
    private String startdate;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "subcompanycode", length = 255)
    private String subcompanycode;

    @Column(name = "jobactivitydesc", length = 255)
    private String jobactivitydesc;

    @Column(name = "bememberdate", length = 255)
    private String bememberdate;

    @Column(name = "modified", length = 255)
    private String modified;

    @Column(name = "oa_id", length = 255)
    private String oaId;

    @Column(name = "mobilecall", length = 255)
    private String mobilecall;

    @Column(name = "nativeplace", length = 255)
    private String nativeplace;

    @Column(name = "certificatenum", length = 255)
    private String certificatenum;

    @Column(name = "height", length = 255)
    private String height;

    @Column(name = "loginid", length = 255)
    private String loginid;

    @Column(name = "created", length = 255)
    private String created;

    @Column(name = "degree", length = 255)
    private String degree;

    @Column(name = "bepartydate", length = 255)
    private String bepartydate;

    @Column(name = "weight", length = 255)
    private String weight;

    @Column(name = "telephone", length = 255)
    private String telephone;

    @Column(name = "residentplace", length = 255)
    private String residentplace;

    @Column(name = "lastname", length = 255)
    private String lastname;

    @Column(name = "healthinfo", length = 255)
    private String healthinfo;

    @Column(name = "enddate", length = 255)
    private String enddate;

    @Column(name = "maritalstatus", length = 255)
    private String maritalstatus;

    @Column(name = "departmentname", length = 255)
    private String departmentname;

    @Column(name = "folk", length = 255)
    private String folk;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "birthday", length = 255)
    private String birthday;

    @Column(name = "accounttype", length = 255)
    private String accounttype;

    @Column(name = "jobcall", length = 255)
    private String jobcall;

    @Column(name = "managerid", length = 255)
    private String managerid;

    @Column(name = "assistantid", length = 255)
    private String assistantid;

    @Column(name = "departmentcode", length = 255)
    private String departmentcode;

    @Column(name = "belongto", length = 255)
    private String belongto;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "seclevel", length = 255)
    private String seclevel;

    @Column(name = "policy", length = 255)
    private String policy;

    @Column(name = "jobtitle", length = 255)
    private String jobtitle;

    @Column(name = "workcode", length = 255)
    private String workcode;

    @Column(name = "sex", length = 255)
    private String sex;

    @Column(name = "departmentid", length = 255)
    private String departmentid;

    @Column(name = "homeaddress", length = 255)
    private String homeaddress;

    @Column(name = "mobile", length = 255)
    private String mobile;

    @Column(name = "lastmoddate", length = 255)
    private String lastmoddate;

    @Column(name = "educationlevel", length = 255)
    private String educationlevel;

    @Column(name = "islabouunion", length = 255)
    private String islabouunion;

    @Column(name = "locationid", length = 255)
    private String locationid;

    @Column(name = "regresidentplace", length = 255)
    private String regresidentplace;

    @Column(name = "dsporder", length = 255)
    private String dsporder;
}
