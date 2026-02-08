import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";
import {createContactPath, homePath, settingsPath} from "@/router/paths.ts";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: homePath,
      name: 'home',
      component: HomeView,
    },
    {
      path: settingsPath,
      name: 'settings',
      component: () => import('../views/Settings.vue'),
    },
    {
      path: createContactPath,
      name: 'contact-creation',
      component: () => import('../views/ContactCreation.vue')
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

export default router
