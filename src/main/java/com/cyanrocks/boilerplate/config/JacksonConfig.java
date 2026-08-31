package com.cyanrocks.boilerplate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author wjq
 * @Date 2025/4/14 12:12
 */
@Configuration
public class JacksonConfig {

    /** 日期格式：yyyy-MM-dd */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        // 注册Long类型序列化规则
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(Long.TYPE, ToStringSerializer.instance);
        // 注册Java8日期类型序列化/反序列化规则（LocalDateTime不带T，格式yyyy-MM-dd HH:mm:ss）
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        // 先注册Java8日期时间模块（LocalDate/LocalDateTime/Instant等基础支持）
        objectMapper.registerModule(new JavaTimeModule());
        // 再注册自定义模块（后注册的覆盖前面的，使日期格式生效）
        objectMapper.registerModule(simpleModule);
        // 日期序列化为字符串，而不是时间戳数组
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}