package com.atguigu.yygh.controller;

import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(tags = "数据字典")
@RequestMapping("/admin/cmn/dict")
@CrossOrigin
public class DictController {
    @Resource
    private DictService dictService;

    @ApiOperation("根据id查询数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.selectById(id);
        return Result.ok(list);
    }

    @ApiOperation("导出数据字典Excel")
    @GetMapping("exportExcel")
    public Result exportExcel(HttpServletResponse response) {
        dictService.exportExcel(response);
        return Result.ok();
    }
    @ApiOperation("导入数据字典Excel")
    @PostMapping("importExcel")
    public Result importExcel(MultipartFile file) {
        dictService.importExcel(file);
        return Result.ok();
    }
}
