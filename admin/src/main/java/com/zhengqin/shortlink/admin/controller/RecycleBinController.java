package com.zhengqin.shortlink.project.controller;

import com.zhengqin.shortlink.project.common.convention.result.Result;
import com.zhengqin.shortlink.project.common.convention.result.Results;
import com.zhengqin.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.zhengqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.zhengqin.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }
}
