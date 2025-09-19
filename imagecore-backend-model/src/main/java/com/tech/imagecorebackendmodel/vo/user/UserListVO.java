package com.tech.imagecorebackendmodel.vo.user;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;
import lombok.Data;

import java.util.List;

@Data
public class UserListVO {
    private String userListJson;


    public List<User> getUserList(String userListStr){

    return JSONUtil.toBean(
            userListStr,
            new TypeReference<List<User>>() {}, // 指定完整泛型结构
            false // 是否忽略转换错误
    );
    }
}
