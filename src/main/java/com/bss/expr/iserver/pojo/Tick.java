package com.bss.expr.iserver.pojo;

import lombok.Builder;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class Tick implements Serializable {
    @QuerySqlField(index = true)
    private String orderId;
    @QuerySqlField(index = true)
    private String micCode;
    @QuerySqlField(index = true)
    private String ricCode;
    @QuerySqlField
    private long quantity;
    @QuerySqlField
    private double price;
    @QuerySqlField(index = true)
    private String side;
    private Date date;
}
