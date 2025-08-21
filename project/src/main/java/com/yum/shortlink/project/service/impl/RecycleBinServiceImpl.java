package com.yum.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.project.common.constants.RedisKeyConstants;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dao.mapper.ShortLinkMapper;
import com.yum.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.yum.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkPageRespDTO;
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

    /**
     * 分页查询回收站
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParam.getGidList())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .orderByDesc(ShortLinkDO::getCreateTime);
        ShortLinkRecycleBinPageReqDTO resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each ->
        {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 从回收站中恢复链接
     */
    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);

        // 删除空缓存键
        stringRedisTemplate.delete(String.format(RedisKeyConstants.GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }
}
