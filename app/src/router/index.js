import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    meta: {
      title: 'Multi-sig Minter'
    },
    path: '/',
    name: 'home',
    component: () => import(/* webpackChunkName: "tables" */ '../views/MultisigMint')
  },
  {
    meta: {
      title: 'Multi-sig Minter'
    },
    path: '/multisig-minter',
    name: 'multisig-minter',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "tables" */ '../views/MultisigMint')
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior (to, from, savedPosition) {
    return savedPosition || { x: 0, y: 0 }
  }
})

export default router
