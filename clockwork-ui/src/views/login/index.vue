<template>
    <div class="login-container">
        <div class="login-form">
            <div class="login-title">欢迎登录Clockwork</div>
            <el-form :model="loginForm" :rules="loginRules" class="demo-loginForm" label-width="0px" ref="loginForm">
                <el-form-item prop="username">
                    <el-input
                        auto-complete="on"
                        placeholder="请输入用户名"
                        prefix-icon="el-icon-user"
                        tabindex="1"
                        type="text"
                        v-model="loginForm.username">
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input
                        :key="passwordType"
                        :type="passwordType"
                        @keyup.enter.native="handleLogin"
                        placeholder="请输入密码"
                        prefix-icon="el-icon-lock"
                        tabindex="2"
                        v-model="loginForm.password">
                    </el-input>
                    <span @click="showPwd" class="show-pwd">
                      <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'"/>
                    </span>
                </el-form-item>
                <div class="login-btn">
                    <el-button @click="handleLogin" type="primary">登录</el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script>

    export default {
        name: 'Login',
        data() {
            return {
                loginForm: {
                    username: '',
                    password: ''
                },
                loginRules: {
                    username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
                    password: [{required: true, message: '请输入密码', trigger: 'blur'}]
                },
                loading: false,
                passwordType: 'password',
                redirect: undefined
            }
        },
        watch: {
            $route: {
                handler: function (route) {
                    this.redirect = route.query && route.query.redirect
                },
                immediate: true
            }
        },
        methods: {
            showPwd() {
                if (this.passwordType === 'password') {
                    this.passwordType = ''
                } else {
                    this.passwordType = 'password'
                }
                this.$nextTick(() => {
                    this.$refs.password.focus()
                })
            },
            handleLogin() {
                let _self = this;
                _self.$refs.loginForm.validate(valid => {
                    if (valid) {
                        this.loading = true;
                        this.$store.dispatch('user/login', this.loginForm).then(() => {
                            this.$router.push({path: this.redirect || '/'});
                            this.loading = false
                        }).catch(err => {
                                _self.$message({
                                    message: err,
                                    type: 'warning',
                                    duration: 5000
                                });
                                this.loading = false
                            }
                        )
                    } else {
                        console.log('error submit!!');
                        return false
                    }
                })
            }
        }
    }
</script>


<style scoped>
    .login-container {
        position: relative;
        background: url("../../assets/images/login_bg.jpg") no-repeat center center;
        width: 100%;
        height: 100%;
    }

    .login-form {
        position: absolute;
        left: 50%;
        top: 50%;
        width: 480px;
        height: 275px;
        margin: -150px 0 0 -240px;
        padding: 25px;
        border-radius: 5px;
        background: rgba(255, 255, 255, 0.9);
    }

    .login-title {
        /* position: absolute;
        top:50%;
        width:100%;
        margin-top: -230px; */
        text-align: center;
        font-size: 20px;
        color: #000;
        padding-bottom: 20px;

    }

    .login-btn {
        text-align: center;
    }

    .login-btn button {
        width: 100%;
        height: 36px;
        margin-top: 12px;
    }

    .show-pwd {
        position: absolute;
        right: 10px;
        top: 7px;
        font-size: 16px;
        cursor: pointer;
        user-select: none;
    }
</style>
