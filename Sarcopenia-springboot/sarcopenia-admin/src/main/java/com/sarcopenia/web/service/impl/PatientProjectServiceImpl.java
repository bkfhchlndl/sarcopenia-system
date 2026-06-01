package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.PatientProject;
import com.sarcopenia.web.mapper.PatientProjectMapper;
import com.sarcopenia.web.service.PatientProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 项目病人关联 业务服务实现
 *
 * 核心逻辑：一个病人可以做多个检查项目（多对多）
 * - 病人做完CGA评估 → 插入 (patientId, projectId=CGA项目ID)
 * - 病人做完SARC评估 → 插入 (patientId, projectId=SARC项目ID)
 * - 同一病人同一项目不重复插入
 */
@Service
public class PatientProjectServiceImpl implements PatientProjectService {

    @Autowired
    private PatientProjectMapper patientProjectMapper;

    /**
     * 插入项目病人关联（如果不存在则插入，已存在则跳过）
     *
     * @param patientId 病人ID
     * @param projectId 项目ID
     * @return 操作结果
     */
    @Override
    public boolean insertIfNotExists(Long patientId, Long projectId) {
        // 第一步：先查是否已存在（防止重复插入）
        int count = patientProjectMapper.selectCount(patientId, projectId);

        // 第二步：已存在则直接返回成功，不重复插入
        if (count > 0) {
            return true;
        }

        // 第三步：不存在则新建关联记录
        PatientProject pp = new PatientProject();
        pp.setPatientId(patientId);
        pp.setProjectId(projectId);
        pp.setDelFlag("0");

        return patientProjectMapper.insertPatientProject(pp) > 0;
    }
}
