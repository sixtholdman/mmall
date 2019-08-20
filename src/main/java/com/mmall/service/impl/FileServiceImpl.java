package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.uitl.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/26/2018 13:39
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();

        //获取扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;

        logger.info("开始上传文件，上传文件的原始文件名为：{}，上传的路径为：{}，新文件名为：{}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try{
            //文件成功上传targetFile
            file.transferTo(targetFile);
            //已经上传FTP服务器
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 上传完成之后，删除upload下面的文件
            targetFile.delete();
        }catch (Exception e){
            logger.error("文件上传失败",e);
            return null;
        }
        return targetFile.getName();
    }
}
