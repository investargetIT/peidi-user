package com.cyanrocks.boilerplate.dao;

import com.cyanrocks.boilerplate.dao.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderDao extends JpaRepository<SalesOrder,Integer> {
}
