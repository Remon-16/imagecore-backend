package com.tech.imagecorebackendpictureservice.infrastructure.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.client.CanalMessageDeserializer;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureCommentDomainService;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(topic = "canalTopic", consumerGroup = "canalGroup")
public class CanalMessageConsumer implements RocketMQListener<MessageExt> {
    private static final String PICTURE = "picture";
    private static final String PICTURE_COMMENT = "picture_comment";

    @Resource
    private PictureDomainService pictureDomainService;

    @Resource
    private PictureCommentDomainService pictureCommentDomainService;

    @Override
    public void onMessage(MessageExt message){
        Message msg = CanalMessageDeserializer.deserializer(message.getBody());
        List<CanalEntry.Entry> entries = msg.getEntries();
        Map<String, List<CanalHandleVo>> canalHandleMap = this.handleEntryList(entries);
        this.cacheHandle(canalHandleMap);
    }

    /**
     * 根据变动结果处理缓存
     */
    private void cacheHandle(Map<String, List<CanalHandleVo>> canalHandleMap){
        canalHandleMap.forEach((tableName, canalHandleVoList) -> {
            if(PICTURE.equals(tableName)){
                pictureDomainService.canalHandlePicture(canalHandleVoList);
            }else if(PICTURE_COMMENT.equals(tableName)){
                pictureCommentDomainService.canalHandlePictureComment(canalHandleVoList);
            }else{
                log.error("未监听表 tableName: {}", tableName);
            }
        });
    }

    /**
     * 处理变动的实体
     */
    private Map<String, List<CanalHandleVo>> handleEntryList(List<CanalEntry.Entry> entryList){
        Map<String, List<CanalHandleVo>> resMap = new HashMap<>();
        for (CanalEntry.Entry entry : entryList) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                log.error(e.fillInStackTrace().toString());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "ERROR ## parser of eromanga-event has an error , data:" + entry.toString());
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            String tableName = entry.getHeader().getTableName();
            List<CanalHandleVo> canalHandleVoList = resMap.getOrDefault(tableName, new ArrayList<>());
            String jsonStr = this.handleRow(rowChage.getRowDatasList(), eventType);
            CanalHandleVo canalHandleVo = new CanalHandleVo();
            canalHandleVo.setEventType(eventType);
            canalHandleVo.setTableName(entry.getHeader().getTableName());
            canalHandleVo.setJsonDataStr(jsonStr);
            canalHandleVoList.add(canalHandleVo);
            resMap.put(tableName, canalHandleVoList);
        }
        return resMap;
    }

    /**
     * 处理实体里的数据行
     */
    private String handleRow(List<CanalEntry.RowData> rowDatasList, CanalEntry.EventType eventType){
        Map<String, String> map = new HashMap<>();
        for (CanalEntry.RowData rowData : rowDatasList) {
            if (eventType == CanalEntry.EventType.DELETE) {
                this.handleColumn(rowData.getBeforeColumnsList(), map);
            } else if (eventType == CanalEntry.EventType.INSERT) {
                this.handleColumn(rowData.getAfterColumnsList(), map);
            } else {
                this.handleColumn(rowData.getAfterColumnsList(), map);
            }
        }
        return JSONUtil.toJsonStr(map);
    }

    /**
     * 处理数据行中的列
     */
    private void handleColumn(List<CanalEntry.Column> columns, Map<String, String> map){
        for (CanalEntry.Column column : columns) {
            if(!column.getValue().isEmpty()){
                map.put(column.getName(), column.getValue());
            }
        }
    }


    private void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    this.printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    this.printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------&gt; before");
                    List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
                    this.printColumn(beforeColumnsList);
                    System.out.println("-------&gt; after");
                    this.printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
