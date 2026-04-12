package com.bigfly.ai.alibaba.tools;

import com.bigfly.vo.WeatherInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 天气工具类（真实 API）
 * 提供给 AI 模型调用的天气查询函数
 */
@Slf4j
@Component
public class WeatherAliTools {

    @Value("${weather.tools.url}")
    private String weatherToolsUrl;

    @Value("${weather.tools.id}")
    private String weatherToolsId;

    @Value("${weather.tools.key}")
    private String weatherToolsKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 查询指定省份和城市的天气预报
     *
     * @param province 省份名称
     * @param city 城市名称
     * @return 天气信息
     */
    @Tool(description = "查询指定省份和城市的天气预报综合信息，需要提供省份和城市名称")
    public WeatherInfo getWeather(String province, String city) {
        log.info("调用天气API工具 - 省份: {}, 城市: {}", province, city);
        
        try {
            String encodedProvince = URLEncoder.encode(province, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format(weatherToolsUrl, weatherToolsId, weatherToolsKey, encodedProvince, encodedCity);
            
            log.info("请求URL: {}", url);
            WeatherInfo weatherInfo = restTemplate.getForObject(url, WeatherInfo.class);
            
            if (weatherInfo != null) {
                log.info("天气查询成功: {}", weatherInfo);
            } else {
                log.warn("天气查询返回null");
            }
            
            return weatherInfo;
        } catch (Exception e) {
            log.error("天气查询失败", e);
            throw new RuntimeException("天气查询失败: " + e.getMessage(), e);
        }
    }
}
