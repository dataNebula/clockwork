import Cookies from 'js-cookie'

const ACCESS_TOKEN_KEY = 'A-TOKEN';

export function getAccessToken() {
    return Cookies.get(ACCESS_TOKEN_KEY)
}

export function setAccessToken(token) {
    console.log(token);
    removeToken(ACCESS_TOKEN_KEY);
    return Cookies.set(ACCESS_TOKEN_KEY, token)
}

export function removeToken() {
    return Cookies.remove(ACCESS_TOKEN_KEY)
}
