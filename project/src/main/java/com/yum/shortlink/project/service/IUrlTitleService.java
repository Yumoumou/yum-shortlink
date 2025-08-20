package com.yum.shortlink.project.service;

import com.yum.shortlink.project.common.convention.result.Result;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * URL标题接口层
 */
public interface IUrlTitleService {

    /**
     * 根据url获取标题
     * @param url
     * @return 网站标题
     */
    String getTitleByUrl(@RequestParam("url") String url);
}
