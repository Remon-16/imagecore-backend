package com.tech.imagecorebackendpictureservice.interfaces.controller.inner;

import com.tech.imagecorebackendmodel.dto.space.analyze.*;
import com.tech.imagecorebackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.tech.imagecorebackendpictureservice.application.service.PictureApplicationService;
import com.tech.imagecorebackendserviceclient.application.service.PictureFeignClient;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import org.springframework.web.multipart.MultipartFile;

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
    @GetMapping("/get/id")
    public Picture getById(Long pictureId) {
        return pictureApplicationService.getById(pictureId);
    }

    @Override
    /**
     * 用户上传头像
     * @return
     */
    @PostMapping("/upload_user_avatar")
    public String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix) {
        return pictureApplicationService.uploadUserAvatar(multipartFile, uploadPathPrefix);
    }
}
