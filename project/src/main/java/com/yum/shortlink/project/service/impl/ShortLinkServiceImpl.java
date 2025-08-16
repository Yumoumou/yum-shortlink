package com.yum.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.project.common.convention.exception.ServiceException;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dao.mapper.ShortLinkMapper;
import com.yum.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkPageRespDTO;
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

    /**
     * 新建短链接
     * @param requestParam 创建短链接请求参数
     * @return
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();

        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException ex) {
            // 需要考虑布隆过滤器误判，因此还是会有duplicateKey异常
            // 再去数据库查一遍，真的重复了再抛出异常
            // 主要是为了防止多线程并发情况下的错误，有多个线程可能会拿到相同的不存在的URI然后返回，接着去插入数据库
            // 相当于使用分布式锁，但性能高于分布式锁
            // TODO：布隆过滤器满了的情况，创建一个新的布隆过滤器，迁移旧过滤器的信息
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.<ShortLinkDO>lambdaQuery()
                            .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (Objects.nonNull(hasShortLinkDO)) {
                log.warn("短链接: {} 重复入库", requestParam.getDomain() + "/" + shortLinkSuffix);
                throw new ServiceException("短链接生成重复");
            }

        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        ShortLinkPageReqDTO resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    /******************************private*******************************/
    /**
     * 根据原始url生成短链接后缀
     * @param requestParam
     * @return
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {

        String shortUri = null;
        int customGenerateCount = 0;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接生成重复");
            }
            String originUrl = requestParam.getOriginUrl();
            // 改变url的hash值，防止冲突
            // 这样会导致同一个链接能够生成多个短链接，这是对的，业务上需要这个特征
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }

            customGenerateCount++;

        }
        return shortUri;
    }
}
