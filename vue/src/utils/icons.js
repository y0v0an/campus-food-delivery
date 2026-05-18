/**
 * 图标名称映射表
 * 统一管理项目中使用的图标名称
 */

export const Icons = {
  // 导航图标
  HOME: 'lucide:home',
  DASHBOARD: 'lucide:layout-dashboard',
  USER: 'lucide:user',
  USERS: 'lucide:users',
  SETTINGS: 'lucide:settings',
  STORE: 'lucide:store',
  SHOPPING_CART: 'lucide:shopping-cart',
  CLIPBOARD_LIST: 'lucide:clipboard-list',
  TICKET: 'lucide:ticket',
  UTENSILS: 'lucide:utensils',
  BAR_CHART: 'lucide:bar-chart-3',
  LINE_CHART: 'lucide:line-chart',
  PIE_CHART: 'lucide:pie-chart',

  // 操作图标
  ADD: 'lucide:plus',
  REMOVE: 'lucide:minus',
  CLOSE: 'lucide:x',
  CHECK: 'lucide:check',
  SEARCH: 'lucide:search',
  FILTER: 'lucide:filter',
  SORT: 'lucide:arrow-up-down',
  REFRESH: 'lucide:refresh-cw',
  DOWNLOAD: 'lucide:download',
  UPLOAD: 'lucide:upload',
  EDIT: 'lucide:pencil',
  DELETE: 'lucide:trash-2',
  SAVE: 'lucide:save',
  COPY: 'lucide:copy',
  PASTE: 'lucide:clipboard',

  // 方向图标
  ARROW_UP: 'lucide:arrow-up',
  ARROW_DOWN: 'lucide:arrow-down',
  ARROW_LEFT: 'lucide:arrow-left',
  ARROW_RIGHT: 'lucide:arrow-right',
  CHEVRON_UP: 'lucide:chevron-up',
  CHEVRON_DOWN: 'lucide:chevron-down',
  CHEVRON_LEFT: 'lucide:chevron-left',
  CHEVRON_RIGHT: 'lucide:chevron-right',

  // 状态图标
  SUCCESS: 'lucide:check-circle',
  ERROR: 'lucide:x-circle',
  WARNING: 'lucide:alert-triangle',
  INFO: 'lucide:info',
  LOADING: 'lucide:loader-2',
  CLOCK: 'lucide:clock',
  CALENDAR: 'lucide:calendar',

  // 用户相关
  USER_CIRCLE: 'lucide:user-circle',
  USER_CHECK: 'lucide:user-check',
  USER_PLUS: 'lucide:user-plus',
  USER_MINUS: 'lucide:user-minus',
  USERS_ROUND: 'lucide:users-round',

  // 商家相关
  STORE_CHECK: 'lucide:store-check',
  PACKAGE: 'lucide:package',
  TRUCK: 'lucide:truck',
  WRENCH: 'lucide:wrench',

  // 订单相关
  RECEIPT: 'lucide:receipt',
  FILE_TEXT: 'lucide:file-text',
  SCROLL: 'lucide:scroll',

  // 位置相关
  MAP_PIN: 'lucide:map-pin',
  LOCATION: 'lucide:location',
  COMPASS: 'lucide:compass',
  ROUTE: 'lucide:route',

  // 时间相关
  HOURGLASS: 'lucide:hourglass',
  TIMER: 'lucide:timer',

  // 财务相关
  WALLET: 'lucide:wallet',
  CREDIT_CARD: 'lucide:credit-card',
  DOLLAR_SIGN: 'lucide:dollar-sign',
  COINS: 'lucide:coins',

  // 通讯相关
  MAIL: 'lucide:mail',
  PHONE: 'lucide:phone',
  SEND: 'lucide:send',
  MESSAGE_SQUARE: 'lucide:message-square',

  // 安全相关
  SHIELD: 'lucide:shield',
  SHIELD_CHECK: 'lucide:shield-check',
  LOCK: 'lucide:lock',
  UNLOCK: 'lucide:unlock',
  KEY: 'lucide:key',
  EYE: 'lucide:eye',
  EYE_OFF: 'lucide:eye-off',

  // 媒体相关
  IMAGE: 'lucide:image',
  VIDEO: 'lucide:video',
  MIC: 'lucide:mic',
  MIC_OFF: 'lucide:mic-off',
  VOLUME: 'lucide:volume-2',
  VOLUME_OFF: 'lucide:volume-x',

  // 文件相关
  FOLDER: 'lucide:folder',
  FOLDER_OPEN: 'lucide:folder-open',
  FILE: 'lucide:file',
  FILE_TEXT: 'lucide:file-text',

  // 其他
  STAR: 'lucide:star',
  HEART: 'lucide:heart',
  BELL: 'lucide:bell',
  BOOKMARK: 'lucide:bookmark',
  TAG: 'lucide:tag',
  HASH: 'lucide:hash',
  LIST: 'lucide:list',
  GRID: 'lucide:grid',
  MENU: 'lucide:menu',
  MORE_HORIZONTAL: 'lucide:more-horizontal',
  MORE_VERTICAL: 'lucide:more-vertical',
  LOG_OUT: 'lucide:log-out',
  LOG_IN: 'lucide:log-in',
  SWITCH: 'lucide:switch-camera',

  // 拼单相关
  USERS_ROUND: 'lucide:users-round',
  USER_PLUS: 'lucide:user-plus',
  GROUP: 'lucide:users',

  // 骑手相关
  BICYCLE: 'lucide:bicycle',
  MOTO: 'lucide:moto',
  WRENCH: 'lucide:wrench',

  // 统计相关
  TRENDING_UP: 'lucide:trending-up',
  TRENDING_DOWN: 'lucide:trending-down',
  ACTIVITY: 'lucide:activity',

  // 搜索相关
  SEARCH_CHECK: 'lucide:search-check',
  SEARCH_X: 'lucide:search-x',

  // 菜单相关
  MENU_SQUARE: 'lucide:square-menu',
  LAYOUT_GRID: 'lucide:layout-grid',
  LAYOUT_LIST: 'lucide:layout-list',

  // 审核相关
  CHECK_SQUARE: 'lucide:check-square-2',
  X_SQUARE: 'lucide:x-square',

  // 个人中心
  ID_CARD: 'lucide:id-card',
  CREDIT_CARD: 'lucide:credit-card'
}

