import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import login from '@/views/login'
import index from '@/views/index'
import man from '@/views/man'
import console from '@/views/console'
import chemical from '@/views/chemical'
import storage from '@/views/storage'
import outbound from '@/views/outbound'
import inventory from '@/views/inventory'
import warning from '@/views/warning'
import UseRecord from '@/views/UseRecord'
import permission from '@/views/permission'
import Register from '@/views/Register'
Vue.use(Router)

const router = new Router({
  routes: [
    // 路由重定向
    {
      path: '/',
      redirect: '/login'
    },
    {
      path:'/HelloWorld',
      name:'HelloWorld',
      component:HelloWorld
    },
    {
      path: '/login',
      name: 'login',
      component: login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/index',
      name: 'index',
      component:index,
      redirect: '/console',
      children: [
        {
          path: '/console',
          name: 'console',
          component: console
        },
        {
          path: '/man',
          name: 'man',
          component: man,
          meta: {
            requiresAdmin: true,  // 需要管理员权限
            title: '员工管理'
          }
        },
        {
          path: '/chemical',
          name: 'chemical',
          component: chemical,
          meta: { title: '危化品信息管理' }
        },
        {
          path: '/inventory',
          name: 'inventory',
          component: inventory,
          meta: { title: '库存监控' }
        },
        {
          path: '/storage',
          name: 'storage',
          component: storage,
          meta: { title: '入库管理' }
        },
        {
          path: '/outbound',
          name: 'outbound',
          component: outbound,
          meta: {
            requiresAdmin: true,  // 需要管理员权限
            title: '出库管理'
          }
        },
        {
          path: '/warning',
          name: 'warning',
          component: warning,
          meta: {
            requiresAdmin: true,  // 需要管理员权限
            title: '安全预警'
          }
        },
        {
          path: '/UseRecord',
          name: 'UseRecord',
          component: UseRecord,
          meta: { title: '使用记录' }
        },
        {
          path: '/permission',
          name: 'permission',
          component: permission,
          meta: {
            requiresAdmin: true,  // 需要管理员权限
            title: '权限管理'
          }
        }
      ]
    }]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 登录和注册页面不需要验证
  if (to.path === '/login' || to.path === '/register') {
    next();
    return;
  }

  // 获取用户信息和令牌
  const userStr = sessionStorage.getItem('user');
  const token = sessionStorage.getItem('token');

  // 关闭验证
  // if (!userStr || !token) {
  //   // 未登录或令牌丢失，跳转到登录页
  //   Vue.prototype.$message.warning('请先登录');
  //   next('/login');
  //   return;
  // }

  try {
    const user = JSON.parse(userStr);

    // 检查是否需要管理员权限
    if (to.meta && to.meta.requiresAdmin && user.userType !== 1) {
      // 普通用户尝试访问管理员页面，跳转到首页
      Vue.prototype.$message.error('您没有权限访问该页面');
      next('/console');
      return;
    }

    // 正常访问
    next();
  } catch (e) {
    console.error('解析用户信息失败', e);
    sessionStorage.removeItem('user');
    sessionStorage.removeItem('token');
    Vue.prototype.$message.error('登录信息已失效，请重新登录');
    next('/login');
  }
});

export default router
