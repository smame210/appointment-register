package com.study.cmnclient;

import com.study.model.cmn.Dict;
import com.study.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("service-cmn")
@Service
public interface DictFeignClient {
    @RequestMapping(value = "/admin/cmn/dict/findDict/{parentDictCode}/{value}", method = RequestMethod.GET)
    Dict findDict(@PathVariable String parentDictCode, @PathVariable Long value);

    @RequestMapping(value = "/admin/cmn/dict/findDict/{value}", method = RequestMethod.GET)
    Dict findDict(@PathVariable Long value);
}
