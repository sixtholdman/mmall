package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/29/2018 18:54
 */
@Service("iShippingService")
public class ShippingService implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;


    public ServerResponse add(Integer userId,Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0){
            //需要将新增的地址的id返回
            Map result = Maps.newHashMap();
            //shippingId是由我们在mybatis中设置的userGeneratedKey生成的，返回的。
            result.put("shippintId",shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新增地址失败");
    }

    public ServerResponse del(Integer userId,Integer shippingId){
        //不可以采用这个语句，因为这会产生横向越权的问题（是个用户都可以调用这个接口，来删除地址（也许这个地址不是他本人的））
//        int resultCount = shippingMapper.deleteByPrimaryKey(shippingId);
        //新增一个自己写的删除地址操作，必要要通过userId和shippingId两个参数来进行操作。防止横向越权问题
        int resultCount = shippingMapper.deleteByShippingIdUserId(shippingId,userId);

        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse update(Integer userId,Shipping shipping){
        //这里不是删除，只是更新，可以通过设置session.userId来防止横向越权
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);

        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId,Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(shippingId,userId);

        if (shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("更新地址成功",shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.listByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
