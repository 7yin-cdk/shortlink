package com.zhengqin.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhengqin.shortlink.project.common.convention.exception.ServiceException;
import com.zhengqin.shortlink.project.dao.entity.ShortLinkDO;
import com.zhengqin.shortlink.project.dao.mapper.ShortLinkMapper;
import com.zhengqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zhengqin.shortlink.project.service.ShortLinkService;
import com.zhengqin.shortlink.project.tookit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static cn.hutool.core.date.DateTime.now;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain()+"/"+shortLinkSuffix;
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .fullShortUrl(fullShortUrl)
                .enableStatus(0)
                .build();
        shortLinkDO.setCreateTime(now());
        shortLinkDO.setUpdateTime(now());
        shortLinkDO.setDelFlag(0);
        try{
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException e){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if(hasShortLinkDO != null){
                log.warn("短链接：{}重复入库",fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    public String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while(true){
            if(customGenerateCount > 10){
                throw new ServiceException("短链接创建频繁");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if(!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain()+"/"+shortUri)){
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
