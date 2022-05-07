package com.atguigu.yygh.controller;

import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.service.OSSservice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/oss/file")
public class OssController {

    @Resource
    private OSSservice osSservice;
    @PostMapping("fileupload")
    public Result  fileupload(MultipartFile file){

        String upload = osSservice.upload(file);
        return Result.ok(upload);
    }
}
