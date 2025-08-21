package com.yum.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yum.shortlink.project.common.convention.result.Result;
import com.yum.shortlink.project.common.convention.result.Results;
import com.yum.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.yum.shortlink.project.service.IRecycleBinService;
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

    private final IRecycleBinService recycleBinService;

    /**
     * 将链接放入回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        IPage<ShortLinkPageRespDTO> result = recycleBinService.pageShortLink(requestParam);
        return Results.success(result);
    }
}
