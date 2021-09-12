package com.study.task.config;

import com.study.rabbit.constant.MqConst;
import com.study.rabbit.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledTaskConfig {

    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天8点执行 提醒就诊 cron时间间隔 有在线生成器
     */
    @Scheduled(cron = "0/30 * * * * ? ")
    public void task1() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }

}
