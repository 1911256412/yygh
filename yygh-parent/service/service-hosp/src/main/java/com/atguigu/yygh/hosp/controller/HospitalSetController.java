package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.handler.YyghException;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.result.Result;
import com.atguigu.yygh.utils.MD5;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin
@Api(tags = "医院设置")
public class HospitalSetController {
    @Resource
    private HospitalSetService hospitalSetService;

    @GetMapping("findAll")
    @ApiOperation("查询所有医院")
    public Result selectAll() {

        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    @DeleteMapping("{id}")
    @ApiOperation("逻辑删除医院")
    public Result deleteById(@PathVariable Long id) {
        boolean b = hospitalSetService.removeById(id);
        if (b) {
            return Result.ok();
        }
        return Result.fail();
    }

    @PostMapping("/findPageHospSet/{current}/{limit}")
    @ApiOperation("条件分页查询")
    public Result selectPage(@PathVariable long current, @PathVariable long limit, @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        Page<HospitalSet> page = new Page<HospitalSet>(current, limit);
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        String hosname = hospitalSetQueryVo.getHosname();
        String hoscode = hospitalSetQueryVo.getHoscode();
        if (!StringUtils.isEmpty(hosname)) {
            wrapper.like(HospitalSet::getHosname, hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hoscode)) {
            wrapper.eq(HospitalSet::getHoscode, hospitalSetQueryVo.getHoscode());
        }
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);
        return Result.ok(hospitalSetPage);
    }
    @PostMapping("/saveHospitalSet")
    @ApiOperation("添加医院")
    public Result saveHosp(@RequestBody HospitalSet hospitalSet) {
        //设置状态为1可以使用，状态为0不可以使用
        hospitalSet.setStatus(1);
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        boolean save = hospitalSetService.save(hospitalSet);
        if (save) {
            return Result.ok();

        }
        return Result.fail();
    }

    @GetMapping("/getHospSet/{id}")
    @ApiOperation("根据id查询医院")
    public Result select(@PathVariable long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    @PostMapping("/updateHospitalSet")
    @ApiOperation("修改医院")
    public Result update(@RequestBody HospitalSet hospitalSet) {
        boolean b = hospitalSetService.updateById(hospitalSet);
        if (b) {
            return Result.ok();
        }
        return Result.ok();
    }

    @DeleteMapping("/batchRemove")
    @ApiOperation("批量删除医院")
    public Result deleteBath(@RequestBody List<Long> idlist) {
        boolean b = hospitalSetService.removeByIds(idlist);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //医院设置锁定和解锁
    @ApiOperation("设置医院锁定和解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id, @PathVariable Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }

    /**
     * 发送签名key，
     * 医院信息配置后，可以通过短信的形式发送医院编号与签名key给联系人，
     * 联系人拿到该信息就可以参考《尚医通API接口文档.docx》对接接口了。
     */
    @ApiOperation("发送签名秘钥")
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //TODO 发送短信
        return Result.ok();
    }

}
