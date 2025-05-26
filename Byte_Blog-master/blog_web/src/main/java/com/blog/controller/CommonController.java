package com.blog.controller;

import com.blog.result.Result;
import com.blog.utils.AliOssUtil;
import com.blog.utils.ImageCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: kai.hu
 * @Date: 2025-05-12-10:40
 * @Description: 通用接口，用于文件上传
 */
@Slf4j
@RestController
@RequestMapping("/api/common") // 添加/api前缀
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;


    /*
     * @Author: kai.hu
     * @Date: 2025-5-12
     * @Description: 文件上传
     */
    @PostMapping("/upload")
    public Result<?> upload(MultipartFile file){
        log.info("文件上传：{}",file);

        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀   dfdfdf.png
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称
            String objectName = UUID.randomUUID().toString() + extension;

            //文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            // 检查文件是否违规
            String result = ImageCheck.imageTag(filePath);
            if(result.equals("cat") || result.equals("dog")) {
                return Result.error("图片内容违规，请重新上传");
            }else {
                Map<String, Object> map = new HashMap<>();
                map.put("url", filePath);
                return Result.success(map);
//                return Result.success(filePath);
            }
//            Map<String, Object> map = new HashMap<>();
//            map.put("url", filePath);
//            return Result.success(map);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error("文件上传失败");
    }
}
