package com.tech.imagecorebackendpictureservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendpictureservice.domain.picture.repository.PictureCommentRepository;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【picture_comment(评论表)】的数据库操作Service实现
* @createDate 2025-08-07 14:56:19
*/
@Service
public class PictureCommentRepositoryImpl extends ServiceImpl<PictureCommentMapper, PictureComment>
    implements PictureCommentRepository {

}




