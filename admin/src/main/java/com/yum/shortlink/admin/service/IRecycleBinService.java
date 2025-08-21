package com.yum.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yum.shortlink.admin.common.convention.result.Result;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.yum.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.yum.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * 短链接回收站接口层
 */
public interface IRecycleBinService {

    /**
     * 分页查询回收站
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
