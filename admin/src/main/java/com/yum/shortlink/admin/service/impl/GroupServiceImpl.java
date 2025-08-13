package com.yum.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.admin.dao.entity.GroupDO;
import com.yum.shortlink.admin.dao.mapper.GroupMapper;
import com.yum.shortlink.admin.service.IGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements IGroupService {
}
