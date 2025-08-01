package com.tech.imagecorebackendserviceclient.application.service;


import com.tech.imagecorebackendmodel.dto.space.analyze.*;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * @author Remon
 */
@FeignClient(name = "imagecore-backend-picture-service", path = "/api/picture/inner")
public interface PictureFeignClient {

    @PostMapping("/picture_obj")
    List<Object> getPictureObjList(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest);

    @PostMapping("/space_category")
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest);

    @PostMapping("/tag_json")
    List<String> getTagsJson(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest);

    @PostMapping("/picture_size")
    List<Long> getPicSizeList(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest);

    @PostMapping("/query_space_inner")
    List<Map<String, Object>> querySpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest);

    @GetMapping("/get/id")
    Picture getById(@RequestParam("pictureId") Long pictureId);
}
