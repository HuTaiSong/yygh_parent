package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nicc
 * @version 1.0
 * @className DictServiceImpl
 * @description TODO
 * @date 2022-08-29 14:55
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //实现类
    @Autowired
    DictListener dictListener;

    @Override
    public List<Dict> findChildData(Long pid) {

        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", pid);

        List<Dict> dictList = baseMapper.selectList(queryWrapper);

        dictList.forEach(dict -> {
            boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });

        return dictList;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel"); //指示响应内容的格式
            response.setCharacterEncoding("utf-8");

            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            // 指示响应内容以附件形式下载
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            List<Dict> dictList = baseMapper.selectList(null);

            //将Dict泛型转换成DictEeVo
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());

            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictVo);
                dictVoList.add(dictVo);
            }

            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //数据字典导入
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),
                            DictEeVo.class,dictListener)
                     .sheet()//默认选择第一个sheet表进行读取
                     .doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //如果value能唯一定位数据字典，parentDictCode可以传空，
        //例如：省市区的value值能够唯一确定
        if(StringUtils.isEmpty(parentDictCode)) {
            Dict dict = baseMapper.selectOne(
                    new QueryWrapper<Dict>().eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        } else {
            Dict parentDict = this.getDictByDictCode(parentDictCode);
            if(null == parentDict) return "";
            Dict dict = baseMapper.selectOne(
                    new QueryWrapper<Dict>().
                            eq("parent_id",parentDict.getId()).
                            eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        }
        return "";
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict codeDict = this.getDictByDictCode(dictCode);

        if(null == codeDict) return null;

        return this.findChildData(codeDict.getId());
    }

    //根据dict_code查询
    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }

    //判断是否有下级
    private boolean hasChildren(Long id){
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);

        Integer integer = baseMapper.selectCount(queryWrapper);

        return integer > 0;
    }
}
