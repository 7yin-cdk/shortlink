package com.zhengqin.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhengqin.shortlink.admin.common.convention.result.Result;
import com.zhengqin.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.zhengqin.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.zhengqin.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * 短链接回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     * @param requestParam
     * @return
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
