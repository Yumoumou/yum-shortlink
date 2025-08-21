package com.yum.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.project.common.constants.RedisKeyConstants;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dao.mapper.ShortLinkMapper;
import com.yum.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.yum.shortlink.project.service.IRecycleBinService;
import com.yum.shortlink.project.utils.LinkUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements IRecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 将链接放入回收站
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);

        // 删除缓存中的数据
        stringRedisTemplate.delete(String.format(RedisKeyConstants.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }
}
