package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    //custom    防止横向越权问题
    int deleteByShippingIdUserId(@Param("shippingId")Integer shippingId,@Param("userId")Integer userId);

    //custom
    int updateByShipping(Shipping shipping);

    //custom
    Shipping selectByShippingIdUserId(@Param("shippingId")Integer shippingId,@Param("userId")Integer userId);

    //custom
    List<Shipping> listByUserId(Integer userId);
}