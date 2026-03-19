import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import 'vant/lib/index.css'; // 引入 Vant 样式
import { Button, Form, Field, CellGroup, Toast, RadioGroup, Radio } from 'vant';

const app = createApp(App);

app.use(router);
app.use(Button);
app.use(Form);
app.use(Field);
app.use(CellGroup);
app.use(RadioGroup);
app.use(Radio);
app.use(Toast);

app.mount('#app');
