package com.atguigu.yygh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.listener.ExcelListener;
import com.atguigu.yygh.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.service.DictService;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> selectById(String id) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getParentId, id);
        List<Dict> dicts = baseMapper.selectList(wrapper);
        dicts.stream().forEach(dict -> {
            boolean isChild = isChildren(dict.getId());
            dict.setHasChildren(isChild);
        });
        return dicts;
    }

    @Override
    public void exportExcel(HttpServletResponse response) {
        try {
            //从浏览器得到数据流服务器响应写到本地
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
// 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            List<Dict> dicts = baseMapper.selectList(null);
            List<DictEeVo> dictEeVos = new ArrayList<>();
            dicts.stream().forEach(dict -> {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);
                dictEeVos.add(dictEeVo);
            });
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典")
                    .doWrite(dictEeVos);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importExcel(MultipartFile file) {
        try {
            //读取文件流 写到excel中
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new ExcelListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //按照dict和value的值来查询
    @Override
    public String selectByValue(String dictCode, String value) {
        if (StringUtils.isEmpty(dictCode)) {
            LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dict::getValue, value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        } else {
            //如果dictcode不为空，根据dictcode查询id
            LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dict::getDictCode, dictCode);
            Dict dict = baseMapper.selectOne(wrapper);
            LambdaQueryWrapper<Dict> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(Dict::getParentId, dict.getId());
            wrapper1.eq(Dict::getValue,value);
            Dict dict1 = baseMapper.selectOne(wrapper1);
            return dict1.getName();
        }

    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {

        Dict dict = this.selectBydictCode(dictCode);
        List<Dict> DictList = this.selectById(dict.getId().toString());
        return DictList;
    }

    private Dict selectBydictCode(String dictCode) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getDictCode, dictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }

    //判断是否有子节点
    public boolean isChildren(Long id) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getParentId, id);
        Integer integer = baseMapper.selectCount(wrapper);
        return integer > 0;
    }
}
