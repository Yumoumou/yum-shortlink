package com.yum.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.common.biz.user.UserContext;
import com.yum.shortlink.admin.dao.entity.GroupDO;
import com.yum.shortlink.admin.dao.mapper.GroupMapper;
import com.yum.shortlink.admin.dto.response.ShortLinkGroupRespDTO;
import com.yum.shortlink.admin.service.IGroupService;
import com.yum.shortlink.admin.utils.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 短链接分组接口实现
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements IGroupService {

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    @Override
    public void saveGroup(String groupName) {
        // 生成一个唯一标识gid
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (hasGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .name(groupName)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 查询短链接分组
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        // TODO: 获取用户名
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }

    /**
     * 判断当前生成的gid是否与现有记录重复
     * @param gid 本次生成的gid
     */
    private boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // TODO 设置用户名
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return Objects.nonNull(hasGroupFlag);
    }

}
