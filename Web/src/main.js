import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// Vant
import Vant from 'vant'
import 'vant/lib/index.css'

// Rem 自适应
import 'amfe-flexible'

const app = createApp(App)
app.use(router)
app.use(Vant)
app.mount('#app')
