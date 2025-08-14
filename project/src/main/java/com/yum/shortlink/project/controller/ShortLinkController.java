package com.yum.shortlink.project.controller;

import com.yum.shortlink.project.common.convention.result.Result;
import com.yum.shortlink.project.common.convention.result.Results;
import com.yum.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.yum.shortlink.project.service.IShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制层
 */
@RestController
@RequestMapping("/api/short-link/v1")
@RequiredArgsConstructor
public class ShortLinkController {

    private final IShortLinkService shortLinkService;

    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShorLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        ShortLinkCreateRespDTO result = shortLinkService.createShortLink(requestParam);
        return Results.success(result);
    }
}
