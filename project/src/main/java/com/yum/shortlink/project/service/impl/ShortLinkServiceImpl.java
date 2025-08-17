package com.yum.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yum.shortlink.project.common.convention.exception.ClientException;
import com.yum.shortlink.project.common.convention.exception.ServiceException;
import com.yum.shortlink.project.common.enums.VaildDateTypeEnum;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dao.mapper.ShortLinkMapper;
import com.yum.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.yum.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.yum.shortlink.project.service.IShortLinkService;
import com.yum.shortlink.project.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 短链接服务层实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements IShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkMapper shortLinkMapper;

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

    /**
     * 查询指定分组内有多少短链接数
     * @param requestParam 查询短链接分组内数量请求参数 gids
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 修改短链接信息
     * @param requestParam 修改短链接请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO shortLinkRecord = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(shortLinkRecord)) {
            throw new ClientException("短链接记录不存在");
        }
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(shortLinkRecord.getDomain())
                .shortUri(shortLinkRecord.getShortUri())
                .clickNum(shortLinkRecord.getClickNum())
                .favicon(shortLinkRecord.getFavicon())
                .createdType(shortLinkRecord.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        if (Objects.equals(shortLinkRecord.getGid(), requestParam.getGid())) {
            // 未修改gid，直接更新信息
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    // 当有效类型为永久有效时，要保证有效期限是null
                    .set(Objects.equals(requestParam.getValidDateType(), VaildDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // 修改了gid，需要将原记录先删掉，再添加修改后的记录
            // 因为link是按照gid分表的
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, shortLinkRecord.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }
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
