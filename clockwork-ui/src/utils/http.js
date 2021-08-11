import axios from 'axios'
import store from '../store'
import qs from 'qs'
import {MessageBox} from 'element-ui'
import {setAccessToken} from "@/utils/auth";
import {getRefreshToken} from "@/store/modules/user";

// axios config
axios.defaults.timeout = 50000;

// request interceptor
axios.interceptors.request.use(
    config => {
        return config
    },
    err => {
        return Promise.reject(err)
    },
);

// response interceptor
axios.interceptors.response.use(
    /**
     * If you want to get http information such as headers or status
     * Please return  response => response
     */

    /**
     * Determine the request status by custom code
     * Here is just an example
     * You can also judge the status by HTTP Status Code
     */
    response => {
        return response.data
    },
    error => {
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    if ("accessTokenExpired" === error.response.data) {
                        // accessToken过期,系统自动通过refreshToken 获取最新accessToken
                        refreshToken();
                    } else if ("tokenError" === error.response.data || "refreshTokenExpired" === error.response.data) {
                        // clockwork/token 校验失败,重新登录
                        MessageBox.confirm('您已注销，您可以取消停留在该页上，或重新登录', '登陆信息校验失败！', {
                            confirmButtonText: '重新登陆',
                            cancelButtonText: '取消',
                            type: 'warning'
                        }).then(() => {
                            store.dispatch('user/resetAccessToken').then(() => {
                                location.reload()
                            })
                        })
                    }
            }
            return Promise.reject(new Error(error.response.msg || 'Error'))
        }
        return Promise.reject(error)
    },
);


function refreshToken() {
    let refreshToken = getRefreshToken();
    axios.post('/clockwork/web/login/refresh', qs.stringify({refreshToken: refreshToken}))
        .then(response => {
            if (response.code === 'OK') {
                store.commit('SET_ACCESS_TOKEN', response.data.split('__')[0]);
                store.commit('SET_REFRESH_TOKEN', response.data.split('__')[1]);
                setAccessToken(response.data.split('__')[0]);
            } else {
                //clockwork/token 校验失败,重新登录
                MessageBox.confirm('您登陆信息已过期，您可以取消停留在该页上，或重新登录', '登陆信息校验失败！', {
                    confirmButtonText: '重新登陆',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    store.dispatch('user/resetAccessToken').then(() => {
                        location.reload()
                    })
                })
            }
        }).catch(error => {
        console.log(error);
    });
}

export default axios
