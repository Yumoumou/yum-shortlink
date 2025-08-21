package com.yum.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.common.convention.result.Results;
import com.yum.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.yum.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 将链接放入回收站
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
