import Vue from "vue";

import "normalize.css/normalize.css"; // A modern alternative to CSS resets
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import locale from "element-ui/lib/locale/lang/en"; // lang i18n
import "@/styles/index.scss"; // global css
import App from "./App";
import store from "./store";
import router from "./router";
import axios from "./utils/http";
import qs from "qs";
import filters from "@/utils/filters"; // 注册filter
Object.keys(filters).forEach(k => Vue.filter(k, filters[k]));

import { InfiniteScroll } from "mint-ui";
import echarts from "echarts";

import "@/icons"; // icon
import "@/permission"; // permission control

import global_ from "./components/Global";
import eltTransfer from "elt-transfer"; // transfer分页

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === "production") {
  const { mockXHR } = require("../mock");
  mockXHR();
}

// set ElementUI lang to EN
Vue.use(ElementUI, { locale });
Vue.use(InfiniteScroll);
Vue.use(eltTransfer);

// 如果想要中文版 element-ui，按如下方式声明
// Vue.use(ElementUI)

Vue.config.productionTip = false;
Vue.prototype.axios = axios;
Vue.prototype.qs = qs;
Vue.prototype.echarts = echarts;
Vue.prototype.GLOBAL = global_; // 挂载global到Vue实例上面

new Vue({
  el: "#app",
  router,
  store,
  render: h => h(App)
});
