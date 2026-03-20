import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/books' },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/books',
      name: 'books',
      component: () => import('../views/BooksView.vue'),
    },
    {
      path: '/books/:id',
      name: 'book-detail',
      component: () => import('../views/BookDetailView.vue'),
      props: true,
    },
    {
      path: '/me/loans',
      name: 'my-loans',
      component: () => import('../views/MyLoansView.vue'),
      meta: { requiresAuth: true, requiresReader: true },
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/books' },
        {
          path: 'books',
          name: 'admin-books',
          component: () => import('../views/admin/AdminBooksView.vue'),
        },
        {
          path: 'readers',
          name: 'admin-readers',
          component: () => import('../views/admin/AdminReadersView.vue'),
        },
        {
          path: 'loans',
          name: 'admin-loans',
          component: () => import('../views/admin/AdminLoansView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (auth.token && !auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
    }
  }

  if (to.meta.guestOnly) {
    if (auth.token && !auth.user) {
      try {
        await auth.fetchMe()
      } catch {
        auth.logout()
      }
    }
    if (auth.token && auth.user) {
      return { path: '/books' }
    }
    return true
  }

  if (to.meta.requiresAuth) {
    if (!auth.token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (!auth.user) {
      try {
        await auth.fetchMe()
      } catch {
        auth.logout()
        return { path: '/login', query: { redirect: to.fullPath } }
      }
    }
    if (to.meta.requiresAdmin && !auth.isAdmin) {
      return { path: '/books' }
    }
    if (to.meta.requiresReader && !auth.isReader) {
      return auth.isAdmin ? { path: '/admin' } : { path: '/books' }
    }
  }

  return true
})

export default router
