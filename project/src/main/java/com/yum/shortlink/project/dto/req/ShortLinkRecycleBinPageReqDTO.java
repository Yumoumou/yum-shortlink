package com.yum.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yum.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

/**
 * 短链接回收站分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
