import { createRouter, createWebHistory } from 'vue-router'
import AuthValidation from '../components/organisms/AuthValidation.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/HomePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/signin',
    name: 'SignIn',
    component: () => import('../pages/SignInPage.vue')
  },
  {
    path: '/signup',
    name: 'SignUp',
    component: () => import('../pages/SignUpPage.vue')
  },
  {
    path: '/auth/google/callback',
    name: 'GoogleCallback',
    component: () => import('../components/organisms/GoogleCallback.vue'),
    props: route => ({ code: route.query.code })
  },
  {
    path: '/auth/validation',
    name: 'AuthValidation',
    component: AuthValidation,
  },
  {
    // Redirect any unmatched routes to home
    path: '/:catchAll(.*)',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard to check authentication
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('access_token') !== null
  
  if (to.matched.some(record => record.meta.requiresAuth) && !isAuthenticated) {
    // Redirect to login if trying to access a protected route without authentication
    next({ name: 'SignIn' })
  } else {
    next()
  }
})
export default router
