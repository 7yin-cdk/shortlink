package com.zhengqin.shortlink.admin.controller;

import com.zhengqin.shortlink.admin.common.convention.result.Result;
import com.zhengqin.shortlink.admin.common.convention.result.Results;
import com.zhengqin.shortlink.admin.dto.req.GroupReqDTO;
import com.zhengqin.shortlink.admin.dto.req.GroupSortReqDTO;
import com.zhengqin.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.zhengqin.shortlink.admin.dto.resp.GroupRespDTO;
import com.zhengqin.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增用户分组
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> saveGroup(@RequestBody GroupReqDTO requestParam){
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询用户分组集合
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<GroupRespDTO>> listGroup(){
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody GroupUpdateReqDTO requestParam){
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组名
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> deleteGroup(@RequestParam String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }


    @PostMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<GroupSortReqDTO> requestParams){
        groupService.sortGroup(requestParams);
        return Results.success();
    }

}
