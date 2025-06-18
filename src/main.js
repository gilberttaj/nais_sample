import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
import Amplify from 'aws-amplify'
import amplifyconfig from './amplifyconfiguration.json'

Amplify.configure(amplifyconfig)

const app = createApp(App)
app.use(router)
app.mount('#app')
