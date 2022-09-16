package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className DictController
 * @description TODO
 * @date 2022-08-29 14:58
 */
@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Autowired
    private DictService dictService;

    //根据数据id查询子数据列表
    //dict::id
    //value: 缓存的命名空间
    @Cacheable(value = "dict", key = "'dict_cache_'+ #pid")
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{pid}")
    public R findChildData(@PathVariable Long pid) {
        List<Dict> list = dictService.findChildData(pid);
        return R.ok().data("list",list);
    }

    @ApiOperation(value="导出")
    @GetMapping(value = "/exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportData(response);
    }

    @ApiOperation(value = "导入")
    @CacheEvict(value = "dict",allEntries = true)
    @PostMapping("importData")
    public R importData(MultipartFile file) {
        dictService.importDictData(file);
        return R.ok();
    }

    // http://localhost:8202/admin/cmn/dict/getName/Hostype/1    三级甲等
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @PathVariable("parentDictCode") String parentDictCode,
            @PathVariable("value") String value) {

        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }


    // http://localhost:8202/admin/cmn/dict/getName/130100		石家庄市
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @PathVariable("value") String value) {

        return dictService.getNameByParentDictCodeAndValue("", value);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public R findByDictCode(
            @PathVariable String dictCode) {

        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }
}
