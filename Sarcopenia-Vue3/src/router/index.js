import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout'

export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/lock',
    component: () => import('@/views/lock'),
    hidden: true,
    meta: { title: '锁定屏幕' }
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile/:activeTab?',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  // 检测评估主路由 single:true 无下拉箭头
  {
    path: '/patient/detection',
    component: Layout,
    hidden: true,
    redirect: '/patient/detection/index',
    meta: { title: '检测评估', icon: 'search', single: true },
    children: [
      {
        path: 'index',
        component: () => import('@/views/patient/detection/index'),
        name: 'DetectionIndex',
        meta: { title: '检测评估' }
      },
      // 所有子页面全部放在children内部，加 hidden:true 侧边不显示菜单
      {
        path: 'geriatricsyndrome',
        component: () => import('@/views/patient/detection/geriatricsyndrome'),
        name: 'GeriatricSyndrome',
        hidden: true,
        meta: { title: '老年综合评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sarcopeniaSyndrome',
        component: () => import('@/views/patient/detection/sarcopeniaSyndrome'),
        name: 'SarcopeniaSyndrome',
        hidden: true,
        meta: { title: '肌少症综合评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize',
        component: () => import('@/views/patient/detection/sCustomize'),
        name: 'SCustomize',
        hidden: true,
        meta: { title: '肌少症自定义评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/sSarcF',
        component: () => import('@/views/patient/detection/sCustomize/sSarcF.vue'),
        name: 'SSarcF',
        hidden: true,
        meta: { title: 'SARC-F肌少症筛查', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/sCalfMeasure',
        component: () => import('@/views/patient/detection/sCustomize/sCalfMeasure.vue'),
        name: 'SCalfMeasure',
        hidden: true,
        meta: { title: '小腿围测量', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/gripStrength',
        component: () => import('@/views/patient/detection/sCustomize/gripStrength.vue'),
        name: 'GripStrength',
        hidden: true,
        meta: { title: '握力测量', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/situp',
        component: () => import('@/views/patient/detection/sCustomize/situp.vue'),
        name: 'SitUp',
        hidden: true,
        meta: { title: '5次起坐测试', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/pace',
        component: () => import('@/views/patient/detection/sCustomize/pace.vue'),
        name: 'SPace',
        hidden: true,
        meta: { title: '6米步速测量', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/balanceStand',
        component: () => import('@/views/patient/detection/sCustomize/balanceStand.vue'),
        name: 'BalanceStand',
        hidden: true,
        meta: { title: '站立平衡测试', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/bodyComposition',
        component: () => import('@/views/patient/detection/sCustomize/bodyComposition.vue'),
        name: 'BodyComposition',
        hidden: true,
        meta: { title: '人体成分测量', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sCustomize/sComprehensive',
        component: () => import('@/views/patient/detection/sCustomize/sComprehensive.vue'),
        name: 'STotalMeasure',
        hidden: true,
        meta: { title: '肌少症综合测量', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'sComprehensive',
        component: () => import('@/views/patient/detection/sComprehensive.vue'),
        name: 'SComprehensive',
        hidden: true,
        meta: { title: '肌少症综合评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'cga',
        component: () => import('@/views/patient/detection/cga'),
        name: 'Cga',
        hidden: true,
        meta: { title: 'CGA评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'comprehensive',
        component: () => import('@/views/patient/detection/comprehensive'),
        name: 'Comprehensive',
        hidden: true,
        meta: { title: '综合评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize',
        component: () => import('@/views/patient/detection/customize'),
        name: 'Customize',
        hidden: true,
        meta: { title: '自定义检测评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'basicInformation',
        component: () => import('@/views/patient/detection/customize/basicInformation.vue'),
        name: 'BasicInformationEntry',
        hidden: true,
        meta: { title: '基本情况', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/dailyLife',
        component: () => import('@/views/patient/detection/customize/dailyLife.vue'),
        name: 'DailyLife',
        hidden: true,
        meta: { title: '日常生活评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/instrumentalityLife',
        component: () => import('@/views/patient/detection/customize/instrumentalityLife.vue'),
        name: 'InstrumentalityLife',
        hidden: true,
        meta: { title: '工具生活评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/sppb',
        component: () => import('@/views/patient/detection/customize/sppb.vue'),
        name: 'Sppb',
        hidden: true,
        meta: { title: '简易体能评估', activeMenu: '/patient/detection/index' }
      },
      {
        path:'customize/visualAcuity',
        component: () => import('@/views/patient/detection/customize/visualAcuity.vue'),
        name: 'VisualAcuity',
        hidden: true,
        meta: { title: '视力视觉评估', activeMenu: '/patient/detection/index' }
      },
      {
        path:'customize/listening',
        component: () => import('@/views/patient/detection/customize/listening.vue'),
        name: 'Listening',
        hidden: true,
        meta: { title: '听力评估', activeMenu: '/patient/detection/index' }
      },
      {
        path:'customize/homeEnvironment',
        component: () => import('@/views/patient/detection/customize/homeEnvironment.vue'),
        name: 'HomeEnvironment',
        hidden: true,
        meta: { title: '居家环境评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/cognition',
        component: () => import('@/views/patient/detection/customize/cognition.vue'),
        name: 'Cognition',
        hidden: true,
        meta: { title: '认知评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/frail',
        component: () => import('@/views/patient/detection/customize/frail.vue'),
        name: 'Frail',
        hidden: true,
        meta: { title: '衰弱评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/urinaryIncontinence',
        component: () => import('@/views/patient/detection/customize/urinaryIncontinence.vue'),
        name: 'UrinaryIncontinence',
        hidden: true,
        meta: { title: '尿失禁评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/constipation',
        component: () => import('@/views/patient/detection/customize/constipation'),
        name: 'Constipation',
        hidden: true,
        meta: { title: '便秘评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/insomnia',
        component: () => import('@/views/patient/detection/customize/insomnia.vue'),
        name: 'Insomnia',
        hidden: true,
        meta: { title: '失眠评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/chronicPain',
        component: () => import('@/views/patient/detection/customize/chronicPain.vue'),
        name: 'ChronicPain',
        hidden: true,
        meta: { title: '慢性疼痛评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/pressureInjury',
        component: () => import('@/views/patient/detection/customize/pressureInjury.vue'),
        name: 'PressureInjury',
        hidden: true,
        meta: { title: '压力性损伤评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/swallow',
        component: () => import('@/views/patient/detection/customize/swallow.vue'),
        name: 'Swallowing',
        hidden: true,
        meta: { title: '吞咽评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/nutrition',
        component: () => import('@/views/patient/detection/customize/nutrition.vue'),
        name: 'Nutrition',
        hidden: true,
        meta: { title: '营养评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/microNutrition',
        component: () => import('@/views/patient/detection/customize/microNutrition.vue'),
        name: 'MicroNutrition',
        hidden: true,
        meta: { title: '微营养评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/delirium',
        component: () => import('@/views/patient/detection/customize/delirium.vue'),
        name: 'Delirium',
        hidden: true,
        meta: { title: '谵妄评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/depression',
        component: () => import('@/views/patient/detection/customize/depression.vue'),
        name: 'Depression',
        hidden: true,
        meta: { title: '抑郁评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/fall',
        component: () => import('@/views/patient/detection/customize/fall.vue'),
        name: 'Fall',
        hidden: true,
        meta: { title: '跌倒评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/medication',
        component: () => import('@/views/patient/detection/customize/medication.vue'),
        name: 'Medication',
        hidden: true,
        meta: { title: '多重用药评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/anxiety',
        component: () => import('@/views/patient/detection/customize/anxiety.vue'),
        name: 'Anxiety',
        hidden: true,
        meta: { title: '焦虑评估', activeMenu: '/patient/detection/index' }
      },
      {
        path: 'customize/basicInformation',
        component: () => import('@/views/patient/detection/customize/basicInformation.vue'),
        name: 'BasicInformation',
        hidden: true,
        meta: { title: '基本情况评估', activeMenu: '/patient/detection/index' }
      }
    ]
  },
  {
    path: '/report',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'cgareport',
        component: () => import('@/views/report/cgareport'),
        name: 'CgaReport',
        meta: { title: 'CGA评估报告', activeMenu: '/patient/detection/index' }
      }
    ]
  }
]

export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...dynamicRoutes],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

export default router
