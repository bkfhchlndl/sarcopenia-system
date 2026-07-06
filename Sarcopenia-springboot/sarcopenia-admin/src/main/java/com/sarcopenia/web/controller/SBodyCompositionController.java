package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SBodyComposition;
import com.sarcopenia.web.service.SBodyCompositionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人体成分测量控制层
 * 
 * 设备对接说明：
 * - C#端负责和硬件设备通信，测量完成后调用 /device/pushData 推送数据
 * - C#端定期调用 /device/heartbeat 上报设备在线状态
 * - 前端调用 /device/getStatus 获取设备状态和最新测量数据
 */
@RestController
@RequestMapping("/sBodyComposition")
public class SBodyCompositionController {

    @Resource
    private SBodyCompositionService sBodyCompositionService;

    /** 设备状态缓存：key=设备ID, value=状态数据 */
    private final Map<String, DeviceStatus> deviceStatusMap = new ConcurrentHashMap<>();

    /** 设备状态内部类 */
    public static class DeviceStatus {
        public String deviceId;
        public boolean online;
        public long lastHeartbeat;
        public String status; // IDLE/MEASURING/COMPLETED
        public SBodyComposition latestData;
        public Long currentPatientId;
    }

    // ==================== 前端页面接口 ====================

    /**
     * 根据患者ID查询历史记录
     */
    @GetMapping("/selectByPatientId")
    public AjaxResult selectByPatientId(@RequestParam("patientId") Long patientId) {
        List<SBodyComposition> list = sBodyCompositionService.selectByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 前端保存测量记录
     */
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody SBodyComposition entity) {
        boolean success = sBodyCompositionService.insertRecord(entity);
        return success ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }

    /**
     * 前端获取设备状态
     * @param deviceId 设备ID（默认 default）
     */
    @GetMapping("/device/getStatus")
    public AjaxResult getDeviceStatus(
            @RequestParam(value = "deviceId", defaultValue = "default") String deviceId) {
        DeviceStatus status = deviceStatusMap.get(deviceId);
        if (status == null) {
            return AjaxResult.success(Map.of(
                "deviceId", deviceId,
                "online", false,
                "status", "OFFLINE",
                "lastHeartbeat", 0
            ));
        }
        // 判断是否超时离线（超过30秒无心跳视为离线）
        boolean online = (System.currentTimeMillis() - status.lastHeartbeat) < 30000;
        return AjaxResult.success(Map.of(
            "deviceId", status.deviceId,
            "online", online,
            "status", online ? status.status : "OFFLINE",
            "lastHeartbeat", status.lastHeartbeat,
            "currentPatientId", status.currentPatientId != null ? status.currentPatientId : "",
            "latestData", status.latestData != null ? status.latestData : ""
        ));
    }

    // ==================== C# 设备对接接口 ====================

    /**
     * C#设备心跳上报
     * 设备每10秒调用一次，告诉后端设备在线
     */
    @PostMapping("/device/heartbeat")
    public AjaxResult deviceHeartbeat(
            @RequestParam(value = "deviceId", defaultValue = "default") String deviceId,
            @RequestParam(value = "status", defaultValue = "IDLE") String status) {
        DeviceStatus ds = deviceStatusMap.computeIfAbsent(deviceId, k -> new DeviceStatus());
        ds.deviceId = deviceId;
        ds.online = true;
        ds.lastHeartbeat = System.currentTimeMillis();
        ds.status = status;
        return AjaxResult.success("心跳已更新");
    }

    /**
     * C#设备推送测量数据
     * 测量完成后调用，传入完整人体成分数据
     */
    @PostMapping("/device/pushData")
    public AjaxResult devicePushData(
            @RequestParam(value = "deviceId", defaultValue = "default") String deviceId,
            @RequestBody SBodyComposition entity) {
        if (entity.getPatientId() == null) {
            return AjaxResult.error("患者ID不能为空");
        }

        // 保存到数据库
        boolean success = sBodyCompositionService.insertRecord(entity);

        // 更新设备状态
        DeviceStatus ds = deviceStatusMap.computeIfAbsent(deviceId, k -> new DeviceStatus());
        ds.deviceId = deviceId;
        ds.lastHeartbeat = System.currentTimeMillis();
        ds.status = "COMPLETED";
        ds.latestData = entity;
        ds.currentPatientId = entity.getPatientId();

        return success ? AjaxResult.success("数据接收成功") : AjaxResult.error("数据保存失败");
    }
}
