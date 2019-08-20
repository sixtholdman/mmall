package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description：
 * @Author: jarry
 * @Date: 12/26/2018 13:38
 */
public interface IFileService {

    String upload(MultipartFile file, String path);

}
