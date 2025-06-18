import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
import { Amplify } from 'aws-amplify'
import amplifyconfig from '../amplifyconfiguration.json'

// Create a new configuration object by spreading the imported config
// and overriding the oauth section as per the new requirements.
const newAmplifyConfig = {
  ...amplifyconfig,
  oauth: {
    ...amplifyconfig.oauth, // Keep other oauth settings like scope
    // Use the App Runner URL (the current host) as the domain for OAuth.
    // This makes the configuration portable and work automatically in production.
    domain: window.location.host,
    // Update redirect URIs to be dynamic based on the current origin
    redirectSignIn: `${window.location.origin}/auth/callback`,
    redirectSignOut: `${window.location.origin}/auth/signout`,
  }
};

Amplify.configure(newAmplifyConfig);

const app = createApp(App)
app.use(router)
app.mount('#app')
