package com.cyanrocks.boilerplate.dao.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

/**
 * 销售订单
 *
 * @author liu 2024-07-11
 */
@Entity
@Table(name = "sales_order")
@Data
public class SalesOrder {
    //region field

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,  // strategy 设置使用数据库主键自增策略；
            generator = "JDBC")
//    @Column(name = "sid", nullable = false)
//    private Long sid;


    @Column(name = "channel")
    private Integer channel;

    @Column(name = "sourceChannel")
    private Integer  sourceChannel;

    @Column(name = "isVip")
    private Integer isVip;


    @Column(length = 20, name = "orderNo", nullable = false)
    private String orderNo;

    @Column(length = 50, name = "erpOrderNo")
    private String erpOrderNo;

    @Column(name = "orderStatus", nullable = false)
    private Integer orderStatus;

    @Column(name = "category", nullable = false)
    private Integer category;

    @Column(length = 60, name = "ouOrderNo")
    private String ouOrderNo;

    @Column(length = 50, name = "customerCode")
    private String customerCode;

    @Column(length = 100, name = "customerName")
    private String customerName;

    @Column(length = 20, name = "customerPhone")
    private String customerPhone;

    @Column(length = 20, name = "customerFax")
    private String customerFax;

    @Column(length = 20, name = "salesNo")
    private String salesNo;

    @Column(length = 50, name = "salesName")
    private String salesName;

    @Column(length = 20, name = "salesTel")
    private String salesTel;

    @Column(length = 100, name = "companyName")
    private String companyName;

    @Column(length = 20, name = "companyCode")
    private String companyCode;

    @Column(name = "freight")
    private BigDecimal freight;

    @Column(name = "pricingMethod")
    private String orderPricingMethod;


    @Column(name = "interFreight")
    private BigDecimal interFreight;

    @Column(name = "totalFreight")
    private BigDecimal totalFreight;

    @Column(name = "tariff")
    private BigDecimal tariff;

    @Column(name = "sendDate")
    private Date sendDate;

    @Column(name = "shipType")
    private String shipType;

    @Column(length = 10, name = "paymentMethod")
    private String paymentMethod;

    @Column(name = "needInvoice", nullable = false)
    private Boolean needInvoice = false;

    @Column(name = "attached")
    private Boolean attached = false;

    @Column(name = "partialShipment", nullable = false)
    private Boolean partialShipment;

    @Column(name = "userRemark")
    private String userRemark;

    @Column(name = "csrRemark")
    private String csrRemark;

    @Column(name = "releaseRemark")
    private String releaseRemark;

    @Column(name = "esCostCenterCode",length = 120)
    private String esCostCenterCode;

    @Column(name = "esCostCenterName",length = 120)
    private String esCostCenterName;
    /**
     * 企业站成本中心部门ID
     */

    @Column(name = "esDeptId")
    private Long esDeptId;
    /**
     *企业站成本中心部门名称
     */

    @Column(name = "esDeptName",length = 120)
    private String esDeptName;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "goodsAmount")
    private BigDecimal goodsAmount;

    @Column(name = "discountAmound")
    private BigDecimal discountAmount;

    @Column(name = "gpRate")
    private BigDecimal gpRate;

    @Column(length = 50, name = "customerPurchaseNo")
    private String customerPurchaseNo;

    @Column(length = 50, name = "customerPurchaseTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date customerPurchaseTime;

    @Column(length = 50, name = "purchaseName")
    private String purchaseName;

    @Column(length = 50, name = "purchaseCode")
    private String purchaseCode;

    @Column(length = 20, name = "paymentTerms")
    private String paymentTerms;

    @Column(name = "paymentMethodName")
    private String paymentMethodName;

    @Column(name = "paymentTermsName")
    private String paymentTermsName;

    @Column(name = "ordertag")
    private String ordertag;

    @Column(length = 50, name = "contactsName")
    private String contactsName;

    @Column(length = 50, name = "contactsCode")
    private String contactsCode;

    @Column(name = "rebate")
    private BigDecimal rebate;

    @Column(length = 50, name = "csrReviewTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date csrReviewTime;

    @Column(name = "orderTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderTime;

    @Column(length = 20, name = "csrReviewUser")
    private String csrReviewUser;

}
