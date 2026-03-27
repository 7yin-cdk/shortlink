package com.zhengqin.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 分组查询返回实体
 */
@Data
public class GroupRespDTO {
    /**
     * 分组标识（唯一标识符）
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;

    /**
     * 当前分组下短链接数量
     */
    private Integer shortLinkCount;
}
