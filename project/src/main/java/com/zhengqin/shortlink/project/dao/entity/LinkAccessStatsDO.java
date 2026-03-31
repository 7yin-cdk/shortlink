package com.zhengqin.shortlink.project.dao.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhengqin.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接访问统计表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_access_stats")
public class LinkAccessStatsDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量（PV）
     */
    private Integer pv;

    /**
     * 访客数（UV）
     */
    private Integer uv;

    /**
     * 独立IP数（UIP）
     */
    private Integer uip;

    /**
     * 小时（0-23）
     */
    private Integer hour;

    /**
     * 星期（1-7）
     */
    private Integer weekday;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标识（0-未删除 1-已删除）
     */
    private Integer delFlag;
}
