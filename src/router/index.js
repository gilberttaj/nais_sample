import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/HomePage.vue')
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
    // Redirect any unmatched routes to home
    path: '/:catchAll(.*)',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
