package com.study.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author smame210
 * @since 2021-08-30
 */
public interface IDictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> findChildData(Long id);

    void exportData(HttpServletResponse response);

    void importDictData(MultipartFile file);
}
