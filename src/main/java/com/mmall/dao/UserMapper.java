package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //custom
    int checkUsername(String username);

    //custom
    int checkEmail(String email);

    //custom    多个输入参数时，需要采用mybatis的Param注解。在对应的xml文件中，参数名取得是Param注解中的名字
    User selectLogin(@Param("username") String username, @Param("password") String password);

    //custonm
    String selectQuestionByUsername(String username);

    //custom
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    //custom
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);

    //custom
    int checkPassword(@Param("passwordOld") String passwordOld, @Param("userId") Integer userId);

    //custom
    int checkEmailByUserId(@Param("email") String email,@Param("userId") Integer userId);
}