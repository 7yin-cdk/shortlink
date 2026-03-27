package com.zhengqin.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhengqin.shortlink.admin.common.convention.result.Result;
import com.zhengqin.shortlink.admin.remote.ShortLinkRemoteService;
import com.zhengqin.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.zhengqin.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.zhengqin.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.zhengqin.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zhengqin.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接后管控制层
 */
// TODO 后续重构为springcloud调用
@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 查询分组下短链接数
     */
    @GetMapping("/api/short-link/admin/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> pageShortLink(@RequestParam("requestParam") List<String> requestParam) {
        return shortLinkRemoteService.listGroupShortLinkCount(requestParam);
    }
}
