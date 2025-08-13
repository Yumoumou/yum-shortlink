package com.yum.shortlink.admin.dto.request;

import lombok.Data;

/**
 * 短链接排序参数
 */
@Data
public class ShortLinkGroupSortReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
