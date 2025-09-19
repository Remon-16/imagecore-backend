package com.tech.imagecorebackendpictureservice.infrastructure.dco.picture;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.CacheManager;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.RedisKeyUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class PictureCacheHandler {

    @Resource
    CacheManager cacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void updateHomeCache(String keyHead, Picture picture) {
        Set<String> targetKeys = redisTemplate.keys(RedisKeyUtil.buildRedisKey(keyHead + ":*"));
        PictureVO newpictureVO = PictureVO.objToVo(picture);
        for (String targetKey : targetKeys) {
            if(targetKey == null || targetKey.isEmpty()){
                continue;
            }
            Page<PictureVO> pictureVOPage = this.getPicturePage(targetKey);
            boolean needUpdate = false;
            // 更新
            for (PictureVO pictureVO : pictureVOPage.getRecords()) {
                if(Objects.equals(newpictureVO.getId(), pictureVO.getId())) {
                    updateSinglePicture(pictureVO, newpictureVO);
                    needUpdate = true;
                }
            }
            if(needUpdate){
                String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
                cacheManager.putValueToCache(targetKey.replace(CacheUtils.APP_NAME + ":", ""), cacheValue);
            }
        }
    }

    public void deleteHomeCache(String keyHead, Picture picture) {
        Set<String> targetKeys = redisTemplate.keys(RedisKeyUtil.buildRedisKey(keyHead + ":*"));
        PictureVO newpictureVO = PictureVO.objToVo(picture);
        for (String targetKey : targetKeys) {
            if (targetKey == null || targetKey.isEmpty()) {
                continue;
            }
            Page<PictureVO> pictureVOPage = this.getPicturePage(targetKey);
            for (PictureVO pictureVO : pictureVOPage.getRecords()) {
                if(Objects.equals(newpictureVO.getId(), pictureVO.getId())) {
                    cacheManager.removeValueCache(targetKey);
                    break;
                }
            }
        }
    }

    public void updatePictureCache(String key, Picture picture) {
        PictureVO newpictureVO = PictureVO.objToVo(picture);
        Object queryValue = cacheManager.getValueCache(key);
        if (queryValue == null) {
            return;
        }
        PictureVO sourcePictureVO = JSONUtil.toBean((String) queryValue, PictureVO.class);
        this.updateSinglePicture(sourcePictureVO, newpictureVO);
        // 存缓存
        String cacheValue = JSONUtil.toJsonStr(sourcePictureVO);
        cacheManager.putValueToCache(key, cacheValue);
    }

    public void deletePictureCache(String key) {
        cacheManager.removeValueCache(key);
    }

    private void updateSinglePicture(PictureVO sourcePictureVo, PictureVO newpictureVO){
        sourcePictureVo.setName(newpictureVO.getName());
        sourcePictureVo.setIntroduction(newpictureVO.getIntroduction());
        sourcePictureVo.setCategory(newpictureVO.getCategory());
        sourcePictureVo.setTags(newpictureVO.getTags());
        sourcePictureVo.setUrl(newpictureVO.getUrl());
        sourcePictureVo.setThumbnailUrl(newpictureVO.getThumbnailUrl());
        sourcePictureVo.setUpdateTime(newpictureVO.getUpdateTime());
        sourcePictureVo.setEditTime(newpictureVO.getEditTime());
    }

    private Page<PictureVO> getPicturePage(String targetKey){
        Object value = redisTemplate.opsForValue().get(targetKey);
        return JSONUtil.toBean(
                (String) value,
                new TypeReference<>() {}, // 指定完整泛型结构
                false // 是否忽略转换错误
        );
    }

}
