package com.atguigu.yygh.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.service.OSSservice;
import com.atguigu.yygh.utils.ConstantPropertiesUtil;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class OSSserviceImpl implements OSSservice {

    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantPropertiesUtil.END_POINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantPropertiesUtil.KEY_ID;
        String accessKeySecret = ConstantPropertiesUtil.KEY_SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantPropertiesUtil.BUCKET_NAME;
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        //String objectName = "exampledir/exampleobject.txt";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        //String filePath = "D:\\localpath\\examplefile.txt";
        //原始文件名称
        String filename = file.getOriginalFilename();
        //把"-"线换成空格
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String filetype = filename.substring(filename.lastIndexOf("."));
        //把文件按照日期分类 ，新的文件名称，带路径添加    20/1/10/
        String dataPath = new DateTime().toString("yyyy/MM/dd");
        String newfile = dataPath+uuid + filetype;
        //https://edu-benteng.oss-cn-hangzhou.aliyuncs.com/%E8%93%9D%E5%BA%95%E7%85%A7%E7%89%87.jpg
        //返回的路径需要拼接
        String url = "https://" + bucketName + "." + endpoint + "/" + newfile;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            InputStream inputStream = file.getInputStream();
            // 创建PutObject请求。
            ossClient.putObject(bucketName, newfile, inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }

        }
        return url;
    }


}