package com.yum.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.common.convention.result.Results;
import com.yum.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.yum.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/")
public class ShortLinkController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 新建短链接
     */
    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShorLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);

    }
}
