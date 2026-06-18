package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.PatientProject;
import com.sarcopenia.web.mapper.PatientProjectMapper;
import com.sarcopenia.web.service.PatientProjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 患者项目关联业务实现类
 * 核心逻辑：患者与检查项目为多对多关联
 * 1. 患者完成CGA综合评估，自动绑定对应项目
 * 2. 患者完成肌少症专项评估，自动绑定对应项目
 * 3. 同一患者+同一项目仅保留一条关联，不重复新增
 */
@Service
public class PatientProjectServiceImpl implements PatientProjectService {

    @Resource
    private PatientProjectMapper patientProjectMapper;

    /**
     * 无记录则新增患者项目关联，已存在直接跳过
     * @param patientId 患者ID
     * @param projectId 项目ID
     * @return 新增成功返回true；已存在/插入失败返回false
     */
    @Override
    public boolean insertIfNotExists(Long patientId, Long projectId) {
        // 查询是否已存在关联记录，避免重复插入
        int count = patientProjectMapper.selectCount(patientId, projectId);
        if (count > 0) {
            return true;
        }

        // 构建关联实体，逻辑删除标记为正常
        PatientProject pp = new PatientProject();
        pp.setPatientId(patientId);
        pp.setProjectId(projectId);
        pp.setDelFlag("0");

        // 执行插入，返回插入是否成功
        return patientProjectMapper.insertPatientProject(pp) > 0;
    }
}