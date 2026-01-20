import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Notification, NotificationType } from '@/types';

interface NotificationStore {
  notifications: Notification[];
  unreadCount: number;
  addNotification: (
    type: NotificationType,
    title: string,
    message: string,
    link?: string
  ) => void;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  removeNotification: (id: string) => void;
  clearAll: () => void;
}

export const useNotificationStore = create<NotificationStore>()(
  persist(
    (set) => ({
      notifications: [],
      unreadCount: 0,

      addNotification: (type, title, message, link) => {
        const notification: Notification = {
          id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          type,
          title,
          message,
          timestamp: new Date(),
          read: false,
          link,
        };

        set((state) => ({
          notifications: [notification, ...state.notifications].slice(0, 50), // Max 50 notificaciones
          unreadCount: state.unreadCount + 1,
        }));
      },

      markAsRead: (id) => {
        set((state) => {
          const notification = state.notifications.find((n) => n.id === id);
          if (notification && !notification.read) {
            return {
              notifications: state.notifications.map((n) =>
                n.id === id ? { ...n, read: true } : n
              ),
              unreadCount: Math.max(0, state.unreadCount - 1),
            };
          }
          return state;
        });
      },

      markAllAsRead: () => {
        set((state) => ({
          notifications: state.notifications.map((n) => ({ ...n, read: true })),
          unreadCount: 0,
        }));
      },

      removeNotification: (id) => {
        set((state) => {
          const notification = state.notifications.find((n) => n.id === id);
          return {
            notifications: state.notifications.filter((n) => n.id !== id),
            unreadCount: notification && !notification.read
              ? Math.max(0, state.unreadCount - 1)
              : state.unreadCount,
          };
        });
      },

      clearAll: () => {
        set({ notifications: [], unreadCount: 0 });
      },
    }),
    {
      name: 'notifications-storage',
      partialize: (state) => ({
        notifications: state.notifications.slice(0, 20), // Solo persistir las últimas 20
      }),
    }
  )
);

// Hook helper para crear notificaciones desde cualquier parte de la app
export const notify = {
  success: (title: string, message: string, link?: string) => {
    useNotificationStore.getState().addNotification('success', title, message, link);
  },
  error: (title: string, message: string, link?: string) => {
    useNotificationStore.getState().addNotification('error', title, message, link);
  },
  warning: (title: string, message: string, link?: string) => {
    useNotificationStore.getState().addNotification('warning', title, message, link);
  },
  info: (title: string, message: string, link?: string) => {
    useNotificationStore.getState().addNotification('info', title, message, link);
  },
};

export default useNotificationStore;
