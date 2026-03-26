package com.zhengqin.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接分组下链接数统计请求实体
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数
     */
    private Integer shortLinkCount;
}
