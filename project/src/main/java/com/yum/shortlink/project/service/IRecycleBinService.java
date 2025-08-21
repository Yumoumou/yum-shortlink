package com.yum.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import com.yum.shortlink.project.dto.req.RecycleBinSaveReqDTO;

/**
 * 回收站管理接口层
 */
public interface IRecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将链接放入回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);
}
