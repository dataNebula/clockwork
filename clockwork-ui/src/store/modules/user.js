import {getInfo, login, logout} from '@/api/user'
import {getAccessToken, removeToken, setAccessToken} from '@/utils/auth'
import {resetRouter} from '@/router'

const getDefaultState = () => {
    return {
        accessToken: getAccessToken(),
        refreshToken: '',
        name: '',
        role: '',
        isAdmin: ''
    }
};

const state = getDefaultState();

const mutations = {
    RESET_STATE: (state) => {
        Object.assign(state, getDefaultState())
    },
    SET_ACCESS_TOKEN: (state, token) => {
        state.accessToken = token;
    },
    SET_REFRESH_TOKEN: (state, token) => {
        state.refreshToken = token;
    },
    SET_USER_NAME: (state, name) => {
        state.name = name;
    },
    SET_USER_ROLE: (state, role) => {
        state.role = role;
    },
    SET_USER_IS_ADMIN: (state, isAdmin) => {
        state.isAdmin = isAdmin;
    }
};

const actions = {
    // user login
    login({commit}, userInfo) {
        const {username, password} = userInfo;
        return new Promise((resolve, reject) => {
            login(username, password).then(response => {
                const {data} = response;
                if (data) {
                    commit('SET_ACCESS_TOKEN', data.split('__')[0]);
                    commit('SET_REFRESH_TOKEN', data.split('__')[1]);
                    setAccessToken(data.split('__')[0]);
                    resolve()
                } else {
                    throw '登录失败！请检测邮箱密码是否正确！';
                }
            }).catch(error => {
                console.log(error);
                reject(error)
            })
        })
    },

    // get user info
    getInfo({commit, state}) {
        return new Promise((resolve, reject) => {
            getInfo().then(response => {
                if (!response) {
                    return reject('Verification failed, please Login again.')
                }
                let name = response.data.userName;
                let role = response.data.roleName;
                let isAdmin = response.data.isAdmin;
                commit('SET_USER_NAME', name);
                commit('SET_USER_ROLE', role);
                commit('SET_USER_IS_ADMIN', isAdmin);
                resolve();
            }).catch(error => {
                reject(error)
            })
        })
    },

    // user logout
    logout({commit, state}) {
        return new Promise((resolve, reject) => {
            logout(state.accessToken, state.refreshToken).then(() => {
                removeToken(); // must remove  token  first
                resetRouter();
                commit('RESET_STATE');
                resolve()
            }).catch(error => {
                reject(error)
            })
        })
    },

    // remove token
    resetAccessToken({commit}) {
        return new Promise(resolve => {
            removeToken(); // must remove  token  first
            commit('RESET_STATE');
            resolve()
        })
    }
};

export default {
    namespaced: true,
    state,
    mutations,
    actions
}

export function getRefreshToken() {
    return state.refreshToken
}
