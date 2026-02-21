package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.*;
import com.zrrd.catcate.mapper.*;
import com.zrrd.catcate.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    @Resource
    private UsersMapper usersMapper;
    @Resource
    private ReservationsMapper reservationsMapper;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private ReviewsMapper reviewsMapper;
    @Resource
    private ProductsMapper productsMapper;

    /**
     * 注册
     *
     * @param users
     * @return
     */
    @Override
    public Integer register(Users users) {
        // 插入用户时把role字段置为管理员
        users.setRole("ADMIN");
        return usersMapper.insert(users);
    }

    @Override
    public Boolean login(Users users) {
        QueryWrapper<Users> qw = new QueryWrapper<>();
        qw.eq("status", "active").eq("role", "admin").eq("username", users.getUsername()).eq("password", users.getPassword());
        UpdateWrapper<Users> uw = new UpdateWrapper<>();

        if (usersMapper.selectOne(qw) != null) {
            // 更新用户登录时间
            uw.eq("username", users.getUsername());
            uw.set("last_login_time", LocalDateTime.now());
            usersMapper.update(users, uw);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 统计主页显示信息
     *
     * @return
     */
    @Override
    public List<Object> stats() {
        //查询用户总数信息
        QueryWrapper<Users> qw = new QueryWrapper<>();
        qw.eq("role", "CUSTOMER").select("count(*) as total_users");
        System.out.println(usersMapper.selectMaps(qw) + "-----------usersMapper.selectMaps(qw)-----------");
        //查找今日预约的总数信息
        QueryWrapper<Reservations> qw2 = new QueryWrapper<>();
        qw2.eq("reservation_date", LocalDateTime.now().toLocalDate()).select("count(*) as total_reservations");
        System.out.println(reservationsMapper.selectMaps(qw2) + "-----------usersMapper.selectMaps(qw2)-----------");
        //查询本月销售额
        QueryWrapper<Orders> qw3 = new QueryWrapper<>();
        // 获取当前月份的第一天和最后一天
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
        qw3.ge("payment_time", firstDay.atStartOfDay())
                .le("payment_time", lastDay.atTime(23, 59, 59))
                .eq("payment_status", "PAID")
                .select("sum(total_amount) as total_sales");
        //查询待处理事项
        //查找待确定预约的总数信息
        QueryWrapper<Reservations> qw4 = new QueryWrapper<>();
        qw4.eq("status", "PENDING").select("count(*) as total_reservations");
        //查询待审核评价的总数信息
        QueryWrapper<Reviews> qw5 = new QueryWrapper<>();
        qw5.eq("status", "PENDING").select("count(*) as total_reviews");
        //查询待处理订单的总数信息
        QueryWrapper<Orders> qw6 = new QueryWrapper<>();
        qw6.eq("order_status", "PENDING").select("count(*) as total_orders");
        //查询低库存预警商品的总数信息(预警阈值（如≤5）	商品库存低于设定阈值，需管理员安排补货)
        QueryWrapper<Products> qw7 = new QueryWrapper<>();
        qw7.le("stock_quantity", 5).select("count(*) as total_products");
        //把qw4，qw5，qw6，qw7 的值相加
        long l = ((Long) reservationsMapper.selectMaps(qw4).get(0).get("total_reservations")) +
                ((Long) reviewsMapper.selectMaps(qw5).get(0).get("total_reviews")) +
                ((Long) ordersMapper.selectMaps(qw6).get(0).get("total_orders")) +
                ((Long) productsMapper.selectMaps(qw7).get(0).get("total_products"));

        return List.of(usersMapper.selectMaps(qw).get(0).get("total_users"),
                reservationsMapper.selectMaps(qw2).get(0).get("total_reservations"),
                ordersMapper.selectMaps(qw3).get(0).get("total_sales"),
                l
        );
    }

    @Override
    public List<Users> selectList(Users users) {
//        return usersMapper.selectUsersNoNull( users);
        return null;
    }


    @Override
    public Page<Users> getUsersList(Map<String, Object> requestParams) {
        int pageNum = (int) requestParams.getOrDefault("pageNum", 1);
        int pageSize = (int) requestParams.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;
        requestParams.put("offset", offset);
        requestParams.put("pageSize", pageSize);
        List<Users> records = usersMapper.selectUsersNoNull(requestParams);
        int total = usersMapper.countUsersNoNull(requestParams);
        Page<Users> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();

        System.out.println("开始获取最近动态数据...");

        try {
            // 查询所有预约记录，按时间倒序
            QueryWrapper<Reservations> reservationQw = new QueryWrapper<>();
            reservationQw.isNotNull("create_time").orderByDesc("create_time");
            List<Reservations> allReservations = reservationsMapper.selectList(reservationQw);
            System.out.println("查询到预约记录数量: " + allReservations.size());

            for (Reservations reservation : allReservations) {
                try {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("time", reservation.getCreateTime());
                    activity.put("title", "新的预约请求");
                    activity.put("content", String.format("用户ID %d 预约了 %s %s 的撸猫时段，状态：%s",
                            reservation.getUserId(),
                            reservation.getReservationDate() != null ? reservation.getReservationDate().toString() : "未知日期",
                            reservation.getTimeSlot() != null ? reservation.getTimeSlot() : "未知时段",
                            getStatusDisplayName(reservation.getStatus())));
                    activity.put("icon", "InfoFilled");
                    activity.put("type", getActivityTypeByStatus(reservation.getStatus()));
                    activity.put("status", reservation.getStatus() != null ? reservation.getStatus().toLowerCase() : "unknown");

                    // 根据状态添加操作按钮
                    if ("PENDING".equals(reservation.getStatus())) {
                        List<Map<String, Object>> operations = new ArrayList<>();
                        operations.add(createOperationMap(1, "确认", "success", "confirm"));
                        operations.add(createOperationMap(2, "拒绝", "danger", "reject"));
                        activity.put("operations", operations);
                    } else if ("CONFIRMED".equals(reservation.getStatus())) {
                        List<Map<String, Object>> operations = new ArrayList<>();
                        operations.add(createOperationMap(3, "完成", "primary", "complete"));
                        operations.add(createOperationMap(4, "取消", "warning", "cancel"));
                        activity.put("operations", operations);
                    }

                    // 使用HashMap代替Map.of来避免null值问题
                    Map<String, Object> reservationData = new HashMap<>();
                    reservationData.put("reservationId", reservation.getId());
                    reservationData.put("userId", reservation.getUserId());
                    reservationData.put("catId", reservation.getCatId());
                    reservationData.put("reservationDate", reservation.getReservationDate());
                    reservationData.put("timeSlot", reservation.getTimeSlot() != null ? reservation.getTimeSlot() : "");
                    activity.put("data", reservationData);

                    activities.add(activity);
                } catch (Exception e) {
                    System.err.println("处理预约记录时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 查询所有订单记录，按时间倒序
            QueryWrapper<Orders> orderQw = new QueryWrapper<>();
            orderQw.isNotNull("create_time").orderByDesc("create_time");
            List<Orders> allOrders = ordersMapper.selectList(orderQw);
            System.out.println("查询到订单记录数量: " + allOrders.size());

            for (Orders order : allOrders) {
                try {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("time", order.getCreateTime());
                    activity.put("title", "订单状态变更");
                    activity.put("content", String.format("订单 %s 状态变更为 %s，金额 ¥%s",
                            order.getOrderNumber() != null ? order.getOrderNumber() : "未知订单",
                            getOrderStatusDisplayName(order.getOrderStatus()),
                            order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0"));
                    activity.put("icon", "ShoppingCart");
                    activity.put("type", getOrderActivityType(order.getOrderStatus()));
                    activity.put("status", order.getOrderStatus() != null ? order.getOrderStatus().toLowerCase() : "unknown");

                    // 根据订单状态添加操作
                    if ("PENDING".equals(order.getOrderStatus())) {
                        List<Map<String, Object>> operations = new ArrayList<>();
                        operations.add(createOperationMap(5, "处理", "primary", "process"));
                        operations.add(createOperationMap(6, "取消", "warning", "cancel_order"));
                        activity.put("operations", operations);
                    } else if ("PROCESSING".equals(order.getOrderStatus())) {
                        List<Map<String, Object>> operations = new ArrayList<>();
                        operations.add(createOperationMap(7, "发货", "success", "ship"));
                        operations.add(createOperationMap(8, "异常", "danger", "exception"));
                        activity.put("operations", operations);
                    }

                    // 使用HashMap代替Map.of来避免null值问题
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("orderId", order.getId());
                    orderData.put("orderNumber", order.getOrderNumber() != null ? order.getOrderNumber() : "");
                    orderData.put("userId", order.getUserId());
                    orderData.put("totalAmount", order.getTotalAmount());
                    orderData.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus() : "");
                    activity.put("data", orderData);

                    activities.add(activity);
                } catch (Exception e) {
                    System.err.println("处理订单记录时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 查询用户注册记录（如果有用户创建时间字段）
            QueryWrapper<Users> userQw = new QueryWrapper<>();
            userQw.isNotNull("create_time").orderByDesc("create_time");
            List<Users> allUsers = usersMapper.selectList(userQw);
            System.out.println("查询到用户记录数量: " + allUsers.size());

            for (Users user : allUsers) {
                try {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("time", user.getCreateTime());
                    activity.put("title", "新用户注册");
                    activity.put("content", String.format("用户 %s (%s) 完成注册",
                            safeGetString(user.getUsername()),
                            user.getEmail() != null ? user.getEmail() : "无邮箱"));
                    activity.put("icon", "User");
                    activity.put("type", "info");
                    activity.put("status", "registered");

                    // 使用HashMap代替Map.of来避免null值问题
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", user.getId());
                    userData.put("username", safeGetString(user.getUsername()));
                    userData.put("email", safeGetString(user.getEmail()));
                    userData.put("phone", safeGetString(user.getPhone()));
                    activity.put("data", userData);

                    activities.add(activity);
                } catch (Exception e) {
                    System.err.println("处理用户记录时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 按时间排序
            activities.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("time");
                LocalDateTime timeB = (LocalDateTime) b.get("time");
                return timeB.compareTo(timeA);
            });

            System.out.println("总共生成动态记录数量: " + activities.size());
            return activities;

        } catch (Exception e) {
            System.err.println("获取最近动态失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 辅助方法：创建操作映射
    private Map<String, Object> createOperationMap(int id, String text, String type, String action) {
        Map<String, Object> operation = new HashMap<>();
        operation.put("id", id);
        operation.put("text", text);
        operation.put("type", type);
        operation.put("action", action);
        return operation;
    }

    // 辅助方法：安全获取字符串值
    private String safeGetString(String value) {
        return value != null ? value : "";
    }

    // 辅助方法：安全获取对象值
    private Object safeGetObject(Object value) {
        return value != null ? value : "";
    }

    // 辅助方法：根据预约状态获取显示名称
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "PENDING": return "待确认";
            case "CONFIRMED": return "已确认";
            case "CANCELLED": return "已取消";
            case "COMPLETED": return "已完成";
            default: return status;
        }
    }

    // 辅助方法：根据订单状态获取显示名称
    private String getOrderStatusDisplayName(String status) {
        switch (status) {
            case "PENDING": return "待处理";
            case "PROCESSING": return "处理中";
            case "SHIPPED": return "已发货";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }

    // 辅助方法：根据预约状态获取活动类型
    private String getActivityTypeByStatus(String status) {
        switch (status) {
            case "PENDING": return "warning";
            case "CONFIRMED": return "primary";
            case "CANCELLED": return "danger";
            case "COMPLETED": return "success";
            default: return "info";
        }
    }

    // 辅助方法：根据订单状态获取活动类型
    private String getOrderActivityType(String status) {
        switch (status) {
            case "PENDING": return "warning";
            case "PROCESSING": return "primary";
            case "SHIPPED": return "success";
            case "COMPLETED": return "success";
            case "CANCELLED": return "danger";
            default: return "info";
        }
    }
}
