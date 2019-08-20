package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/24/2018 14:37
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService     {

    //增加日志
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }

        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);       //刚创建的分类。表示可用的

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if (categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }

        Category category=new Category();
        //设置Id，确保更新时，可以根据主键(id)更新
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount=categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新品类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名称失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)){
            //如果其子分类为空，那不需要向前端打印一个错误。但是我们需要填入日志
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点的id，及后代节点的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        //通过递归函数，获取分类当前节点和后代节点
        //通过guava的Sets，实现Set的初始化    （guava对集合的处理还是很强大的）
        Set<Category> categorySet = Sets.newHashSet();
        //通过Set不可重复的特性，来排重。但是要重写Set<category>中category的equals()和hashCode()方法（原因可参考《Head first Java》）
        findChildCategory(categorySet,categoryId);

        //将之前节点集合转为节点id集合
        //通过guava的Lists，实现Set的初始化
        List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null){
            for (Category categoryItem : categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    //递归算法，算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            categorySet.add(category);
        }
        //查找子节点，递归算法必须要有一个退出条件
        //categoryMapper是mybatis的，mybatis返回的集合不会有null，所以不用做null判断（如果调用一些不可预知的方法，又不做null判断，for循环中就可能出现nullpointer异常）
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }

        return categorySet;
    }
}
