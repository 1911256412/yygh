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
//@CrossOrigin
public class DictController {
    @Resource
    private DictService dictService;

    @ApiOperation("根据id查询数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable String  id) {
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

    //查询医院等级名称,根据 dictcode和value
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,
                          @PathVariable String value){

        String dictName=dictService.selectByValue(dictCode,value);
        return dictName;
    }

    //查询医院等级名称,根据 value
    @GetMapping("getName/{value}")
    public String getName(@PathVariable String value){
        String dictName=dictService.selectByValue("",value);
        return dictName;
    }
    @ApiOperation(value = "根据dictCode获取省份或者医院节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable  String dictCode){
        List<Dict> dicts=dictService.findByDictCode(dictCode);
        return Result.ok(dicts);
    }

}
