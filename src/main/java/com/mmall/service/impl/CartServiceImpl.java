package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.uitl.BigDecimalUtil;
import com.mmall.uitl.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/27/2018 14:05
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
        //校验参数
        if (productId == null || count == null){
            //这个返回的ServerResponse并没有使用含T的泛型，故可以使用
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);

        if (cart == null){
            //这个产品不在购物车中。需新增该产品记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);

            cartMapper.insert(cartItem);
        }else {
            //这个产品已经在购物车中。只需增加产品数量即可
            count = cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);

        }
//        CartVo cartVo = this.getCartVoLimit(userId);
//        return ServerResponse.createBySuccess(cartVo);
//        有了list方法后，就可以通过复用，简化代码了
        return this.list(userId);
    }

    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count) {
        if (productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if (cart != null){
            cart.setQuantity(count);
            //self
            cartMapper.updateByPrimaryKeySelective(cart);
        }
//        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){
        //利用guava的splitter，来快速实现字符串的分割（否则就要使用数组来实现）
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //self
        int rowResult = cartMapper.deleteByUserIdProductIds(userId,productList);
        if (rowResult == 0){
            return ServerResponse.createByErrorMessage("删除产品失败");
        }
//        cartMapper.deleteByUserIdProductIds(userId,productList);
        return this.list(userId);
    }

    public ServerResponse<CartVo> list(Integer userId){
        //购物车中并不做分页处理
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> selectOrUnSelect(Integer userId,Integer productId,Integer checked){
        //购物车中并不做分页处理
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
//        CartVo cartVo = this.getCartVoLimit(userId);
//        return ServerResponse.createBySuccess(cartVo);
//        有了list方法后，就可以通过复用，简化代码了
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        //防止session中User存在，而User.userId不存在
        if (userId == null){
            //经过考虑后，个人认为只有login有将user写入session的入口，而这里不会产生上述情况。所以这里真的有必要。牺牲这一点点性能，来提高（可能没提高）安全性，
            // （这里如果将null发至数据库，会导致错误信息返回（但是这个错误信息页面，应该做过处理才对，并记入日志才对），并不会想前端暴露出一些系统信息。）
            //  后来，我又想了一下。也许是通过if的判断，来减少无效信息的性能消耗。不过这个决策是否真的提高了系统性能，不得而知。
            //  其问题的核心，还是在 该情况发生的可能性有多少。不过，从代码角度来说，这提高了代码的健壮性也是不可否认的
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 根据userId，返回检查过库存等校验后，并计算完总价的购物车Vo
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)){
            for (Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();

                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock()    );

                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()){
                        //库存充足时
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存。（要从数据库这一根源更新，才是有效，正确的）
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if (cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选，增加到购物车总价当中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if (userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }



}
