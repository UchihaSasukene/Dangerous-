<template>
  <div class="login-container">
    <el-form :model="ruleForm2" :rules="rules2"
             status-icon
             ref="ruleForm2"
             label-position="left"
             label-width="0px"
             class="demo-ruleForm login-page">
      <h3 class="title">危化品信息管理系统登录</h3>
      <el-form-item prop="username">
        <el-input type="text"
                  v-model="ruleForm2.username"
                  auto-complete="off"
                  placeholder="用户名"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input type="password"
                  v-model="ruleForm2.password"
                  auto-complete="off"
                  placeholder="密码"
        ></el-input>
      </el-form-item>

      <!-- 添加用户类型选择 -->
      <el-form-item prop="userType">
        <el-radio-group v-model="ruleForm2.userType">
          <el-radio :label="0">普通用户</el-radio>
          <el-radio :label="1">管理员</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-checkbox
        v-model="checked"
        class="rememberme"
      >记住密码
      </el-checkbox>
      <el-form-item style="width:100%;">
        <el-button type="primary" style="width:100%;" @click="handleSubmit" :loading="logining">登录</el-button>
      </el-form-item>

      <!-- 添加注册链接 -->
      <div class="register-link">
        <router-link to="/register">没有账号？立即注册</router-link>
      </div>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: "login",
  data(){
    return {
      logining: false,
      ruleForm2: {
        username: 'admin',
        password: '123456',
        userType: 1 // 默认为管理员
      },
      rules2: {
        username: [{required: true, message: '请输入邮箱', trigger: 'blur'}],
        password: [{required: true, message: '请输入密码', trigger: 'blur'}],
        userType: [{required: true, message: '请选择用户类型', trigger: 'change'}]
      },
      checked: false
    }
  },
  methods: {
    handleSubmit(event){
      this.$refs.ruleForm2.validate((valid) => {
        if(valid){
          this.logining = true;

          // 构建登录请求数据
          const loginData = {
            email: this.ruleForm2.username,
            password: this.ruleForm2.password,
            userType: this.ruleForm2.userType
          };

          // 发送登录请求
          this.$http.post('/user/login', loginData)
            .then(response => {
              this.logining = false;
              if (response.data && response.data.code === 200) {
                // 登录成功
                const loginResponse = response.data.data;
                const userData = loginResponse.user || loginResponse;
                const token = loginResponse.token;

                if (!userData || !token) {
                  this.$message.error('登录响应数据格式错误');
                  return;
                }

                // 将用户信息和token保存到sessionStorage
                sessionStorage.setItem('user', JSON.stringify(userData));
                sessionStorage.setItem('token', token);

                this.$router.push({path: '/index'});
                this.$message.success('登录成功');
              } else {
                // 登录失败
                this.$message.error(response.data.message || '登录失败');
              }
            })
            .catch(error => {
              this.logining = false;
              if (error.response && error.response.data) {
                this.$message.error(error.response.data.message || '登录失败');
              } else {
                this.$message.error('网络错误，请稍后重试');
              }
              console.error('登录错误:', error);
            });
        }else{
          console.log('error submit!');
          return false;
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("../assets/loginimge.png");
  background-size: cover;
  background-position: center;
}
.login-page {
  -webkit-border-radius: 5px;
  border-radius: 5px;
  width: 350px;
  padding: 35px 35px 15px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
  margin: 0 auto;
  position: relative;
}

label.el-checkbox.rememberme {
  margin: 0px 0px 15px;
  text-align: left;
}

.title{
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.register-link {
  text-align: right;
  margin-top: 15px;
  font-size: 14px;
}

.register-link a {
  color: #409EFF;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>


