package com.yum.shortlink.admin.controller;

import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.common.convention.result.Results;
import com.yum.shortlink.admin.dto.request.ShortLinkGroupSaveReqDTO;
import com.yum.shortlink.admin.dto.request.ShortLinkGroupSortReqDTO;
import com.yum.shortlink.admin.dto.request.ShortLinkGroupUpdateReqDTO;
import com.yum.shortlink.admin.dto.response.ShortLinkGroupRespDTO;
import com.yum.shortlink.admin.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/group")
public class GroupController {

    private final IGroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {

        groupService.saveGroup(requestParam.getName());

        return Results.success();
    }

    /**
     * 查询短链接分组
     */
    @GetMapping
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {

        return Results.success(groupService.listGroup());

    }

    /**
     * 修改短链接分组
     */
    @PutMapping
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {

        groupService.updateGroup(requestParam);

        return Results.success();
    }

    /**
     * 删除短链接分组
     */
    @DeleteMapping
    public Result<Void> deleteGroup(@RequestParam String gid) {

        groupService.deleteGroup(gid);

        return Results.success();
    }

    /**
     * 短链接分组排序
     */
    @PostMapping("/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam) {

        groupService.sortGroup(requestParam);

        return Results.success();
    }


}
