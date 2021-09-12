package com.study.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.cmn.listener.DictListener;
import com.study.cmn.mapper.DictMapper;
import com.study.cmn.service.IDictService;
import com.study.model.cmn.Dict;
import com.study.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author smame210
 * @since 2021-08-30
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements IDictService {

    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long parentId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId);
        List<Dict> dicts = baseMapper.selectList(wrapper);
        dicts.forEach(dict -> {
            boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });
        return dicts;
    }

    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        List<Dict> dicts = new ArrayList<>();
        if(dict != null){
             dicts = this.findChildData(dict.getId());
        }
        return dicts;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = "dict";
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            List<Dict> dicts = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dicts.size());
            dicts.forEach(dict -> {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);
                dictVoList.add(dictEeVo);
            });

            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public Dict findDictByParentDictCodeAndValue(String dictCode, Long value) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(wrapper);

        QueryWrapper<Dict> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("parent_id", dict.getId());
        wrapper2.eq("value", value);
        Dict dict2 = baseMapper.selectOne(wrapper2);
        return dict2;
    }

    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public Dict findDictByValue(Long value) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("value", value);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }

    private boolean hasChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count>0;
    }
}
