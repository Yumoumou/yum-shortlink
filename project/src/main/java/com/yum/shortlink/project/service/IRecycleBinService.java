package com.yum.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dto.req.*;
import com.yum.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface IRecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将链接放入回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 从回收站恢复短链接
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 从回收站中彻底删除
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
