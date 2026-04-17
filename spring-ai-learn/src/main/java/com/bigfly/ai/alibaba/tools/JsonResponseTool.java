
package com.bigfly.ai.alibaba.tools;

import com.bigfly.ai.alibaba.tools.JsonResponseTool.UserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * JSON 响应工具
 * 演示如何让 AI 返回指定格式的 JSON 数据
 */
@Slf4j
@Component
public class JsonResponseTool extends BaseTools {

    @Override
    public Object getToolInstance() {
        return this;
    }

    /**
     * 用户信息对象
     */
    @Data
    public static class UserInfo {
        private String name;
        private Integer age;
        private String email;
        private String phone;
        private String address;
    }

    /**
     * 订单信息对象
     */
    @Data
    public static class OrderInfo {
        private String orderId;
        private String productName;
        private Double price;
        private Integer quantity;
        private String status;
    }

    /**
     * 获取用户信息（返回结构化 JSON）
     *
     * @param userId 用户ID
     * @return 用户信息对象
     */
    @Tool(description = "获取指定用户的详细信息，返回包含姓名、年龄、邮箱、电话和地址的结构化数据")
    public UserInfo getUserInfo(String userId) {
        log.info("调用工具：获取用户信息, userId: {}", userId);
        
        // 模拟从数据库查询用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setName("张三");
        userInfo.setAge(28);
        userInfo.setEmail("zhangsan@example.com");
        userInfo.setPhone("13800138000");
        userInfo.setAddress("北京市朝阳区xxx路xxx号");
        
        log.info("用户信息查询成功: {}", userInfo);
        return userInfo;
    }

    /**
     * 获取订单信息（返回结构化 JSON）
     *
     * @param orderId 订单ID
     * @return 订单信息对象
     */
    @Tool(description = "获取指定订单的详细信息，返回包含订单号、商品名称、价格、数量和状态的结构化数据")
    public OrderInfo getOrderInfo(String orderId) {
        log.info("调用工具：获取订单信息, orderId: {}", orderId);
        
        // 模拟从数据库查询订单信息
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(orderId);
        orderInfo.setProductName("iPhone 15 Pro");
        orderInfo.setPrice(8999.0);
        orderInfo.setQuantity(1);
        orderInfo.setStatus("已发货");
        
        log.info("订单信息查询成功: {}", orderInfo);
        return orderInfo;
    }

    /**
     * 获取多个用户信息列表
     *
     * @return 用户信息列表
     */
    @Tool(description = "获取所有用户的列表信息，返回用户信息数组")
    public UserInfo[] getAllUsers() {
        log.info("调用工具：获取所有用户信息");
        
        UserInfo user1 = new UserInfo();
        user1.setName("张三");
        user1.setAge(28);
        user1.setEmail("zhangsan@example.com");
        user1.setPhone("13800138000");
        user1.setAddress("北京市朝阳区");
        
        UserInfo user2 = new UserInfo();
        user2.setName("李四");
        user2.setAge(32);
        user2.setEmail("lisi@example.com");
        user2.setPhone("13900139000");
        user2.setAddress("上海市浦东新区");
        
        UserInfo user3 = new UserInfo();
        user3.setName("王五");
        user3.setAge(25);
        user3.setEmail("wangwu@example.com");
        user3.setPhone("13700137000");
        user3.setAddress("广州市天河区");
        
        UserInfo[] users = {user1, user2, user3};
        log.info("用户列表查询成功，共 {} 个用户", users.length);
        return users;
    }
}
