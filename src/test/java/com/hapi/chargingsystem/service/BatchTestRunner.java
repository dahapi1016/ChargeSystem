package com.hapi.chargingsystem.service;

import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.dto.req.ChargeReqDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * 批量测试运行器
 * 用于演示如何批量创建不同用户的充电请求
 */
@SpringBootTest
public class BatchTestRunner {

    /**
     * 演示批量创建充电请求的方法
     */
    @Test
    public void demonstrateBatchRequestCreation() {
        System.out.println("=== 批量充电请求创建演示 ===");
        
        // 创建50个不同用户的充电请求
        List<Long> userIds = createUserIds(50, 1L);
        List<ChargeReqDTO> requests = createVariousChargingRequests(50);
        
        System.out.println("创建了 " + userIds.size() + " 个用户ID");
        System.out.println("创建了 " + requests.size() + " 个充电请求");
        
        // 统计不同类型的请求
        long fastChargingCount = requests.stream()
                .filter(req -> ChargingMode.FAST.getCode().equals(req.getChargingMode()))
                .count();
        long trickleChargingCount = requests.stream()
                .filter(req -> ChargingMode.TRICKLE.getCode().equals(req.getChargingMode()))
                .count();
        
        System.out.println("快充请求数量: " + fastChargingCount);
        System.out.println("慢充请求数量: " + trickleChargingCount);
        
        // 显示请求详情示例
        System.out.println("\n=== 请求详情示例 ===");
        for (int i = 0; i < Math.min(5, requests.size()); i++) {
            ChargeReqDTO request = requests.get(i);
            Long userId = userIds.get(i);
            System.out.printf("用户ID: %d, 充电模式: %s, 请求电量: %s kWh, 电池容量: %s kWh%n",
                    userId,
                    ChargingMode.getByCode(request.getChargingMode()).getDescription(),
                    request.getRequestAmount(),
                    request.getBatteryCapacity());
        }
    }

