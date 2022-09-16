package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className DictService
 * @description TODO
 * @date 2022-08-29 14:55
 */
public interface DictService{
    List<Dict> findChildData(Long pid);


    /**
     * 导出数据字典
     * @param response
     */
    void exportData(HttpServletResponse response);

    /**
     * 导入数据字典
     * @param file
     */
    public void importDictData(MultipartFile file);

    /**
     * 根据上级编码与值获取数据字典名称
     * @param parentDictCode
     *  @param value
     */
    String getNameByParentDictCodeAndValue(String parentDictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
