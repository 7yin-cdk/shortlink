package com.zhengqin.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhengqin.shortlink.admin.common.biz.user.UserContext;
import com.zhengqin.shortlink.admin.common.convention.result.Result;
import com.zhengqin.shortlink.admin.dao.entity.GroupDO;
import com.zhengqin.shortlink.admin.dao.mapper.GroupMapper;
import com.zhengqin.shortlink.admin.dto.req.GroupSortReqDTO;
import com.zhengqin.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.zhengqin.shortlink.admin.dto.resp.GroupRespDTO;
import com.zhengqin.shortlink.admin.remote.ShortLinkRemoteService;
import com.zhengqin.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zhengqin.shortlink.admin.service.GroupService;
import com.zhengqin.shortlink.admin.toolkit.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DateTime.now;

/**
 * 短链接接口分组实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public void saveGroup(String name) {
        saveGroup(UserContext.getUsername(),name);
    }

    @Override
    public void saveGroup(String username, String name) {
        String gid;
        while(true){
            gid = RandomUtil.generateRandomString();
            if(!hasGid(username,gid)){
                break;
            }
        }
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .username(username)
                .name(name)
                .sortOrder(0)
                .build();
        groupDO.setCreateTime(now());
        groupDO.setUpdateTime(now());
        groupDO.setDelFlag(0);
        baseMapper.insert(groupDO);
    }

    @Override
    public List<GroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getCreateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(queryWrapper);
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService
                .listGroupShortLinkCount(groupDOS.stream().map(GroupDO::getGid).toList());
        List<GroupRespDTO> groupRespDTOList = BeanUtil.copyToList(groupDOS, GroupRespDTO.class);
        groupRespDTOList.forEach(each->{
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(item->each.setShortLinkCount(first.get().getShortLinkCount()));
        });
        return groupRespDTOList;
    }

    @Override
    public void updateGroup(GroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        groupDO.setUpdateTime(now());
        baseMapper.update(groupDO,updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid,gid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO,updateWrapper);
    }

    @Override
    public void sortGroup(List<GroupSortReqDTO> requestParams) {
        for (GroupSortReqDTO groupSortReqDTO : requestParams) {
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getGid, groupSortReqDTO.getGid())
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getDelFlag, 0);
            GroupDO groupDO = new GroupDO();
            groupDO.setSortOrder(groupSortReqDTO.getSortOrder());
            groupDO.setUpdateTime(now());
            baseMapper.update(groupDO,updateWrapper);
        }
    }

    public boolean hasGid(String username,String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO groupDO = baseMapper.selectOne(queryWrapper);
        return groupDO != null;
    }
}
