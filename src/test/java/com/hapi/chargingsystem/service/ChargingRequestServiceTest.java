package com.hapi.chargingsystem.service;

import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.common.enums.RequestStatus;
import com.hapi.chargingsystem.common.utils.QueueNumberGenerator;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.dto.req.ChargeReqDTO;
import com.hapi.chargingsystem.dto.resp.ChargeRespDTO;
import com.hapi.chargingsystem.dto.resp.QueueStatusRespDTO;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import com.hapi.chargingsystem.service.impl.ChargingRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@SpringJUnitConfig
class ChargingRequestServiceTest {

    @Mock
    private ChargingRequestMapper chargingRequestMapper;

    @Mock
    private QueueNumberGenerator queueNumberGenerator;

    @Mock
    private SystemParamService systemParamService;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ChargingRequestServiceImpl chargingRequestService;

    private List<ChargeReqDTO> testRequests;
    private List<Long> testUserIds;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUserIds = new ArrayList<>();
        testRequests = new ArrayList<>();
        // 创建10个不同的用户ID和充电请求
        for (int i = 1; i <= 10; i++) {
            testUserIds.add((long) i);
            ChargeReqDTO request = new ChargeReqDTO();
            request.setChargingMode(i % 2 == 0 ? ChargingMode.FAST.getCode() : ChargingMode.TRICKLE.getCode());
            request.setRequestAmount(new BigDecimal("50.0").add(new BigDecimal(i * 10)));
            request.setBatteryCapacity(new BigDecimal("100.0"));
            testRequests.add(request);
        }

