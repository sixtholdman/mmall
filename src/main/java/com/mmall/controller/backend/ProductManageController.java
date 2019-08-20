package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;

import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.uitl.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/25/2018 14:46
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;


    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse ProductSave(HttpSession session, Product product) {
        //检测用户登录情况
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        //检测用户管理员权限
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充增加商品的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充增加商品的业务逻辑
            return iProductService.setSaleStatus(productId,status);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充增加商品的业务逻辑
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充商品列表的业务逻辑
            return iProductService.getProductList(pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName,Integer productId,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充商品搜索的业务逻辑
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //这部分涉及文件上传问题，可以细看。其中springMVC有关文件上传的配置请查看dispatcher-servlet.xml。MultipartFile是有关springMVC的
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,
                                 //此处的value就是文件上传时的name属性（属性名-参数名）。不加该RequestParam注解，则以参数列表的的对应参数（file)为准。 （说明注解的value，默认值就是参数列表的参数）
                                 @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        //这里也需要进行身份核实，只有登录用户可以上传文件。避免被人批量上传文件，来攻击接口
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录管理员");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充文件上传的业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }


    //todo 这个方法有问题，未进行普通图片上传前，进行富文本上传，会发现，上传的图片没有扩展名。。。但是如果先普通上传一下，就没有这个问题。。。。
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request,
                                 //这里需要引入response，来修改header （这是semditor的要求）
                                 HttpServletResponse response){
        //simditor要求返回的类型和我们定义的ServerResponse不一致，故无法采用ServerResponse。但由于这不常用，所以直接设定一个map即可
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success",false);
            resultMap.put("msg","用户未登录，请登录管理员");
            return resultMap;
        }
        //富文本中对于返回值有自己的要求，我们使用的是simditor,所以按照simditor要求进行
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充富文本文件上传的业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            //只需要在成功的返回中添加
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作，需要管理员权限");
            return resultMap;
        }
    }
}