/**
 * 根据功能类型获取图标
 */
export const getIconByType = (type) => {
  const iconMap = {
    // 导航类型
    home: Icons.HOME,
    dashboard: Icons.DASHBOARD,
    profile: Icons.USER_CIRCLE,
    settings: Icons.SETTINGS,

    // 功能类型
    order: Icons.CLIPBOARD_LIST,
    product: Icons.PACKAGE,
    user: Icons.USER,
    merchant: Icons.STORE,
    rider: Icons.BICYCLE,
    coupon: Icons.TICKET,
    statistics: Icons.BAR_CHART,

    // 操作类型
    add: Icons.ADD,
    edit: Icons.EDIT,
    delete: Icons.DELETE,
    view: Icons.EYE,
    search: Icons.SEARCH,
    filter: Icons.FILTER,
    export: Icons.DOWNLOAD,
    import: Icons.UPLOAD,

    // 状态类型
    success: Icons.SUCCESS,
    error: Icons.ERROR,
    warning: Icons.WARNING,
    info: Icons.INFO,
    pending: Icons.CLOCK,
    processing: Icons.LOADING,

    // 方向类型
    up: Icons.ARROW_UP,
    down: Icons.ARROW_DOWN,
    left: Icons.ARROW_LEFT,
    right: Icons.ARROW_RIGHT
  }

  return iconMap[type] || Icons.HELP
}
