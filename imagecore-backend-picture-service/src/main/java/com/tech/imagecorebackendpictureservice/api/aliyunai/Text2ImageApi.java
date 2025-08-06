package com.tech.imagecorebackendpictureservice.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.GeText2ImageTaskResponse;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.Text2ImageTaskRequest;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.Text2ImageTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Text2ImageApi {

    // 读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址
    public static final String TEXT_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    // 查询任务状态
    public static final String GET_TEXT_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务
     *
     * @param text2ImageTaskRequest
     * @return
     */
    public Text2ImageTaskResponse createText2ImageTask(Text2ImageTaskRequest text2ImageTaskRequest) {
        if (text2ImageTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文生图参数为空");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(TEXT_TO_IMAGE_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                // 必须开启异步处理
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(text2ImageTaskRequest));
        // 处理响应
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 生成图像失败");
            }
            Text2ImageTaskResponse text2ImageTaskResponse = JSONUtil.toBean(httpResponse.body(), Text2ImageTaskResponse.class);
            if (text2ImageTaskResponse.getCode() != null) {
                String errorMessage = text2ImageTaskResponse.getMessage();
                log.error("请求异常：{}", errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 生成图像失败，" + errorMessage);
            }
            return text2ImageTaskResponse;
        }
    }

    /**
     * 查询创建的任务结果
     *
     * @param taskId
     * @return
     */
    public GeText2ImageTaskResponse getText2ImageTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 ID 不能为空");
        }
        // 处理响应
        String url = String.format(GET_TEXT_TO_IMAGE_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GeText2ImageTaskResponse.class);
        }
    }
}
