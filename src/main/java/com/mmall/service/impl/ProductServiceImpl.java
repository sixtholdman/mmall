package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.uitl.DateTimeUtil;
import com.mmall.uitl.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.collections.list.PredicatedList;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/25/2018 14:57
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;


    private static Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);


    public ServerResponse saveOrUpdateProduct(Product product){
        if (product != null){
            //添加业务代码
            //设置主图，主图一般设置为图片组的第一张图片
            if (StringUtils.isNotBlank(product.getSubImages())){
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }
            //由product是否具有id，判断是更新还是新增。（id只有服务器能够生成，添加(数据库字段主键））
            if (product.getId() != null){
                int rowCount = productMapper.updateByPrimaryKeySelective(product);
                if (rowCount > 0){
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");
            }else {
                int rowCount = productMapper.insertSelective(product);
                if (rowCount > 0){
                    return ServerResponse.createBySuccessMessage("添加产品成功");
                }
                return ServerResponse.createBySuccessMessage("添加产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
    }

    public ServerResponse setSaleStatus(Integer productId,Integer status){
        if (productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("更新产品销售状态失败");
    }

    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        //开始业务处理
        //完整的系统，多采用pojo -> BO(Bussiness) -> VO(View Object)
        //由于本项目（一期）较为简单，所以只采用VO(Value Object)   （此VO，非彼VO）
        ProductDetailVo productDetailVo = assumbleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * VO-ProductDetailVo的组装方法
     * @param product
     * @return
     */
    private ProductDetailVo assumbleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        //imageHost，通过自定义的工具方法，读取配置文件
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.jarry.top/"));
        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null){
            productDetailVo.setParentCategoryId(0); //默认保存在根节点下
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        //createTime    需要将系统的毫秒数转为日期
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize){
        //pagehelper 使用逻辑:  第一步，startPage--start；第二步，填充自己的sql查询逻辑；第三步，pageHelper--收尾
        //第一步：startPage--start
        PageHelper.startPage(pageNum,pageSize);

        //第二步：填充自己的sql查询逻辑
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem :productList){
            ProductListVo productListVo = assumbleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        //第三步：pageHelper--收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * VO-ProductListVo的组装方法
     * @param product
     * @return
     */
    private ProductListVo assumbleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        //设置imageHost
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.jarry.top/"));

        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByProductNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem :productList){
            ProductListVo productListVo = assumbleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }



    //portal
    public ServerResponse<ProductDetailVo> productDetail(Integer productId){
        if (productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode() ) {
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        ProductDetailVo productDetailVo = assumbleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
        if (StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();

        if (categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)){
                //没有该分类，并且还没有关键字，这个时候返回一个空的结果集，不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)){
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByProductNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList){
            ProductListVo productListVo = assumbleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

}
