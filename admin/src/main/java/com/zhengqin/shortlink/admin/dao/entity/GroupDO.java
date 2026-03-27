package com.zhengqin.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhengqin.shortlink.admin.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接分组实体
 */
@Data
@TableName("t_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDO extends BaseDO {

    /**
     * ID（主键）
     */
    private Long id;

    /**
     * 分组标识（唯一标识符）
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组所属用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}