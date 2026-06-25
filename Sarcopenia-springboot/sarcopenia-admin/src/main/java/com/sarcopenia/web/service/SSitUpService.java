package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.SSitUp;

import java.util.List;

/**
 * 5次起坐时间测量记录业务接口
 */
public interface SSitUpService extends IService<SSitUp> {

    /**
     * 根据患者ID查询5次起坐测量记录列表（按创建时间倒序）
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    List<SSitUp> selectByPatientId(Long patientId);

    /**
     * 新增5次起坐测量记录
     * @param entity 测量记录
     * @return 是否成功
     */
    boolean insertRecord(SSitUp entity);
}
