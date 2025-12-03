// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from 'axios';

// 配置axios
axios.defaults.baseURL = 'http://localhost:9090'; // 设置请求的基础URL，确保指向后端服务地址
axios.defaults.timeout = 10000; // 设置超时时间

// 请求拦截器
axios.interceptors.request.use(
  config => {
    // 从sessionStorage中获取token
    const token = sessionStorage.getItem('token');
    if (token) {
      // 添加token到请求头
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('发送请求:', config.url, config.params || config.data);
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
axios.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.url, response.data);
    return response;
  },
  error => {
    console.error('请求错误:', error.config ? error.config.url : '未知', error.message);
    if (error.response) {
      // 处理401未授权错误
      if (error.response.status === 401) {
        // 清除用户信息
        sessionStorage.removeItem('user');
        // 跳转到登录页
        router.push('/login');
        Vue.prototype.$message.error('登录已过期，请重新登录');
      }
    }
    return Promise.reject(error);
  }
);

Vue.prototype.$http = axios
Vue.config.productionTip = false
Vue.use(ElementUI);
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
