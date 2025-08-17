package com.yum.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.common.biz.user.UserContext;
import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.dao.entity.GroupDO;
import com.yum.shortlink.admin.dao.mapper.GroupMapper;
import com.yum.shortlink.admin.dto.request.ShortLinkGroupSortReqDTO;
import com.yum.shortlink.admin.dto.request.ShortLinkGroupUpdateReqDTO;
import com.yum.shortlink.admin.dto.response.ShortLinkGroupRespDTO;
import com.yum.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.yum.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.yum.shortlink.admin.service.IGroupService;
import com.yum.shortlink.admin.utils.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 短链接分组接口实现
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements IGroupService {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    /**
     * 新增短链接分组, 供无法在UserContext中获取username时使用
     * @param groupName 短链接分组名
     * @param username 用户名
     */
    @Override
    public void saveGroup(String username, String groupName) {
        // 生成一个唯一标识gid
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (hasGid(username, gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .name(groupName)
                .username(username)
                .sortOrder(0)
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 查询短链接分组
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        Result<List<ShortLinkGroupCountQueryRespDTO>> groupCountResult = shortLinkRemoteService.listGroupShortLinkCount(
                groupDOList.stream()
                        .map(GroupDO::getGid)
                        .collect(Collectors.toList()));
        List<ShortLinkGroupRespDTO> result = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
        result.forEach(each -> {
            Optional<ShortLinkGroupCountQueryRespDTO> first = groupCountResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });
        return result;
    }

    /**
     * 修改短链接分组
     * @param requestParam 修改链接分组参数
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getGid, requestParam.getGid());

        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());

        baseMapper.update(groupDO, updateWrapper);
    }

    /**
     * 删除短链接分组
     *
     * 软删除 -- 将删除标识设置为1
     * @param gid 短链接分组标识
     */
    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getGid, gid);

        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);

        baseMapper.update(groupDO, updateWrapper);
    }

    /**
     * 短链接分组排序
     * @param requestParam 短链接分组排序参数
     */
    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(record -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(record.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, record.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }

    /*****************************private****************************/

    /**
     * 判断当前生成的gid是否与现有记录重复
     * @param gid 本次生成的gid
     */
    private boolean hasGid(String username, String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return Objects.nonNull(hasGroupFlag);
    }

}
