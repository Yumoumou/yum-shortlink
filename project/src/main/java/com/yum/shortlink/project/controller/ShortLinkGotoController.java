package com.yum.shortlink.project.controller;


import com.yum.shortlink.project.service.IShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接跳转控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkGotoController {

    private final IShortLinkService shortLinkService;

    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        shortLinkService.restoreUrl(shortUri, request, response);
    }
}
