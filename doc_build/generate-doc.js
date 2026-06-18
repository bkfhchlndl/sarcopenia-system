const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, AlignmentType, BorderStyle, WidthType } = require('docx');
const fs = require('fs');

const SIZE_BODY = 21; // 五号
const SIZE_TITLE = 48;
const SIZE_H1 = 36;
const SIZE_H2 = 28;
const SIZE_H3 = 24;

function paragraph(text, opts = {}) {
  return new Paragraph({
    children: [new TextRun({
      text: text,
      size: opts.size || SIZE_BODY,
      bold: opts.bold || false,
      color: opts.color || '000000',
      font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' }
    })],
    alignment: opts.alignment || AlignmentType.LEFT,
    spacing: { before: opts.spaceBefore || 120, after: opts.spaceAfter || 120, line: 360 }
  });
}

function heading(text, level) {
  const size = level === 1 ? SIZE_H1 : level === 2 ? SIZE_H2 : SIZE_H3;
  const color = level === 1 ? '1F4E79' : '2E75B6';
  return new Paragraph({
    heading: `Heading ${level}`,
    children: [new TextRun({
      text: text,
      size: size,
      bold: true,
      color: color,
      font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' }
    })],
    spacing: { before: level === 1 ? 480 : 240, after: 160, line: 360 },
    pageBreakBefore: level === 1
  });
}

