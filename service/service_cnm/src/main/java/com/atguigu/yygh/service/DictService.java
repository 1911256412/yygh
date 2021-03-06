package com.atguigu.yygh.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    List<Dict> selectById(String id);

    void exportExcel(HttpServletResponse response);

    void importExcel(MultipartFile file);

    String selectByValue(String dictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
