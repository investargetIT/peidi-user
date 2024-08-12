package com.cyanrocks.boilerplate.dao;

import com.cyanrocks.boilerplate.dao.entity.SalesOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import javax.annotation.Resource;
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SalesOrderRepositoryTest {


    @Resource
    SalesOrderDao salesOrderRepository;

    @Test
    public void salesOrderInsert(){
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderNo("ddd2");
        salesOrder.setOrderStatus(1);
        salesOrder.setCategory(1);
        salesOrder.setNeedInvoice(true);
        salesOrder.setAttached(false);
        salesOrder.setPartialShipment(false);
       // salesOrder.setSid(1000000000L);
        salesOrder.setCompanyName("新色");
        salesOrder.setSalesName("adasd");
        salesOrderRepository.save(salesOrder);
    }
}



