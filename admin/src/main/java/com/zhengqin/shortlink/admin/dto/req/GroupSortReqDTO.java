package com.zhengqin.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class GroupSortReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