        // 设置通用Mock行为
        when(systemParamService.getWaitingAreaSize()).thenReturn(50);
        when(queueNumberGenerator.generateQueueNumber(anyInt())).thenReturn("Q001", "Q002", "Q003", "Q004", "Q005", "Q006", "Q007", "Q008", "Q009", "Q010");
        doNothing().when(scheduleService).triggerSchedule();
    }

    @Test
    void testBatchSubmitRequests() {
        // Mock 依赖方法
        when(chargingRequestMapper.selectOne(any())).thenReturn(null); // 没有现有请求
        when(chargingRequestMapper.selectCount(any())).thenReturn(0L); // 等候区未满
        when(chargingRequestMapper.insert(any(ChargingRequest.class))).thenReturn(1);

        List<ChargeRespDTO> results = new ArrayList<>();
        // 批量提交充电请求
        for (int i = 0; i < testUserIds.size(); i++) {
            Long userId = testUserIds.get(i);
            ChargeReqDTO request = testRequests.get(i);
            try {
                ChargeRespDTO result = chargingRequestService.submitRequest(userId, request);
                results.add(result);
                // 验证结果
                assertNotNull(result);
                assertEquals(request.getChargingMode(), result.getChargingMode());
                assertEquals(request.getRequestAmount(), result.getRequestAmount());
                assertEquals(RequestStatus.WAITING.getCode(), result.getStatus());
                assertNotNull(result.getQueueNumber());
            } catch (Exception e) {
                fail("批量提交请求失败，用户ID: " + userId + ", 错误: " + e.getMessage());
            }
        }
        // 验证所有请求都成功提交
        assertEquals(testUserIds.size(), results.size());
        // 验证数据库插入操作被调用了正确的次数
        verify(chargingRequestMapper, times(testUserIds.size())).insert(any(ChargingRequest.class));
        verify(scheduleService, times(testUserIds.size())).triggerSchedule();
    }

    @Test
    void testConcurrentBatchSubmitRequests() {
        // Mock 依赖方法
        when(chargingRequestMapper.selectOne(any())).thenReturn(null);
        when(chargingRequestMapper.selectCount(any())).thenReturn(0L);
        when(chargingRequestMapper.insert(any(ChargingRequest.class))).thenReturn(1);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<ChargeRespDTO>> futures = new ArrayList<>();

        // 并发提交充电请求
        for (int i = 0; i < testUserIds.size(); i++) {
            final int index = i;
            CompletableFuture<ChargeRespDTO> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return chargingRequestService.submitRequest(testUserIds.get(index), testRequests.get(index));
                } catch (Exception e) {
                    throw new RuntimeException("并发提交失败，用户ID: " + testUserIds.get(index), e);
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有请求完成并验证结果
        List<ChargeRespDTO> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        assertEquals(testUserIds.size(), results.size());
        // 验证每个结果都不为空且状态正确
        results.forEach(result -> {
            assertNotNull(result);
            assertEquals(RequestStatus.WAITING.getCode(), result.getStatus());
            assertNotNull(result.getQueueNumber());
        });

        executor.shutdown();
    }

    @Test
    void testBatchSubmitWithDifferentChargingModes() {
        // Mock 依赖方法
        when(chargingRequestMapper.selectOne(any())).thenReturn(null);
        when(chargingRequestMapper.selectCount(any())).thenReturn(0L);
        when(chargingRequestMapper.insert(any(ChargingRequest.class))).thenReturn(1);

        List<ChargeRespDTO> fastChargingResults = new ArrayList<>();
        List<ChargeRespDTO> trickleChargingResults = new ArrayList<>();

        // 提交不同充电模式的请求
        for (int i = 0; i < testUserIds.size(); i++) {
            Long userId = testUserIds.get(i);
            ChargeReqDTO request = testRequests.get(i);
            ChargeRespDTO result = chargingRequestService.submitRequest(userId, request);
            if (ChargingMode.FAST.getCode().equals(request.getChargingMode())) {
                fastChargingResults.add(result);
            } else {
                trickleChargingResults.add(result);
            }
        }

        // 验证快充和慢充请求都有
        assertTrue(fastChargingResults.size() > 0, "应该有快充请求");
        assertTrue(trickleChargingResults.size() > 0, "应该有慢充请求");
        assertEquals(testUserIds.size(), fastChargingResults.size() + trickleChargingResults.size());

        // 验证充电模式描述正确设置
        fastChargingResults.forEach(result -> {
            assertEquals(ChargingMode.FAST.getCode(), result.getChargingMode());
            assertEquals(ChargingMode.FAST.getDescription(), result.getChargingModeDesc());
        });

        trickleChargingResults.forEach(result -> {
            assertEquals(ChargingMode.TRICKLE.getCode(), result.getChargingMode());
            assertEquals(ChargingMode.TRICKLE.getDescription(), result.getChargingModeDesc());
        });
    }

    @Test
    void testBatchGetCurrentRequests() {
        // 准备Mock数据
        List<ChargingRequest> mockRequests = new ArrayList<>();
        for (int i = 0; i < testUserIds.size(); i++) {
            ChargingRequest request = new ChargingRequest();
            request.setId((long) (i + 1));
            request.setUserId(testUserIds.get(i));
            request.setChargingMode(testRequests.get(i).getChargingMode());
            request.setRequestAmount(testRequests.get(i).getRequestAmount());
            request.setBatteryCapacity(testRequests.get(i).getBatteryCapacity());
            request.setStatus(RequestStatus.WAITING.getCode());
            request.setQueueNumber("Q" + String.format("%03d", i + 1));
            request.setQueueStartTime(LocalDateTime.now().minusMinutes(10));
            mockRequests.add(request);
        }

        // Mock 查询方法 - 为每个用户ID设置返回值
        for (int i = 0; i < testUserIds.size(); i++) {
            final int index = i;
            when(chargingRequestMapper.selectOne(argThat(wrapper -> {
                String wrapperStr = wrapper.toString();
                return wrapperStr.contains("user_id") && wrapperStr.contains(testUserIds.get(index).toString());
            }))).thenReturn(mockRequests.get(i));
        }

        // 批量获取当前请求
        List<ChargeRespDTO> results = new ArrayList<>();
        for (Long userId : testUserIds) {
            ChargeRespDTO result = chargingRequestService.getCurrentRequest(userId);
            if (result != null) {
                results.add(result);
            }
        }

        // 验证结果
        assertEquals(testUserIds.size(), results.size());
        for (ChargeRespDTO result : results) {
            assertNotNull(result.getQueueNumber());
            assertEquals(RequestStatus.WAITING.getCode(), result.getStatus());
            assertTrue(result.getWaitingTime() >= 0);
        }
    }

    @Test
    void testBatchCancelRequests() {
        // 准备Mock数据
        List<ChargingRequest> mockRequests = new ArrayList<>();
        for (int i = 0; i < testUserIds.size(); i++) {
            ChargingRequest request = new ChargingRequest();
            request.setId((long) (i + 1));
            request.setUserId(testUserIds.get(i));
            request.setStatus(RequestStatus.WAITING.getCode());
            mockRequests.add(request);
        }

        // Mock 查询和更新方法
        for (int i = 0; i < testUserIds.size(); i++) {
            final int index = i;
            when(chargingRequestMapper.selectOne(argThat(wrapper -> {
                String wrapperStr = wrapper.toString();
                return wrapperStr.contains("user_id") && wrapperStr.contains(testUserIds.get(index).toString());
            }))).thenReturn(mockRequests.get(i));
        }
        when(chargingRequestMapper.update(any(), any())).thenReturn(1);

        // 批量取消请求
        List<Boolean> results = new ArrayList<>();
        for (Long userId : testUserIds) {
            boolean result = chargingRequestService.cancelRequest(userId);
            results.add(result);
        }

        // 验证所有取消操作都成功
        assertEquals(testUserIds.size(), results.size());
        assertTrue(results.stream().allMatch(result -> result), "所有取消操作都应该成功");
        // 验证更新操作被调用了正确的次数
        verify(chargingRequestMapper, times(testUserIds.size())).update(any(), any());
        verify(scheduleService, times(testUserIds.size())).triggerSchedule();
    }

    @Test
    void testBatchGetQueueStatus() {
        // 准备Mock数据
        for (int i = 0; i < testUserIds.size(); i++) {
            ChargingRequest request = new ChargingRequest();
            request.setId((long) (i + 1));
            request.setUserId(testUserIds.get(i));
            request.setChargingMode(testRequests.get(i).getChargingMode());
            request.setStatus(RequestStatus.WAITING.getCode());
            request.setQueueNumber("Q" + String.format("%03d", i + 1));

            final int index = i;
            when(chargingRequestMapper.selectOne(argThat(wrapper -> {
                String wrapperStr = wrapper.toString();
                return wrapperStr.contains("user_id") && wrapperStr.contains(testUserIds.get(index).toString());
            }))).thenReturn(request);
            // Mock 排队相关查询
            when(chargingRequestMapper.countWaitingAhead(testRequests.get(i).getChargingMode(), (long) (i + 1))).thenReturn(i);
            when(chargingRequestMapper.countWaitingByMode(testRequests.get(i).getChargingMode())).thenReturn(5);
        }

        // Mock 系统参数服务
        when(systemParamService.getFastChargingPower()).thenReturn(50.0);
        when(systemParamService.getTrickleChargingPower()).thenReturn(7.0);

        // 批量获取排队状态
        List<QueueStatusRespDTO> results = new ArrayList<>();
        for (Long userId : testUserIds) {
            try {
                QueueStatusRespDTO result = chargingRequestService.getQueueStatus(userId);
                results.add(result);
            } catch (Exception e) {
                fail("获取排队状态失败，用户ID: " + userId + ", 错误: " + e.getMessage());
            }
        }

        // 验证结果
        assertEquals(testUserIds.size(), results.size());
        for (QueueStatusRespDTO result : results) {
            assertNotNull(result.getQueueNumber());
            assertNotNull(result.getWaitingCount());
            assertNotNull(result.getTotalWaitingCount());
            assertNotNull(result.getEstimatedWaitingTime());
        }
    }

    @Test
    void testCreateRequestsWithVariousAmounts() {
        // Mock 依赖方法
        when(chargingRequestMapper.selectOne(any())).thenReturn(null);
        when(chargingRequestMapper.selectCount(any())).thenReturn(0L);
        when(chargingRequestMapper.insert(any(ChargingRequest.class))).thenReturn(1);

        // 创建不同充电量的请求
        List<BigDecimal> amounts = List.of(
            new BigDecimal("10.5"),
            new BigDecimal("25.0"),
            new BigDecimal("50.0"),
            new BigDecimal("75.5"),
            new BigDecimal("100.0")
        );

        List<ChargeRespDTO> results = new ArrayList<>();
        for (int i = 0; i < amounts.size(); i++) {
            Long userId = (long) (i + 100); // 使用不同的用户ID
            ChargeReqDTO request = new ChargeReqDTO();
            request.setChargingMode(ChargingMode.FAST.getCode());
            request.setRequestAmount(amounts.get(i));
            request.setBatteryCapacity(new BigDecimal("100.0"));
            ChargeRespDTO result = chargingRequestService.submitRequest(userId, request);
            results.add(result);
            assertEquals(amounts.get(i), result.getRequestAmount());
        }

        assertEquals(amounts.size(), results.size());
    }

    @Test
    void testBatchEndCharging() {
        // 准备Mock数据 - 充电中的请求
        List<ChargingRequest> mockRequests = new ArrayList<>();
        for (int i = 0; i < testUserIds.size(); i++) {
            ChargingRequest request = new ChargingRequest();
            request.setId((long) (i + 1));
            request.setUserId(testUserIds.get(i));
            request.setStatus(RequestStatus.CHARGING.getCode());
            request.setPileId((long) (i + 1));
            request.setChargingStartTime(LocalDateTime.now().minusHours(1));
            mockRequests.add(request);
        }

        // Mock 查询和更新方法
        for (int i = 0; i < testUserIds.size(); i++) {
            final int index = i;
            when(chargingRequestMapper.selectOne(argThat(wrapper -> {
                String wrapperStr = wrapper.toString();
                return wrapperStr.contains("user_id") && wrapperStr.contains(testUserIds.get(index).toString());
            }))).thenReturn(mockRequests.get(i));
        }
        when(chargingRequestMapper.update(any(), any())).thenReturn(1);
        doNothing().when(scheduleService).releasePile(anyLong());

        // 批量结束充电
        List<Boolean> results = new ArrayList<>();
        for (Long userId : testUserIds) {
            boolean result = chargingRequestService.endCharging(userId);
            results.add(result);
        }

        // 验证所有结束操作都成功
        assertEquals(testUserIds.size(), results.size());
        assertTrue(results.stream().allMatch(result -> result), "所有结束充电操作都应该成功");

        // 验证相关方法被调用
        verify(chargingRequestMapper, times(testUserIds.size())).update(any(), any());
        verify(scheduleService, times(testUserIds.size())).releasePile(anyLong());
        verify(scheduleService, times(testUserIds.size())).triggerSchedule();
    }

    /**
     * 创建批量测试数据的辅助方法
     */
    public static List<ChargeReqDTO> createBatchTestRequests(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    ChargeReqDTO request = new ChargeReqDTO();
                    request.setChargingMode(i % 2 == 0 ? ChargingMode.FAST.getCode() : ChargingMode.TRICKLE.getCode());
                    request.setRequestAmount(new BigDecimal("30.0").add(new BigDecimal(i * 5)));
                    request.setBatteryCapacity(new BigDecimal("100.0"));
                    return request;
                })
                .toList();
    }

    /**
     * 创建批量用户ID的辅助方法
     */
    public static List<Long> createBatchUserIds(int count, long startId) {
        return IntStream.range(0, count)
                .mapToObj(i -> startId + i)
                .toList();
    }

    /**
     * 测试大批量请求处理能力
     */
    @Test
    void testLargeBatchSubmitRequests() {
        // 创建100个用户的充电请求
        List<Long> largeUserIds = createBatchUserIds(100, 1000L);
        List<ChargeReqDTO> largeRequests = createBatchTestRequests(100);

        // Mock 依赖方法
        when(chargingRequestMapper.selectOne(any())).thenReturn(null);
        when(chargingRequestMapper.selectCount(any())).thenReturn(0L);
        when(chargingRequestMapper.insert(any(ChargingRequest.class))).thenReturn(1);

        List<ChargeRespDTO> results = new ArrayList<>();

        // 批量提交大量请求
        for (int i = 0; i < largeUserIds.size(); i++) {
            Long userId = largeUserIds.get(i);
            ChargeReqDTO request = largeRequests.get(i);

            ChargeRespDTO result = chargingRequestService.submitRequest(userId, request);
            results.add(result);

            // 验证基本属性
            assertNotNull(result);
            assertEquals(request.getChargingMode(), result.getChargingMode());
            assertEquals(RequestStatus.WAITING.getCode(), result.getStatus());
        }

        // 验证所有请求都成功提交
        assertEquals(largeUserIds.size(), results.size());

        // 验证快充和慢充请求的分布
        long fastCount = results.stream().filter(r -> ChargingMode.FAST.getCode().equals(r.getChargingMode())).count();
        long trickleCount = results.stream().filter(r -> ChargingMode.TRICKLE.getCode().equals(r.getChargingMode())).count();

        assertEquals(50, fastCount, "应该有50个快充请求");
        assertEquals(50, trickleCount, "应该有50个慢充请求");
    }
}