package com.yum.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.project.common.convention.exception.ServiceException;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dao.mapper.ShortLinkMapper;
import com.yum.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.yum.shortlink.project.service.IShortLinkService;
import com.yum.shortlink.project.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 短链接服务层实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements IShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(requestParam.getDomain() + "/" + shortLinkSuffix);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);

        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException ex) {
            // TODO: 需要考虑布隆过滤器误判，因此还是会有duplicateKey异常
            log.warn("短链接: {} 重复入库", requestParam.getDomain() + "/" + shortLinkSuffix);
            throw new ServiceException("短链接生成重复");
        }
        shortUriCreateCachePenetrationBloomFilter.add(requestParam.getDomain() + "/" + shortLinkSuffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    /******************************private*******************************/
    /**
     * 根据原始url生成短链接后缀
     * @param requestParam
     * @return
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        String originUrl = requestParam.getOriginUrl();
        String shortUri = null;
        int customGenerateCount = 0;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接生成重复");
            }
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }

            customGenerateCount++;

        }
        return shortUri;
    }
}
