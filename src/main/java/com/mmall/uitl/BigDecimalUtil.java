package com.mmall.uitl;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/27/2018 15:19
 */
public class BigDecimalUtil {

    private BigDecimalUtil(){

    }

    public static BigDecimal add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }
    public static BigDecimal sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }
    public static BigDecimal mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }
    public static BigDecimal div(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        //考虑除之不尽的情况，利用BigDecimal.divide的重载方法
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);    //保留两位小数，四舍五入

    }

}
