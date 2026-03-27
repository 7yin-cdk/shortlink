package com.zhengqin.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhengqin.shortlink.project.dao.entity.ShortLinkDO;
import com.zhengqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.zhengqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询请求参数
     * @return 分页查询返回参数
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接每个分组内链接数量
     * @param requestParam gid集合
     * @return
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);
}