function makeTable(headers, rows) {
  const border = { style: BorderStyle.SINGLE, size: 4, color: 'B4C6E7' };
  const borders = { top: border, bottom: border, left: border, right: border, insideHorizontal: border, insideVertical: border };

  const headerRow = new TableRow({
    tableHeader: true,
    children: headers.map(h => new TableCell({
      borders: borders,
      children: [new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: h, bold: true, size: 20, color: '1F4E79', font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
        shading: { fill: 'D6E4F0' },
        spacing: { before: 60, after: 60 }
      })]
    }))
  });

  const bodyRows = rows.map(row => new TableRow({
    children: row.map(cell => new TableCell({
      borders: borders,
      children: [new Paragraph({
        children: [new TextRun({ text: String(cell || ''), size: 20, color: '000000', font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
        spacing: { before: 60, after: 60 }
      })]
    }))
  }));

  return new Table({ rows: [headerRow, ...bodyRows] });
}

// ========= 数据 =========
const scalesAll = [
  [1, '基本日常生活活动能力筛查', 'basic_adl', '8', 'WS/T 888-2026 B.1'],
  [2, '工具性日常生活活动能力筛查', 'instrumental_adl', '6', 'WS/T 888-2026 B.2'],
  [3, '运动功能筛查', 'exercise_function', '4', 'WS/T 888-2026 B.3'],
  [4, '视力视觉简易评估', 'vision_assessment', '8', 'WS/T 888-2026 B.4+B.5'],
  [5, '听力简易评估', 'hearing_assessment', '8', 'WS/T 888-2026 B.6+B.7'],
  [6, '认知功能筛查', 'cognition_screen', '5', 'WS/T 888-2026 B.8'],
  [7, '居家环境筛查', 'home_environment', '3', 'WS/T 888-2026 B.9'],
  [8, 'FRAIL衰弱评估', 'frail_assessment', '5', 'WS/T 888-2026 B.10'],
  [9, '尿失禁筛查', 'incontinence_screen', '3', 'WS/T 888-2026 B.11'],
  [10, '便秘筛查', 'constipation_screen', '3', 'WS/T 888-2026 B.12'],
  [11, '睡眠障碍筛查', 'sleep_disorder_screen', '3', 'WS/T 888-2026 B.13'],
  [12, '慢性疼痛筛查', 'chronic_pain_screen', '6', 'WS/T 888-2026 B.14+B.15'],
  [13, '压力性损伤风险筛查', 'pressure_injury_risk', '4', 'WS/T 888-2026 B.16'],
  [14, '洼田饮水试验', 'water_swallowing_test', '5', 'WS/T 888-2026 B.17'],
  [15, '营养风险筛查', 'nutrition_risk_screen', '8', 'WS/T 888-2026 B.18+B.19'],
  [16, '谵妄评估', 'delirium_assessment', '4', 'WS/T 888-2026 B.21'],
  [17, '抑郁筛查', 'depression_screen', '3', 'WS/T 888-2026 7.3.2'],
  [18, '焦虑筛查', 'anxiety_screen', '3', 'WS/T 888-2026 7.3.3'],
  [19, '跌倒风险筛查', 'fall_risk_screen', '6', 'WS/T 888-2026 7.5.1'],
  [20, '多重用药状态评估', 'polypharmacy_assessment', '3', 'WS/T 888-2026 7.5.8'],
  [21, '肌少症初筛', 'sarcopenia_screen', '2', 'WS/T 888-2026 7.5.11'],
  [22, '基本日常生活活动能力评估量表', 'daily_life', '100', 'WS/T 888-2026 附录B 日常生活Barthel指数'],
  [23, '工具性日常生活活动能力量表', 'instrumental_life', '8', 'WS/T 888-2026 B.2 工具性ADL'],
  [24, '简易体能状况量表(SPPB)', 'sppb', '12', '平衡+步速+起坐；10-12良好/7-9中等/0-6较差'],
  [25, '视力与视觉功能综合评估', 'visual_acuity', '7', 'WS/T 888-2026 B.4+B.5'],
  [26, '听力与耳语试验综合评估', 'listening', '8', 'WS/T 888-2026 B.6+B.7 听力4分+耳语4分'],
  [27, '居家环境筛查表', 'home_environment', '14', 'WS/T 888-2026 B.9 勾选=是=1分；得分越低风险越大'],
  [28, '简易智力评估量表', 'cognition', '5', 'WS/T 888-2026 B.8 Mini-Cog；0~2阳性/3~5阴性'],
  [29, 'FRAIL衰弱评估量表', 'frail_scale', '5', 'WS/T 888-2026 B.10；≥3衰弱/1~2前期/0无衰弱'],
  [30, '国际尿失禁咨询委员会问卷简表', 'urinary_incontinence', '21', 'WS/T 888-2026 B.11；最近4周漏尿评估'],
  [31, '老年人便秘症状评估表', 'constipation', '0', 'WS/T 888-2026 便秘症状评估'],
  [32, '阿森斯失眠量表(AIS)', 'insomnia', '24', 'WS/T 888-2026 过去1个月睡眠质量'],
  [33, '老年人慢性疼痛综合评估量表', 'chronic_pain', '26', 'WS/T 888-2026 NRS+CPOT；插管1+2+3题/非插管1+2+4题'],
  [34, '压力性损伤风险评估量表(Braden)', 'braden', '23', 'WS/T 888-2026 Braden；≤9极高危/10~12高危/13~14中危/15~18低危'],
  [35, '洼田饮水试验(吞咽障碍)', 'swallowing', '5', 'WS/T 888-2026 30ml温水饮用观察；1级正常/2级可疑/3-5级异常'],
  [36, '营养风险终筛表(NRS-2002)', 'nutrition', '7', 'WS/T 888-2026；0~2每周复评/3~7启动营养干预；≥70岁+1'],
  [37, '微营养评定法简表(MNA-SF)', 'micro_nutrition', '14', 'WS/T 888-2026；≤11存在营养不良风险；BMI与小腿围二选一'],
  [38, '营养不良诊断与分级标准(GLIM)', 'glim', '0', 'WS/T 888-2026 GLIM；1项表现型+1项病因型=确诊；严重程度分重度/中度'],
  [39, '老年人谵妄评估量表', 'delirium', '0', 'WS/T 888-2026；条目1是AND条目2是AND(条目3是OR条目4是)=阳性'],
  [40, 'GDS-15简版老年抑郁量表', 'depression', '15', '正向题是=1分；反向题(1/5/7/11/13)否=1分；≤4无/5~9可疑/≥10抑郁'],
  [41, '老年人跌倒风险筛查量表', 'fall_risk', '0', '跌倒相关危险因素综合评估'],
  [42, 'Beers不适当用药筛查量表', 'medication', '0', '老年人潜在不恰当用药(PIM)筛查；存在风险项建议药师会诊'],
  [43, 'GAD-7广泛性焦虑障碍筛查量表', 'anxiety', '21', '从没有0/几天1/一半以上2/几乎每天3；0~4无/5~9轻度/10~14中度/15~21重度']
];

const controllerApis = [
  ['1', '/patient/insertPatient', 'POST', '新增患者信息'],
  ['2', '/patient/deletePatientById', 'PUT', '删除患者信息(逻辑删除)'],
  ['3', '/patient/updatePatient', 'PUT', '修改患者信息'],
  ['4', '/patient/selectPatientList', 'GET', '查询患者列表（条件过滤）'],
  ['5', '/patient/selectPatientById', 'GET', '根据ID查询单条患者详情'],
  ['6', '/patient/selectPatientReportedList', 'GET', '查询已完成评估患者列表'],
  ['7', '/cgaScale/selectScaleList', 'GET', '查询综合量表信息（1-21，含题目及选项）'],
  ['8', '/cgaScale/selectDailyLifeScale', 'GET', '查询日常生活能力量表（22）'],
  ['9', '/cgaScale/selectInstrumentalityLifeScale', 'GET', '查询工具性日常生活活动能力量表（23）'],
  ['10', '/cgaScale/selectSppbScale', 'GET', '查询简易体能状况量表（SPPB，24）'],
  ['11', '/cgaScale/selectVisualAcuityScale', 'GET', '获取视力与视觉功能综合评估表（25）'],
  ['12', '/cgaScale/selectListeningScale', 'GET', '获取听力与耳语试验综合评估表（26）'],
  ['13', '/cgaScale/selectHomeEnvironmentScale', 'GET', '获取居家环境筛查表（27）'],
  ['14', '/cgaScale/selectCognitionScale', 'GET', '获取认知功能评估量表（28）'],
  ['15', '/cgaScale/selectFrailScale', 'GET', '获取FRAIL衰弱评估量表（29）'],
  ['16', '/cgaScale/selectUrinaryIncontinenceScale', 'GET', '获取尿失禁评估量表（30）'],
  ['17', '/cgaScale/selectConstipationScale', 'GET', '获取便秘症状评估量表（31）'],
  ['18', '/cgaScale/selectInsomniaScale', 'GET', '获取阿森斯失眠量表(AIS)（32）'],
  ['19', '/cgaScale/selectChronicPainScale', 'GET', '获取慢性疼痛综合评估量表（33）'],
  ['20', '/cgaScale/selectPressureInjuryScale', 'GET', '获取压力性损伤风险评估量表(Braden)（34）'],
  ['21', '/cgaScale/selectSwallowScale', 'GET', '获取洼田饮水试验量表（35）'],
  ['22', '/cgaScale/selectNutritionScale', 'GET', '获取营养风险终筛表(NRS-2002)（36）'],
  ['23', '/cgaScale/selectMicroNutritionScale', 'GET', '获取微营养评定法简表(MNA-SF)（37）'],
  ['24', '/cgaScale/selectNutritionalStandardsScale', 'GET', '获取营养不良诊断与分级标准(GLIM)（38）'],
  ['25', '/cgaScale/selectDeliriumScale', 'GET', '获取老年人谵妄评估量表（39）'],
  ['26', '/cgaScale/selectDepressionScale', 'GET', '获取GDS-15简版老年抑郁量表（40）'],
  ['27', '/cgaScale/selectFallScale', 'GET', '获取跌倒风险筛查量表（41）'],
  ['28', '/cgaScale/selectMedicationScale', 'GET', '获取Beers不适当用药筛查量表（42）'],
  ['29', '/cgaScale/selectAnxietyScale', 'GET', '获取GAD-7广泛性焦虑障碍筛查量表（43）'],
  ['30', '/cgaRecord/insertCgaRecord', 'POST', '提交评估记录（POST提交答案JSON+总分）'],
  ['31', '/cgaRecord/selectCgaRecordList', 'GET', '根据条件查询评估记录列表'],
  ['32', '/cgaReportVo/getFullReport/{id}', 'GET', '根据ID获取完整评估报告'],
  ['33', '/cgaReportVo/getFullReportByPatient/{patientId}', 'GET', '根据患者ID获取最新完整报告'],
  ['34', '/cgaReportVo/generateReport/{patientId}', 'GET', '触发生成CGA综合评估报告'],
  ['35', '/cgaReportVo/deleteReportByPatientId/{patientId}', 'DELETE', '根据患者ID删除评估记录']
];

const routeList = [
  ['1', '/patient/detection/index', 'src/views/patient/detection/index', '检测评估首页'],
  ['2', '/patient/detection/geriatricsyndrome', 'src/views/patient/detection/geriatricsyndrome', '老年综合评估入口'],
  ['3', '/patient/detection/cga', 'src/views/patient/detection/cga', 'CGA综合评估（21个筛查量表一次完成）'],
  ['4', '/patient/detection/comprehensive', 'src/views/patient/detection/comprehensive', '综合评估页面'],
  ['5', '/patient/detection/customize', 'src/views/patient/detection/customize', '自定义专项评估入口（量表目录页）'],
  ['6', '/patient/detection/basicInformation', 'src/views/patient/detection/basicInformation', '基本情况登记'],
  ['7', '/patient/detection/customize/dailyLife', 'src/views/patient/detection/customize/dailyLife.vue', '日常生活评估（Barthel）'],
  ['8', '/patient/detection/customize/instrumentalityLife', 'src/views/patient/detection/customize/instrumentalityLife.vue', '工具性生活评估（Lawton IADL）'],
  ['9', '/patient/detection/customize/sppb', 'src/views/patient/detection/customize/sppb.vue', '简易体能状况评估（SPPB）'],
  ['10', '/patient/detection/customize/visualAcuity', 'src/views/patient/detection/customize/visualAcuity.vue', '视力视觉评估'],
  ['11', '/patient/detection/customize/listening', 'src/views/patient/detection/customize/listening.vue', '听力评估'],
  ['12', '/patient/detection/customize/homeEnvironment', 'src/views/patient/detection/customize/homeEnvironment.vue', '居家环境评估'],
  ['13', '/patient/detection/customize/cognition', 'src/views/patient/detection/customize/cognition.vue', '认知功能评估（Mini-Cog）'],
  ['14', '/patient/detection/customize/frail', 'src/views/patient/detection/customize/frail.vue', '衰弱评估（FRAIL）'],
  ['15', '/patient/detection/customize/urinaryIncontinence', 'src/views/patient/detection/customize/urinaryIncontinence.vue', '尿失禁评估'],
  ['16', '/patient/detection/customize/constipation', 'src/views/patient/detection/customize/constipation.vue', '便秘症状评估'],
  ['17', '/patient/detection/customize/insomnia', 'src/views/patient/detection/customize/insomnia.vue', '失眠评估（AIS）'],
  ['18', '/patient/detection/customize/chronicPain', 'src/views/patient/detection/customize/chronicPain.vue', '慢性疼痛评估（NRS+CPOT）'],
  ['19', '/patient/detection/customize/pressureInjury', 'src/views/patient/detection/customize/pressureInjury.vue', '压力性损伤风险评估（Braden）'],
  ['20', '/patient/detection/customize/swallow', 'src/views/patient/detection/customize/swallow.vue', '吞咽障碍评估（洼田饮水）'],
  ['21', '/patient/detection/customize/nutrition', 'src/views/patient/detection/customize/nutrition.vue', '营养评估（NRS-2002）'],
  ['22', '/patient/detection/customize/microNutrition', 'src/views/patient/detection/customize/microNutrition.vue', '微营养评估（MNA-SF）'],
  ['23', '/patient/detection/customize/delirium', 'src/views/patient/detection/customize/delirium.vue', '谵妄评估（组合逻辑判定）'],
  ['24', '/patient/detection/customize/depression', 'src/views/patient/detection/customize/depression.vue', '老年抑郁评估（GDS-15）'],
  ['25', '/patient/detection/customize/fall', 'src/views/patient/detection/customize/fall.vue', '跌倒风险评估'],
  ['26', '/patient/detection/customize/medication', 'src/views/patient/detection/customize/medication.vue', '多重用药/不适当用药评估（Beers）'],
  ['27', '/patient/detection/customize/anxiety', 'src/views/patient/detection/customize/anxiety.vue', '焦虑障碍评估（GAD-7）'],
  ['28', '/report/cgareport', 'src/views/report/cgareport', 'CGA综合评估报告查看/打印/删除']
];

const rules = [
  ['1', 'basic_adl / basic_dl', '阴阳规则', '得分越高越独立；满分=无依赖'],
  ['2', 'instrumental_adl / instrumental_life', '阴阳规则', '得分越低工具性能力越差'],
  ['3', 'exercise_function / sppb', '阴阳规则（专项另用SPPB分段）', '10-12良好/7-9中等/0-6较差'],
  ['4', 'vision_assessment / visual_acuity', '阴阳规则', '视力>3且视功能>2=阴性'],
  ['5', 'hearing_assessment / listening', '阴阳规则', '听力>3且耳语能听清=阴性'],
  ['6', 'cognition_screen / cognition', 'ScoreRule（分段判定）', 'score<=2阳性/3-5阴性'],
  ['7', 'home_environment', '阴阳规则', '全选否=阴性；勾选=居家安全风险'],
  ['8', 'frail_assessment / frail_scale', 'ScoreRule（分段判定）', 'score==5无/score>=3前期/其他衰弱'],
  ['9', 'incontinence_screen / urinary_incontinence', '阴阳规则', '无漏尿=阴性；其他阳性'],
  ['10', 'constipation_screen / constipation', '阴阳规则', '无便秘症状=阴性'],
  ['11', 'sleep_disorder_screen / insomnia', '阴阳规则', '无睡眠障碍=阴性'],
  ['12', 'chronic_pain_screen / chronic_pain', '阴阳规则', '无慢性疼痛=阴性'],
  ['13', 'pressure_injury_risk / braden', 'ScoreRule（分段判定）', 'Braden<=9极高/10-12高/13-14中/15-18低'],
  ['14', 'water_swallowing_test / swallowing', '阴阳规则', '1级=正常；2级可疑；3-5级吞咽障碍'],
  ['15', 'nutrition_risk_screen / nutrition', 'ScoreRule（分段判定）', 'NRS<3无/3-7启动营养干预'],
  ['16', 'delirium_assessment / delirium', '特殊组合逻辑', '条目1是AND条目2是AND(条目3是OR条目4是)=谵妄阳性'],
  ['17', 'depression_screen / depression', '阴阳规则（专项用GDS-15）', 'GDS<=4无/5-9可疑/≥10抑郁'],
  ['18', 'anxiety_screen / anxiety', '阴阳规则（专项用GAD-7）', 'GAD 0-4无/5-9轻度/10-14中度/15-21重度'],
  ['19', 'fall_risk_screen / fall_risk', '阴阳规则', '无跌倒风险因素=阴性'],
  ['20', 'polypharmacy_assessment / medication', 'ScoreRule（分段判定）', '用药<5无/5-9轻度/≥10重度多重用药']
];

// ========= 组装文档内容 =========
const children = [];

// 封面
children.push(new Paragraph({ spacing: { before: 3000 }, children: [] }));
children.push(new Paragraph({
  alignment: AlignmentType.CENTER,
  children: [new TextRun({ text: '肌少症评估系统', size: SIZE_TITLE, bold: true, color: '1F4E79', font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
  spacing: { before: 200, after: 200 }
}));
children.push(new Paragraph({
  alignment: AlignmentType.CENTER,
  children: [new TextRun({ text: '接口与功能文档（V1.1 补充版）', size: SIZE_H1, bold: true, color: '2E75B6', font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
  spacing: { before: 200, after: 400 }
}));
children.push(new Paragraph({
  alignment: AlignmentType.CENTER,
  children: [new TextRun({ text: '版本：V1.1', size: SIZE_H2, color: '404040', font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
  spacing: { before: 200, after: 200 }
}));
children.push(new Paragraph({
  alignment: AlignmentType.CENTER,
  children: [new TextRun({ text: '日期：2026-06-15', size: SIZE_H2, color: '404040', font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
  spacing: { before: 200, after: 200 }
}));

// 第一章 系统概述
children.push(heading('第一章 系统概述', 1));
children.push(paragraph('本系统为一套面向老年综合评估（CGA - Comprehensive Geriatric Assessment）的 Web 信息系统。底层采用 SpringBoot + MyBatis + MySQL 构建后端服务，前端使用 Vue3 + Element Plus。'));
children.push(paragraph('核心价值：将 WS/T 888-2026《老年综合评估技术应用规范》中的 43 个评估量表数字化，实现患者信息管理、量表评估、规则引擎自动判定、综合报告生成与输出。'));

children.push(heading('1.1 系统架构', 2));
children.push(paragraph('表示层（前端 Vue3）：提供患者管理、评估录入、报告浏览等交互页面。'));
children.push(paragraph('业务层（后端 Java / SpringBoot）：业务逻辑通过 Service 层组织，核心功能通过 Controller 暴露 REST 接口。'));
children.push(paragraph('数据层（MySQL + MyBatis）：量表题目/选项、评估记录、患者档案通过数据库持久化。Mapper XML 组织复杂嵌套查询（量表 → 题目 → 选项）。'));
children.push(paragraph('规则引擎层：CgaRuleEngine + CgaRuleRegistry，按 scaleCode 注册评估规则，输出正常范围、评估结果描述、提示信息。'));

children.push(heading('1.2 评估量表体系', 2));
children.push(paragraph('数据库 cga_scale 表共注册 43 个评估量表条目。分为两大体系：'));
children.push(paragraph('体系一（筛查型，scale_id 1-21）：面向初筛，题目较少（2-8 题/量表），答案以选择题为主，规则引擎通过"阴阳规则"快速判定。'));
children.push(paragraph('体系二（专项评估型，scale_id 22-43）：面向深入评估，题目较多且评分规则复杂，如 Barthel ADL（100分）、SPPB（12分）、GDS-15（15分）、GAD-7（21分）等。'));

children.push(heading('1.3 评估入口说明', 2));
children.push(paragraph('CGA综合评估：进入 /patient/detection/cga 页面，一次性完成 21 个筛查量表（scale_id 1-21），快速完成老年综合初筛。'));
children.push(paragraph('专项/自定义评估：从 /patient/detection/customize 目录页进入具体量表，每个量表独立页面（共 21 个专项页面），针对特定维度做深入评估。'));
children.push(paragraph('专项评估结果优先级高于综合筛查结果：当患者同一维度同时存在综合筛查记录与专项评估记录时，报告中展示专项评估的 result/suggest。'));

// 第二章 接口设计
children.push(heading('第二章 接口设计（Controller）', 1));
children.push(paragraph('本章汇总系统全部公开 REST 接口。原文档仅列出 6 个接口，下表补充完整 35 个接口（含患者管理、量表查询、评估记录、报告生成四大类）。'));

children.push(heading('2.1 完整接口清单', 2));
children.push(makeTable(
  ['序号', '接口路径', '请求方式', '功能说明'],
  controllerApis.map(a => [a[0], a[1], a[2], a[3]])
));

children.push(heading('2.2 接口四大分类说明', 2));
children.push(paragraph('患者管理（/patient/*，共 6 个接口）：患者信息 CRUD + 已评估患者过滤。其中 insertPatient、updatePatient 支持中文姓名、病历号、性别、身高、出生日期、电话、身份证、地址等字段。'));
children.push(paragraph('量表查询（/cgaScale/*，共 23 个接口）：selectScaleList 一次性查询综合量表（1-21）嵌套题目与选项；其余 22 个接口分别查询各专项量表（22-43）的题目与选项。'));
children.push(paragraph('评估记录（/cgaRecord/*，共 2 个接口）：insertCgaRecord 接收包含 answers（Map<questionId, List<optionId>>）的 POST 请求，保存评估记录并计算总分。'));
children.push(paragraph('评估报告（/cgaReportVo/*，共 4 个接口）：按评估记录 ID 或患者 ID 获取完整报告；generateReport 触发生成；deleteReportByPatientId 删除指定患者的所有评估记录。'));

// 第三章 业务层
children.push(heading('第三章 业务层（Service）', 1));
children.push(paragraph('本章说明服务层主要方法。原文档列出的 PatientService / PatientProjectService / CgaScaleService / CgaReportVoService / CgaRecordService 五大类保持不变，以下补充每个类的主要功能。'));

children.push(heading('3.1 PatientService 病人信息', 2));
children.push(makeTable(
  ['方法名', '功能说明', '参数', '返回'],
  [
    ['insertPatient', '新增患者信息', 'Patient patient', 'int 影响行数'],
    ['deletePatientById', '根据患者ID删除（逻辑删除）', 'Long id', 'int'],
    ['updatePatient', '修改患者信息', 'Patient patient', 'int'],
    ['selectPatientList', '按条件查询患者列表', 'Patient patient（过滤条件）', 'List<Patient>'],
    ['selectPatientById', '查询单条患者详情', 'Long patientId', 'Patient'],
    ['selectPatientReportedList', '查询已完成评估患者', 'Patient patient', 'List<Patient>']
  ]
));

children.push(heading('3.2 PatientProjectService 项目病人关联', 2));
children.push(makeTable(
  ['方法名', '功能说明', '参数', '返回'],
  [
    ['insertIfNotExists', '不存在则插入关联记录，避免重复绑定', 'Long patientId, Long projectId', 'boolean']
  ]
));

children.push(heading('3.3 CgaScaleService 评估量表', 2));
children.push(paragraph('核心方法为 selectScaleList（综合量表 1-21，含题目与选项的三层嵌套结构），以及针对各专项量表的独立查询方法（selectDailyLifeScale、selectSppbScale、...、selectAnxietyScale，共 22 个方法）。'));
children.push(paragraph('数据返回结构：ScaleQuestionOptionVO（量表 → QuestionOptionVO（题目）→ CgaOption（选项）三层嵌套）。'));

children.push(heading('3.4 CgaRecordService 提交评估记录', 2));
children.push(makeTable(
  ['方法名', '功能说明', '参数', '返回'],
  [
    ['insertCgaRecord', '提交量表评估答案，保存评估记录与总分', 'CgaRecordDTO dto', 'boolean'],
    ['selectCgaRecordList', '按条件查询评估记录列表', 'Long patientId（或条件对象）', 'List<CgaRecord>']
  ]
));

children.push(heading('3.5 CgaReportVoService 评估报告', 2));
children.push(makeTable(
  ['方法名', '功能说明', '参数', '返回'],
  [
    ['getFullReport', '根据评估记录ID获取完整报告', 'Long id', 'CgaReportVO'],
    ['getFullReportByPatient', '根据患者ID获取最新完整报告', 'Long patientId', 'CgaReportVO'],
    ['deleteReportByPatientId', '根据患者ID删除相关评估记录', 'Long patientId', 'int']
  ]
));
children.push(paragraph('报告聚合逻辑：查询患者基础信息 → 汇总患者全部评估记录 → 针对每个 scaleCode 调用规则引擎生成 itemScore/normalRange/tip → 若存在专项评估记录则覆盖综合筛查的同名维度 → 组装为 CgaReportVO 返回。'));

// 第四章 实体类
children.push(heading('第四章 实体类（Entity）', 1));
children.push(paragraph('本章补充关键实体字段的业务含义，原文档仅列出基本字段名。'));

children.push(heading('4.1 Patient（patient表）', 2));
children.push(makeTable(
  ['字段', '类型', '说明'],
  [
    ['id', 'Long', '主键，自增'],
    ['caseNo', 'String', '病历号（院区/科室编码+流水号）'],
    ['name', 'String', '患者姓名'],
    ['gender', 'String', '性别（男/女）'],
    ['birthday', 'Date', '出生日期，用于计算年龄'],
    ['age', 'Integer', '年龄（报告中由 birthday 自动计算）'],
    ['height', 'BigDecimal', '身高（cm；部分营养量表中需计算BMI参考）'],
    ['phone', 'String', '手机号码'],
    ['idCard', 'String', '身份证号'],
    ['address', 'String', '家庭住址'],
    ['isDel', 'Integer', '删除标志（0=正常/1=已删除）'],
    ['isReport', 'Integer', '报告状态（0=未评估/1=已评估）']
  ]
));

children.push(heading('4.2 CgaScale（cga_scale表）量表信息表', 2));
children.push(makeTable(
  ['字段', '类型', '说明'],
  [
    ['id', 'Long', '量表ID（1-43，与业务约定一一对应）'],
    ['scaleName', 'String', '量表名称（显示名）'],
    ['code', 'String', '量表编码（规则引擎中 scaleCode 的唯一键）'],
    ['totalScore', 'Integer', '量表满分/题目数；0表示无统一满分逻辑'],
    ['remark', 'String', '评分标准备注说明'],
    ['delFlag', 'String', '删除标记（0=正常/1=已删除）']
  ]
));

children.push(heading('4.3 CgaQuestion（cga_question表）量表题目表', 2));
children.push(makeTable(
  ['字段', '类型', '说明'],
  [
    ['id', 'Long', '题目主键'],
    ['scaleId', 'Long', '所属量表ID（外键 cga_scale.id）'],
    ['title', 'String', '题目内容（显示在评估页面的题目文字）'],
    ['type', 'Integer', '题目类型（1=单选/2=多选/3=文本输入）'],
    ['sort', 'Integer', '题目排序（用于前端显示顺序）'],
    ['delFlag', 'String', '删除标记']
  ]
));

children.push(heading('4.4 CgaOption（cga_option表）量表选项表', 2));
children.push(makeTable(
  ['字段', '类型', '说明'],
  [
    ['id', 'Long', '选项主键'],
    ['questionId', 'Long', '所属题目ID（外键 cga_question.id）'],
    ['label', 'String', '选项标识（如 A/B/C或 是/否）'],
    ['content', 'String', '选项详细描述'],
    ['score', 'Integer', '选项得分（用于累加计算量表总分）'],
    ['sort', 'Integer', '选项排序'],
    ['delFlag', 'String', '删除标记']
  ]
));

children.push(heading('4.5 CgaRecord（cga_record表）评估记录表', 2));
children.push(makeTable(
  ['字段', '类型', '说明'],
  [
    ['id', 'Long', '记录主键'],
    ['patientId', 'Long', '患者ID（外键 patient.id）'],
    ['userId', 'Long', '操作人/评估人ID（谁做的本次评估）'],
    ['scaleId', 'Long', '量表ID（外键 cga_scale.id）'],
    ['totalScore', 'Integer', '本量表总得分（所选选项score累加）'],
    ['result', 'String', '评估结果（由前端或后端规则引擎生成的结论描述）'],
    ['suggest', 'String', '医生建议/干预建议'],
    ['answerJson', 'String', '答案JSON（格式：questionId -> 选中的optionId列表）'],
    ['createTime', 'Date', '创建时间'],
    ['delFlag', 'String', '删除标记']
  ]
));

children.push(heading('4.6 PatientProject（patient_project表）病人项目关联表', 2));
children.push(makeTable(
  ['字段', '类型', '说明'],
  [
    ['id', 'Integer', '主键'],
    ['patientId', 'Long', '病人ID'],
    ['projectId', 'Long', '项目ID'],
    ['delFlag', 'String', '删除标志']
  ]
));

// 第五章 DTO / VO
children.push(heading('第五章 DTO / VO', 1));

children.push(heading('5.1 CgaRecordDTO', 2));
children.push(paragraph('提交评估记录的请求参数对象。核心字段：patientId（患者ID，@NotNull）、projectId（项目ID，@NotNull）、answers（Map<Long, List<Long>>，题目ID到所选选项ID列表的映射，@NotNull）。'));
children.push(paragraph('answers 示例：{ "1": [2], "2": [1], ... }，表示第1题选了选项ID=2，第2题选了选项ID=1（单选题）。多选题对应 List 有多个元素，文本题对应 optionId 可能为 0 或特殊约定的内容。'));

children.push(heading('5.2 CgaReportVO', 2));
children.push(paragraph('老年综合整体评估报告视图对象。核心字段包括：patientId、caseNo、patientName、gender、age（由 birthday 计算）、height、phone、idCard、address、recordId、userId、totalScore、result、suggest、createTime、assessmentList（List<AssessmentItemVO>，每个量表一条评估项）。'));

children.push(heading('5.3 AssessmentItemVO（内部类）', 2));
children.push(paragraph('单个评估项的详细信息。核心字段：scaleId、scaleName、scaleCode、scaleTotalScore（量表满分）、rawScore（原始得分）、itemScore（项目得分/评估结论文本）、normalRange（正常范围说明）、tip（提示/风险提示）、remark。'));
children.push(paragraph('其中 normalRange / itemScore / tip 三个字段由 CgaRuleEngine 按 scaleCode 匹配规则动态生成。'));

children.push(heading('5.4 ScaleQuestionOptionVO', 2));
children.push(paragraph('量表 + 题目 + 选项的完整嵌套视图对象。核心字段：scaleId、scaleName、code、totalScore、remark、questionList（List<QuestionOptionVO>）。'));

children.push(heading('5.5 QuestionOptionVO', 2));
children.push(paragraph('题目 + 选项结构。核心字段：questionId、title、type、sort、optionList（List<CgaOption>）。'));

// 第六章 评估量表库（补充）
children.push(heading('第六章 评估量表库（补充）', 1));
children.push(paragraph('本章列出数据库 cga_scale 表中全部 43 个量表的完整信息，并按综合筛查（1-21）与专项评估（22-43）分组展示。'));

children.push(heading('6.1 综合筛查量表（scale_id 1-21）', 2));
children.push(makeTable(
  ['scale_id', '量表名称', 'code', '满分/题数', '备注/评分标准'],
  scalesAll.filter(s => parseInt(s[0]) <= 21).map(s => [s[0], s[1], s[2], s[3], s[4]])
));

children.push(heading('6.2 专项评估量表（scale_id 22-43）', 2));
children.push(makeTable(
  ['scale_id', '量表名称', 'code', '满分/题数', '备注/评分标准'],
  scalesAll.filter(s => parseInt(s[0]) >= 22).map(s => [s[0], s[1], s[2], s[3], s[4]])
));

children.push(heading('6.3 题目类型与答题流程', 2));
children.push(paragraph('question.type 字段用于控制前端渲染方式：'));
children.push(paragraph('type=1（单选）：最常见，一题选一个选项（如日常生活、认知评估的多数题目）。每题选中一个 optionId。'));
children.push(paragraph('type=2（多选）：一题允许多个选项（如慢性疼痛CPOT、居家环境风险清单、GLIM病因型指标等）。'));
children.push(paragraph('type=3（文本输入）：一题允许自由输入文本（如多重用药第6题"请列出药品清单"、疾病评估中描述特殊疾病情况）。不计入总分统计。'));

children.push(heading('6.4 答案数据结构（answerJson）', 2));
children.push(paragraph('评估记录保存时，前端提交的 answers（Map<Long, List<Long>>）会被序列化为 JSON 字符串存到 cga_record.answerJson 字段。'));
children.push(paragraph('典型结构：{ "101": [201], "102": [205], "103": [211,212] }。其中 key 为 questionId，value 为 optionId 数组（单选题数组长度为 1）。'));
children.push(paragraph('报告生成时，后端会反序列化 answerJson，遍历每个 questionId 取出 optionId，再查询每个 optionId 的 score 累加得到该量表的 rawScore，再送入规则引擎生成 itemScore。'));

// 第七章 规则引擎
children.push(heading('第七章 规则引擎（补充）', 1));

children.push(heading('7.1 规则引擎架构', 2));
children.push(paragraph('CgaRule 接口：定义每条规则的三个输出：getNormalRange(score) / formatScore(score) / getTip(score, isPositive)。'));
children.push(paragraph('YinYangRule 阴阳规则：接收 normalRange / positiveMessage / negativeMessage 三个参数。按得分是否触发阈值来输出"阳性/阴性"二分类结论。适合大部分筛查量表。'));
children.push(paragraph('ScoreRule 评分规则：接收 normalRange（字符串）+ formatFunction（score -> 描述文本的函数）。适合需要分段判定逻辑的量表。'));
children.push(paragraph('CgaRuleRegistry：按 scaleCode 注册 20+ 条规则。报告聚合时通过 CgaReportVoServiceImpl 按 scaleCode 查找规则并生成每项评估内容。'));
children.push(paragraph('特殊判定（谵妄）：不从原始分数判定，而是解析 answerJson 中指定的 4 个题目得分，使用组合逻辑（条目1 AND 条目2 AND(条目3 OR 条目4)）判定是否为谵妄阳性。由 CgaReportVoServiceImpl 中分支代码处理。'));

children.push(heading('7.2 规则注册表（按评估维度分类）', 2));
children.push(makeTable(
  ['编号', 'scaleCode（综合/专项）', '规则类型', '正常范围/判定标准'],
  rules.map(r => [r[0], r[1], r[2], r[3]])
));

children.push(heading('7.3 ScoreRule 分段判定详解', 2));
children.push(makeTable(
  ['量表', '分段规则'],
  [
    ['认知功能（cognition_screen）', 'score <= 2 → 认知功能障碍筛查阳性；score > 2 → 阴性。提示：Mini-Cog 0-2分需进一步MMSE/MoCA评估。'],
    ['谵妄（delirium_assessment）', '组合逻辑：条目1是 AND 条目2是 AND(条目3是 OR 条目4是) = 阳性。score >=3 → 正常；否则阳性。'],
    ['衰弱（frail_assessment）', 'score == 5 → 无衰弱；score >= 3 → 衰弱前期；其他 → 衰弱。'],
    ['压力性损伤（braden/pressure_injury_risk）', 'Braden score <= 9 极高危；10-12 高危；13-14 中危；15-18 低危；>18 无风险。'],
    ['营养风险（nutrition/nutrition_risk_screen）', 'NRS-2002 score < 3 无营养风险；3-7 存在营养风险需启动营养干预；age >= 70岁 自动加1分。'],
    ['多重用药（medication/polypharmacy_assessment）', '用药数 <5 无多重用药风险；5-9 轻度多重用药风险；>=10 重度多重用药风险。'],
    ['跌倒风险（fall_risk_screen/fall_risk）', '存在跌倒史/步态异常/平衡障碍/多类药物/认知障碍等任一 = 阳性。'],
    ['抑郁（GDS-15/depression）', 'score <= 4 无抑郁；5-9 可疑抑郁；>=10 抑郁。（反向题：1/5/7/11/13 题选"否"才得分）'],
    ['焦虑（GAD-7/anxiety）', 'score 0-4 无焦虑；5-9 轻度焦虑；10-14 中度焦虑；15-21 重度焦虑。']
  ]
));

// 第八章 前端路由与页面（补充）
children.push(heading('第八章 前端路由与页面（补充）', 1));
children.push(heading('8.1 业务路由清单（完整 28 个页面）', 2));
children.push(makeTable(
  ['序号', '路由路径', '页面组件文件', '功能描述'],
  routeList.map(r => [r[0], r[1], r[2], r[3]])
));

children.push(heading('8.2 前端 API 汇总（src/api 目录）', 2));
children.push(makeTable(
  ['API 文件', '接口数量', '核心功能'],
  [
    ['src/api/patient.js', '6个接口', '患者信息CRUD+已评估患者过滤（insert/delete/update/selectById/selectList/selectReportedList）'],
    ['src/api/cga.js', '25个接口', '综合量表selectScaleList + 22个专项selectXxxScale + insertCgaRecord + selectCgaRecordList'],
    ['src/api/cgaReport.js', '4个接口', 'getFullReport / getFullReportByPatient / generateReport / deleteReportByPatientId']
  ]
));

children.push(heading('8.3 前端页面功能简述', 2));
children.push(paragraph('新增患者（newCase/index.vue）：录入患者基本信息，保存后可立即跳转评估。'));
children.push(paragraph('患者列表（caseList/index.vue）：查询、编辑、删除患者；支持"已评估/未评估"状态过滤。'));
children.push(paragraph('评估入口（detection/index.vue）：提供三个评估入口卡片 — CGA综合评估 / 老年综合评估 / 自定义专项评估。'));
children.push(paragraph('CGA综合评估（detection/cga.vue）：一个页面内按顺序完成 21 个筛查量表。进度条显示当前进度，支持中间保存。'));
children.push(paragraph('专项评估（detection/customize/*.vue）：21 个独立页面，每个页面只针对一个量表。适合在完成综合评估后针对某一风险维度做深入评估。如 Barthel ADL、SPPB、GDS-15、GAD-7、Braden、MNA-SF、NRS-2002、GLIM 等。'));
children.push(paragraph('评估报告（report/cgareport/index.vue）：汇总展示患者全部评估结果。每条评估项按量表维度输出量表名称、原始得分、正常范围、结论、建议等信息。支持 PDF导出/打印 / 一键删除。'));

// 第九章 业务流程
children.push(heading('第九章 业务流程（补充）', 1));

children.push(heading('9.1 完整评估流程', 2));
children.push(makeTable(
  ['步骤', '操作者', '操作', '前端路由/页面', '后端接口'],
  [
    ['1', '医护人员', '录入/选择患者信息', '/patient/newCase 或 /patient/detection/index', '/patient/insertPatient、/patient/selectPatientById'],
    ['2', '医护人员', '选择评估入口（CGA综合评估 或 专项评估）', '/patient/detection/index', '-'],
    ['3a', '医护人员', '(综合评估)一次性完成21个筛查量表', '/patient/detection/cga', '/cgaScale/selectScaleList + /cgaRecord/insertCgaRecord'],
    ['3b', '医护人员', '(专项评估)进入具体评估页完成单个量表', '/patient/detection/customize/*', '/cgaScale/selectXxxScale + /cgaRecord/insertCgaRecord'],
    ['4', '系统(后端)', '自动聚合评估记录 + 规则引擎生成每项结论', '-', 'CgaReportVoServiceImpl.getFullReportByPatient'],
    ['5', '医护人员', '浏览综合评估报告/打印/删除', '/report/cgareport', '/cgaReportVo/getFullReportByPatient、/deleteReportByPatientId'],
    ['6', '系统(后端)', '自动更新患者isReport标记为已评估', '-', 'CgaRecordService.insertCgaRecord']
  ]
));

children.push(heading('9.2 报告生成流程（内部）', 2));
children.push(paragraph('(1) 查询患者基础信息（Patient），根据 birthday 自动计算 age。'));
children.push(paragraph('(2) 查询该患者所有评估记录（按 scaleId 分组）。'));
children.push(paragraph('(3) 对每个 scaleId 找到最新一条记录，反序列化 answerJson，按 optionId 查 score 累加得到 rawScore。'));
children.push(paragraph('(4) 通过 CgaRuleEngine 按 scaleCode 匹配规则，输出 normalRange、itemScore、tip。'));
children.push(paragraph('(5) 若同一维度存在专项评估记录覆盖综合筛查结果，则以专项评估的 result/suggest 为准。'));
children.push(paragraph('(6) 特殊分支：谵妄评估使用组合逻辑解析 answerJson 中 4 道指定题目，而非原始分数判定。'));
children.push(paragraph('(7) 组装为 CgaReportVO 返回前端。'));

children.push(heading('9.3 数据流向示意', 2));
children.push(paragraph('录入数据：前端 answers (Map<questionId, List<optionId>>) -> JSON 序列化 -> cga_record.answerJson。'));
children.push(paragraph('读取数据：cga_record.answerJson 反序列化 -> optionId 查 score 累加 -> rawScore -> CgaRuleEngine -> itemScore/normalRange/tip。'));
children.push(paragraph('报告输出：CgaReportVO（患者信息 + assessmentList 逐项评估结果）-> report/cgareport 页面渲染。'));

// 第十章 扩展性与维护
children.push(heading('第十章 扩展性与维护说明（补充）', 1));

children.push(heading('10.1 新增评估量表流程', 2));
children.push(paragraph('如需新增一个评估量表（如 MMSE 简易精神状态检查、MNA 完整版等），按以下步骤操作：'));
children.push(paragraph('(1) 数据库：在 cga_scale 表插入一条 scale 记录；在 cga_question 表批量插入题目；在 cga_option 表插入每个题目对应的选项（含 score 字段）。'));
children.push(paragraph('(2) 后端 Mapper/Service：在 CgaScaleMapper.xml 增加一个 selectXxxScale 的查询（复用 selectScaleList 的 resultMap，按 scale_id = N 查询量表+题目+选项的嵌套结构）。'));
children.push(paragraph('(3) 后端 Controller：在 CgaScaleController 增加一个 getXxxScale 接口。'));
children.push(paragraph('(4) 规则引擎：在 CgaRuleRegistry 按新 scaleCode 注册一条 YinYangRule 或 ScoreRule。'));
children.push(paragraph('(5) 前端 API：在 src/api/cga.js 增加对应函数。'));
children.push(paragraph('(6) 前端页面：在 src/views/patient/detection/customize/ 新增 xxx.vue 评估页面（可参考 dailyLife.vue 模板）。'));
children.push(paragraph('(7) 路由：在 src/router/index.js 增加一条路由条目指向新页面。'));

children.push(heading('10.2 修改现有评分规则', 2));
children.push(paragraph('规则引擎集中在 CgaRuleRegistry.java，按 scaleCode 组织。调整某量表的正常范围或分段判定标准时，仅修改对应 register 行的构造参数即可，无需修改业务层/DAO层。'));
children.push(paragraph('例如：调整压力性损伤 Braden 分数段 -> 修改 braden 注册行的 ScoreRule 分段逻辑 -> 重新部署后端即可生效。'));

children.push(heading('10.3 数据备份与清理', 2));
children.push(paragraph('量表定义数据（scale / question / option）为配置型数据，变化较少，建议数据库层面定期备份（mysqldump）。'));
children.push(paragraph('评估记录数据（cga_record）随业务增长较快，建议按患者维度建立分区索引，或按月归档历史记录。'));

children.push(heading('10.4 版本更新说明', 2));
children.push(paragraph('V1.0（原文档）：基础架构 + 患者管理 + 6 个基本接口 + 简化量表库。'));
children.push(paragraph('V1.1（本次补充）：完整 43 个评估量表 + 23 个专项量表接口 + 规则引擎 20+ 条规则 + 28 个前端业务页面 + 报告生成逻辑 + 数据结构说明。'));

children.push(new Paragraph({ spacing: { before: 600 }, children: [] }));
children.push(new Paragraph({
  alignment: AlignmentType.CENTER,
  children: [new TextRun({ text: '—— 文档结束 ——', size: SIZE_BODY, color: '808080', bold: true, font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' } })],
  spacing: { before: 400, after: 400 }
}));

// ========= 生成 docx =========
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: { eastAsia: 'Microsoft YaHei', ascii: 'Arial', hAnsi: 'Arial' }, size: SIZE_BODY }
      }
    }
  },
  sections: [{ children: children }]
});

Packer.toBuffer(doc).then(buffer => {
  const outputPath = 'd:\\sarcopenia\\肌少症评估系统_接口文档_V1.1.docx';
  fs.writeFileSync(outputPath, buffer);
  console.log('文档已生成: ' + outputPath);
  console.log('文件大小: ' + (buffer.length / 1024).toFixed(1) + ' KB');
});
