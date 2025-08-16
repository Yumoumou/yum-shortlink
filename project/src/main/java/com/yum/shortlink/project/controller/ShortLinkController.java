package com.yum.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yum.shortlink.project.common.convention.result.Result;
import com.yum.shortlink.project.common.convention.result.Results;
import com.yum.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.yum.shortlink.project.service.IShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接控制层
 */
@RestController
@RequestMapping("/api/short-link/v1")
@RequiredArgsConstructor
public class ShortLinkController {

    private final IShortLinkService shortLinkService;

    /**
     * 新建短链接
     */
    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShorLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        ShortLinkCreateRespDTO result = shortLinkService.createShortLink(requestParam);
        return Results.success(result);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkPageRespDTO> result = shortLinkService.pageShortLink(requestParam);
        return Results.success(result);
    }
}
