package com.yum.shortlink.admin.controller;

import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.common.convention.result.Results;
import com.yum.shortlink.admin.dto.request.ShortLinkGroupSaveReqDTO;
import com.yum.shortlink.admin.dto.response.ShortLinkGroupRespDTO;
import com.yum.shortlink.admin.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final IGroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping("api/short-link/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {

        groupService.saveGroup(requestParam.getName());

        return Results.success();
    }

    /**
     * 查询短链接分组
     */
    @GetMapping("api/short-link/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {

        return Results.success(groupService.listGroup());

    }
}
