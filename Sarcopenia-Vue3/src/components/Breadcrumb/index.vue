<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || index == levelList.length - 1" class="no-redirect">{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const levelList = ref([])

function getBreadcrumb() {
  const customBreadcrumb = getCustomBreadcrumb()
  if (customBreadcrumb.length) {
    levelList.value = customBreadcrumb
    return
  }

  // only show routes with meta.title
  let matched = []
  const pathNum = findPathNum(route.path)
  // multi-level menu
  if (pathNum > 2) {
    const reg = /\/\w+/gi
    const pathList = route.path.match(reg).map((item, index) => {
      if (index !== 0) item = item.slice(1)
      return item
    })
    getMatched(pathList, permissionStore.defaultRoutes, matched)
    if (!matched.length) {
      matched = route.matched.filter((item) => item.meta && item.meta.title)
    }
  } else {
    matched = route.matched.filter((item) => item.meta && item.meta.title)
  }
  // 判断是否为首页
  if (!isDashboard(matched[0])) {
    matched = [{ path: "/index", meta: { title: "首页" } }].concat(matched)
  }
  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}

function getCustomBreadcrumb() {
  const map = {
    '/patient/detection/index': ['检测评估'],
    '/patient/detection/geriatricsyndrome': ['检测评估', '老年综合评估'],
    '/patient/detection/cga': ['检测评估', '老年综合评估', 'CGA评估'],
    '/patient/detection/comprehensive': ['检测评估', '老年综合评估', '综合评估'],
    '/patient/detection/customize': ['检测评估', '老年综合评估', '自定义检测评估'],
    '/patient/detection/basicInformation': ['检测评估', '老年综合评估', '基本情况'],
    '/patient/detection/customize/basicInformation': ['检测评估', '老年综合评估', '自定义检测评估', '基本情况评估'],
    '/report/cgareport': ['检测评估', '老年综合评估', 'CGA评估报告']
  }
  const assessmentTitleMap = {
    dailyLife: '日常生活评估',
    instrumentalityLife: '工具生活评估',
    sppb: '简易体能评估',
    visualAcuity: '视力视觉评估',
    listening: '听力评估',
    homeEnvironment: '居家环境评估',
    cognition: '认知评估',
    frail: '衰弱评估',
    urinaryIncontinence: '尿失禁评估',
    constipation: '便秘评估',
    insomnia: '失眠评估',
    chronicPain: '慢性疼痛评估',
    pressureInjury: '压力性损伤评估',
    swallow: '吞咽评估',
    nutrition: '营养评估',
    microNutrition: '微营养评估',
    delirium: '谵妄评估',
    depression: '抑郁评估',
    fall: '跌倒评估',
    medication: '多重用药评估',
    anxiety: '焦虑评估'
  }
  const path = route.path.replace(/\/$/, '')
  let titles = map[path]
  if (!titles && path.startsWith('/patient/detection/customize/')) {
    const key = path.split('/').pop()
    if (assessmentTitleMap[key]) {
      titles = ['检测评估', '老年综合评估', '自定义检测评估', assessmentTitleMap[key]]
    }
  }
  if (!titles) {
    return []
  }
  return [{ path: '/index', redirect: 'noRedirect', meta: { title: '首页' } }].concat(titles.map((title) => ({
    path: '',
    redirect: 'noRedirect',
    meta: { title }
  })))
}
function findPathNum(str, char = "/") {
  let index = str.indexOf(char)
  let num = 0
  while (index !== -1) {
    num++
    index = str.indexOf(char, index + 1)
  }
  return num
}
function getMatched(pathList, routeList, matched) {
  let data = routeList.find(item => item.path == pathList[0] || (item.name += '').toLowerCase() == pathList[0])
  if (data) {
    matched.push(data)
    if (data.children && pathList.length) {
      pathList.shift()
      getMatched(pathList, data.children, matched)
    }
  }
}
function isDashboard(route) {
  const name = route && route.name
  if (!name) {
    return false
  }
  return name.trim() === 'Index'
}
function handleLink(item) {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

watchEffect(() => {
  // if you go to the redirect page, do not update the breadcrumbs
  if (route.path.startsWith('/redirect/')) {
    return
  }
  getBreadcrumb()
})
getBreadcrumb()
</script>

<style lang='scss' scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>
