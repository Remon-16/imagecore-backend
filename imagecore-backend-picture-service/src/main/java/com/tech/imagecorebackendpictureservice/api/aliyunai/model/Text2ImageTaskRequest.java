package com.tech.imagecorebackendpictureservice.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Text2ImageTaskRequest {
    /**
     * 模型
     */
    private String model = "wanx-v1";

    /**
     * 输入图像信息
     */
    private CreateOutPaintingTaskRequest.Input input;

    /**
     * 图像处理参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    @Data
    public static class Input {
        /**
         * 必选，提示词
         */
        private String prompt;
        /**
         * 屏蔽内容的提示词
         */
        @Alias("negative_prompt")
        private String negativePrompt;
        /**
         * 参考图像（垫图）的URL地址。模型根据参考图像生成相似风格的图像。
         * 图像限制：
         * 图片格式：JPG、JPEG、PNG、BMP、TIFF、WEBP等常见格式。
         * 图像大小：不超过10 MB。
         * 图像分辨率：不低于256×256像素且不超过4096×4096像素。
         * URL地址中不能包含中文字符。
         */
        @Alias("ref_img")
        private String refImg;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * <auto>：默认值，由模型随机输出图像风格。
         * <photography>：摄影。
         * <portrait>：人像写真。
         * <3d cartoon>：3D卡通。
         * <anime>：动画。
         * <oil painting>：油画。
         * <watercolor>：水彩。
         * <sketch>：素描。
         * <chinese painting>：中国画。
         * <flat illustration>：扁平插画。
         */
        private String style ;

        /**
         * 1024*1024：默认值。
         * 720*1280
         * 768*1152
         * 1280*720
         */
        @Alias("size")
        private String size;

        /**
         * 生成图片的数量。取值范围为1~4张，默认为4。
         */
        private Integer n;

        /**
         * 控制输出图像与垫图（参考图）的相似度。
         * 取值范围为[0.0, 1.0]。取值越大，代表生成的图像与参考图越相似
         */
        @Alias("ref_strength")
        @JsonProperty("refStrength")
        private Float refStrength;

        /**
         * 基于垫图（参考图）生成图像的模式。目前支持的模式有
         * repaint：默认值，基于参考图的内容生成图像。
         * refonly：基于参考图的风格生成图像。
         */
        @Alias("ref_mode")
        private String refMode;
    }
}