    /**
     * 演示并发批量请求处理
     */
    @Test
    public void demonstrateConcurrentBatchProcessing() {
        System.out.println("\n=== 并发批量请求处理演示 ===");
        
        int batchSize = 20;
        List<Long> userIds = createUserIds(batchSize, 100L);
        List<ChargeReqDTO> requests = createVariousChargingRequests(batchSize);
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // 模拟并发提交请求
        for (int i = 0; i < batchSize; i++) {
            final int index = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                Long userId = userIds.get(index);
                ChargeReqDTO request = requests.get(index);
                
                // 模拟处理时间
                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return String.format("用户 %d 的%s请求已处理", 
                        userId, 
                        ChargingMode.getByCode(request.getChargingMode()).getDescription());
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        System.out.println("并发处理完成，共处理 " + results.size() + " 个请求");
        results.forEach(System.out::println);
        
        executor.shutdown();
    }

    /**
     * 演示不同场景的充电请求
     */
    @Test
    public void demonstrateVariousScenarios() {
        System.out.println("\n=== 不同场景充电请求演示 ===");
        
        // 场景1：紧急充电（小电量，快充）
        List<ChargeReqDTO> emergencyRequests = createEmergencyChargingRequests(5);
        System.out.println("紧急充电请求 " + emergencyRequests.size() + " 个");
        
        // 场景2：日常充电（中等电量，慢充）
        List<ChargeReqDTO> dailyRequests = createDailyChargingRequests(10);
        System.out.println("日常充电请求 " + dailyRequests.size() + " 个");
        
        // 场景3：满电充电（大电量，快充）
        List<ChargeReqDTO> fullChargeRequests = createFullChargingRequests(3);
        System.out.println("满电充电请求 " + fullChargeRequests.size() + " 个");
        
        // 显示各场景的详细信息
        System.out.println("\n紧急充电场景:");
        emergencyRequests.forEach(req -> 
            System.out.printf("  %s - %s kWh%n", 
                ChargingMode.getByCode(req.getChargingMode()).getDescription(),
                req.getRequestAmount()));
        
        System.out.println("\n日常充电场景:");
        dailyRequests.forEach(req -> 
            System.out.printf("  %s - %s kWh%n", 
                ChargingMode.getByCode(req.getChargingMode()).getDescription(),
                req.getRequestAmount()));
        
        System.out.println("\n满电充电场景:");
        fullChargeRequests.forEach(req -> 
            System.out.printf("  %s - %s kWh%n", 
                ChargingMode.getByCode(req.getChargingMode()).getDescription(),
                req.getRequestAmount()));
    }

    /**
     * 创建用户ID列表
     */
    private List<Long> createUserIds(int count, long startId) {
        return IntStream.range(0, count)
                .mapToObj(i -> startId + i)
                .toList();
    }

    /**
     * 创建各种类型的充电请求
     */
    private List<ChargeReqDTO> createVariousChargingRequests(int count) {
        Random random = new Random();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    ChargeReqDTO request = new ChargeReqDTO();
                    
                    // 随机选择充电模式
                    request.setChargingMode(random.nextBoolean() ? 
                            ChargingMode.FAST.getCode() : ChargingMode.TRICKLE.getCode());
                    
                    // 随机充电量 (10-80 kWh)
                    double amount = 10 + random.nextDouble() * 70;
                    request.setRequestAmount(new BigDecimal(String.format("%.1f", amount)));
                    
                    // 随机电池容量 (60-120 kWh)
                    double capacity = 60 + random.nextDouble() * 60;
                    request.setBatteryCapacity(new BigDecimal(String.format("%.1f", capacity)));
                    
                    return request;
                })
                .toList();
    }

    /**
     * 创建紧急充电请求（小电量，快充）
     */
    private List<ChargeReqDTO> createEmergencyChargingRequests(int count) {
        Random random = new Random();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    ChargeReqDTO request = new ChargeReqDTO();
                    request.setChargingMode(ChargingMode.FAST.getCode());
                    
                    // 紧急充电：10-25 kWh
                    double amount = 10 + random.nextDouble() * 15;
                    request.setRequestAmount(new BigDecimal(String.format("%.1f", amount)));
                    request.setBatteryCapacity(new BigDecimal("75.0"));
                    
                    return request;
                })
                .toList();
    }

    /**
     * 创建日常充电请求（中等电量，慢充）
     */
    private List<ChargeReqDTO> createDailyChargingRequests(int count) {
        Random random = new Random();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    ChargeReqDTO request = new ChargeReqDTO();
                    request.setChargingMode(ChargingMode.TRICKLE.getCode());
                    
                    // 日常充电：30-50 kWh
                    double amount = 30 + random.nextDouble() * 20;
                    request.setRequestAmount(new BigDecimal(String.format("%.1f", amount)));
                    request.setBatteryCapacity(new BigDecimal("80.0"));
                    
                    return request;
                })
                .toList();
    }

    /**
     * 创建满电充电请求（大电量，快充）
     */
    private List<ChargeReqDTO> createFullChargingRequests(int count) {
        Random random = new Random();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    ChargeReqDTO request = new ChargeReqDTO();
                    request.setChargingMode(ChargingMode.FAST.getCode());
                    
                    // 满电充电：60-100 kWh
                    double amount = 60 + random.nextDouble() * 40;
                    request.setRequestAmount(new BigDecimal(String.format("%.1f", amount)));
                    request.setBatteryCapacity(new BigDecimal("100.0"));
                    
                    return request;
                })
                .toList();
    }

    /**
     * 性能测试：大批量请求创建
     */
    @Test
    public void performanceTestLargeBatch() {
        System.out.println("\n=== 性能测试：大批量请求创建 ===");
        
        int[] batchSizes = {100, 500, 1000, 2000};
        
        for (int batchSize : batchSizes) {
            long startTime = System.currentTimeMillis();
            
            List<Long> userIds = createUserIds(batchSize, 1L);
            List<ChargeReqDTO> requests = createVariousChargingRequests(batchSize);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.printf("批量大小: %d, 创建时间: %d ms, 平均每个请求: %.2f ms%n",
                    batchSize, duration, (double) duration / batchSize);
            
            // 验证数据完整性
            assert userIds.size() == batchSize;
            assert requests.size() == batchSize;
            assert requests.stream().allMatch(req -> req.getChargingMode() != null);
            assert requests.stream().allMatch(req -> req.getRequestAmount() != null);
        }
    }
}