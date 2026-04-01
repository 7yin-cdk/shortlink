package com.zhengqin.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhengqin.shortlink.project.common.convention.exception.ServiceException;
import com.zhengqin.shortlink.project.common.enums.VailDateTypeEnum;
import com.zhengqin.shortlink.project.dao.entity.*;
import com.zhengqin.shortlink.project.dao.mapper.*;
import com.zhengqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.zhengqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.zhengqin.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zhengqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.zhengqin.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import com.zhengqin.shortlink.project.service.ShortLinkService;
import com.zhengqin.shortlink.project.tookit.HashUtil;
import com.zhengqin.shortlink.project.tookit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.hutool.core.date.DateTime.now;
import static com.zhengqin.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;
import static com.zhengqin.shortlink.project.common.constant.RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY;
import static com.zhengqin.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper  shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper  linkAccessStatsMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

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
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        }catch (DuplicateKeyException e){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if(hasShortLinkDO != null){
                log.warn("短链接：{}重复入库",fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        stringRedisTemplate.opsForValue().set(GOTO_SHORT_LINK_KEY+fullShortUrl, shortLinkDO.getOriginUrl(), LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://"+shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if(hasShortLinkDO == null){
            throw new ServiceException("短链接记录不存在");
        }
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .clickNum(hasShortLinkDO.getClickNum())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        if(Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())){
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO,updateWrapper);
        }else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each,ShortLinkPageRespDTO.class);
            result.setDomain("http://"+result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList,ShortLinkGroupCountQueryRespDTO.class);
    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(GOTO_SHORT_LINK_KEY + fullShortUrl);
        if (StrUtil.isNotBlank(originalLink)) {
            shortLinkStats(null,fullShortUrl,request,response);
            ((HttpServletResponse)response).sendRedirect(originalLink);
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if(!contains){
            ((HttpServletResponse)response).sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(LOCK_GOTO_SHORT_LINK_KEY + fullShortUrl);
        lock.lock();
        try{
            originalLink = stringRedisTemplate.opsForValue().get(GOTO_SHORT_LINK_KEY + fullShortUrl);
            if (StrUtil.isNotBlank(originalLink)) {
                shortLinkStats(null,fullShortUrl,request,response);
                ((HttpServletResponse)response).sendRedirect(originalLink);
                return;
            }
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if(shortLinkGotoDO == null){
                ((HttpServletResponse)response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if(shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(now()))){
                ((HttpServletResponse)response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    GOTO_SHORT_LINK_KEY+fullShortUrl,
                    shortLinkDO.getOriginUrl(), LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
            shortLinkStats(shortLinkDO.getGid(),fullShortUrl,request,response);
            ((HttpServletResponse)response).sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock();
        }
    }

    private void shortLinkStats(String gid,String fullShortUrl,ServletRequest request, ServletResponse response){
        Map<String,Object> producerMap = new HashMap<>();
        //用户是否第一次访问标识
        AtomicBoolean uvFlagStats = new AtomicBoolean(false);
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        Runnable addResponseCookieTask = () ->{
            String uv = UUID.fastUUID().toString();
            Cookie uvCookie = new Cookie("uv", uv);
            uvCookie.setMaxAge(60*60*24*30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl,fullShortUrl.indexOf("/"),fullShortUrl.length()));
            ((HttpServletResponse)response).addCookie(uvCookie);
            uvFlagStats.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv);
        };
        try{
            if(ArrayUtil.isNotEmpty(cookies)){
                Arrays.stream(cookies)
                        .filter(each->Objects.equals(each.getName(),"uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each-> {
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                            uvFlagStats.set(uvAdded != null && uvAdded > 0L);
                        },() ->{
                            String uv = UUID.fastUUID().toString();
                            Cookie uvCookie = new Cookie("uv", uv);
                            uvCookie.setMaxAge(60*60*24*30);
                            uvCookie.setPath(StrUtil.sub(fullShortUrl,fullShortUrl.indexOf("/"),fullShortUrl.length()));
                            ((HttpServletResponse)response).addCookie(uvCookie);
                            uvFlagStats.set(Boolean.TRUE);
                            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv);
                        });
            }else {
                addResponseCookieTask.run();
            }
            String remoteAddr = LinkUtil.getIp((HttpServletRequest)request);
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = (uipAdded != null && uipAdded > 0L);
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFlagStats.get()?1:0)
                    .uip(uipFirstFlag?1:0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            producerMap.put("linkAccessStatsDO", linkAccessStatsDO);
//            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String,Object> map = new HashMap<>();
            map.put("key",statsLocaleAmapKey);
            map.put("ip",remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, map);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infocode = localeResultObj.getString("infocode");
            LinkLocalStatsDO linkLocalStatsDO = null;
            if(StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode,"10000")){
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province,"[]");
                linkLocalStatsDO = LinkLocalStatsDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .province(unknownFlag?"未知" :  province)
                        .city(unknownFlag?"未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag?"未知" :localeResultObj.getString("adcode"))
                        .country("中国")
                        .cnt(1)
                        .gid(gid)
                        .date(new Date())
                        .build();
//                linkLocalStatsMapper.shortLinkLocalStats(linkLocalStatsDO);
            }
            producerMap.put("linkLocalStatsDO",linkLocalStatsDO);
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .os(LinkUtil.getOs((HttpServletRequest)request))
                    .cnt(1)
                    .gid(gid)
                    .date(new Date())
                    .build();
            producerMap.put("linkOsStatsDO",linkOsStatsDO);
//            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
            LinkBrowserStatsDO browserStatsDO = LinkBrowserStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .browser(LinkUtil.getBrowser((HttpServletRequest)request))
                    .cnt(1)
                    .gid(gid)
                    .date(new Date())
                    .build();
            producerMap.put("browserStatsDO",browserStatsDO);
//            linkBrowserStatsMapper.shortLinkBrowserState(browserStatsDO);
            shortLinkStatsSaveProducer.send(producerMap);
        }
        catch (Throwable e){
            log.error("短链接统计异常");
        }
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
