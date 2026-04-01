package com.zhengqin.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhengqin.shortlink.project.dao.entity.LinkLocalStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 地区统计持久层
 */
public interface LinkLocalStatsMapper extends BaseMapper<LinkLocalStatsDO> {

    /**
     * 记录基础访问监控数据
     * @param linkLocalStatsDO
     */
    @Insert("INSERT INTO t_link_locale_stats (full_short_url, gid, date, cnt, province, city, adcode, country, create_time, update_time, del_flag)" +
            "VALUES (#{linkLocalStats.fullShortUrl}, #{linkLocalStats.gid}, #{linkLocalStats.date}, #{linkLocalStats.cnt}, #{linkLocalStats.province},#{linkLocalStats.city}, #{linkLocalStats.adcode}, #{linkLocalStats.country}, NOW(), NOW(), 0)" +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{linkLocalStats.cnt};")
    void shortLinkLocalStats(@Param("linkLocalStats") LinkLocalStatsDO linkLocalStatsDO);
}
