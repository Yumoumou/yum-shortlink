package com.yum.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.dao.entity.GroupDO;
import com.yum.shortlink.admin.dao.mapper.GroupMapper;
import com.yum.shortlink.admin.service.IGroupService;
import com.yum.shortlink.admin.utils.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 判断当前生成的gid是否与现有记录重复
     * @param gid
     * @return
     */
    private boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // TODO 设置用户名
                .eq(GroupDO::getUsername, null);
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return Objects.nonNull(hasGroupFlag);
    }

}
