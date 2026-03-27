package com.zhengqin.shortlink.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhengqin.shortlink.admin.dao.entity.GroupDO;
import com.zhengqin.shortlink.admin.dto.req.GroupSortReqDTO;
import com.zhengqin.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.zhengqin.shortlink.admin.dto.resp.GroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param name
     */
    void saveGroup(String name);

    /**
     * 新增短链接分组
     * @param username 用户名
     * @param name
     */
    void saveGroup(String username,String name);

    /**
     * 查询用户短链接分组集合
     * @return
     */
    List<GroupRespDTO> listGroup();

    /**
     * 修改短链接分组名
     * @param requestParam 修改分组参数
     */
    void updateGroup(GroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid 分组标识
     */
    void deleteGroup(String gid);

    /**
     * 分组排序
     * @param requestParams 分组排序请求参数
     */
    void sortGroup(List<GroupSortReqDTO> requestParams);
}
