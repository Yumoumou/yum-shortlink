package com.yum.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yum.shortlink.admin.common.biz.user.UserContext;
import com.yum.shortlink.admin.common.convention.exception.ServiceException;
import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.dao.entity.GroupDO;
import com.yum.shortlink.admin.dao.mapper.GroupMapper;
import com.yum.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.yum.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.yum.shortlink.admin.service.IRecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短链接回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements IRecycleBinService {

    private final GroupMapper groupMapper;

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 回收站分页查询
     */
    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        // 获取用户的所有分组
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).collect(Collectors.toList()));
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
