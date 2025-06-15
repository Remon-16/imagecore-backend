package com.tech.imagecorebackendpictureservice.interfaces.controller.inner;

import com.tech.imagecorebackendmodel.dto.space.analyze.*;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.tech.imagecorebackendpictureservice.application.service.PictureApplicationService;
import com.tech.imagecorebackendserviceclient.application.service.PictureFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inner")
public class PictureInnerController implements PictureFeignClient {
    @Resource
    private PictureApplicationService pictureApplicationService;

    @Override
    @PostMapping("/picture_obj")
    public List<Object> getPictureObjList(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest) {
        return pictureApplicationService.getPictureObjList(spaceUsageAnalyzeRequest);
    }

    @Override
    @PostMapping("/space_category")
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest) {
        return pictureApplicationService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest);
    }

    @Override
    @PostMapping("/tag_json")
    public List<String> getTagsJson(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest) {
        return pictureApplicationService.getTagsJson(spaceTagAnalyzeRequest);
    }

    @Override
    @PostMapping("/picture_size")
    public List<Long> getPicSizeList(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest) {
        return pictureApplicationService.getPicSizeList(spaceSizeAnalyzeRequest);
    }

    @Override
    @PostMapping("/query_space_inner")
    public List<Map<String, Object>> querySpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest) {
        return pictureApplicationService.querySpaceUserAnalyze(spaceUserAnalyzeRequest);
    }

    @Override
    public Picture getById(Long pictureId) {
        return pictureApplicationService.getById(pictureId);
    }
}
