import axios from '@/utils/http'
import qs from 'qs'

export function login(username, password) {
    return axios.post(
        '/clockwork/web/login/in',
        qs.stringify({username: username.trim(), password: password})
    )
}

export function logout(accessToken, refreshToken) {
    return axios.post(
        '/clockwork/web/login/out',
        qs.stringify({accessToken: accessToken, refreshToken: refreshToken})
    )
}

export function getInfo() {
    return axios.get(
        '/clockwork/web/user/getUserInfo')
}

