import { describe, it, expect, beforeEach } from 'vitest';
import { useNotificationStore, notify } from '../notificationStore';

describe('notificationStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useNotificationStore.setState({
      notifications: [],
      unreadCount: 0,
    });
  });

  it('should add a notification', () => {
    const { addNotification } = useNotificationStore.getState();

    addNotification('success', 'Test Title', 'Test message');

    const { notifications, unreadCount } = useNotificationStore.getState();
    expect(notifications).toHaveLength(1);
    expect(notifications[0].title).toBe('Test Title');
    expect(notifications[0].message).toBe('Test message');
    expect(notifications[0].type).toBe('success');
    expect(notifications[0].read).toBe(false);
    expect(unreadCount).toBe(1);
  });

  it('should add multiple notifications', () => {
    const { addNotification } = useNotificationStore.getState();

    addNotification('info', 'Info 1', 'Message 1');
    addNotification('warning', 'Warning 1', 'Message 2');
    addNotification('error', 'Error 1', 'Message 3');

    const { notifications, unreadCount } = useNotificationStore.getState();
    expect(notifications).toHaveLength(3);
    expect(unreadCount).toBe(3);
    // Latest notification should be first
    expect(notifications[0].title).toBe('Error 1');
  });

  it('should mark a notification as read', () => {
    const { addNotification, markAsRead } = useNotificationStore.getState();

    addNotification('info', 'Test', 'Message');
    const { notifications } = useNotificationStore.getState();
    const notificationId = notifications[0].id;

    markAsRead(notificationId);

    const state = useNotificationStore.getState();
    expect(state.notifications[0].read).toBe(true);
    expect(state.unreadCount).toBe(0);
  });

  it('should not decrease unreadCount when marking already read notification', () => {
    const { addNotification, markAsRead } = useNotificationStore.getState();

    addNotification('info', 'Test', 'Message');
    const { notifications } = useNotificationStore.getState();
    const notificationId = notifications[0].id;

    markAsRead(notificationId);
    markAsRead(notificationId); // Mark again

    const state = useNotificationStore.getState();
    expect(state.unreadCount).toBe(0);
  });

  it('should mark all notifications as read', () => {
    const { addNotification, markAllAsRead } = useNotificationStore.getState();

    addNotification('info', 'Test 1', 'Message 1');
    addNotification('warning', 'Test 2', 'Message 2');

    markAllAsRead();

    const state = useNotificationStore.getState();
    expect(state.notifications.every((n) => n.read)).toBe(true);
    expect(state.unreadCount).toBe(0);
  });

  it('should remove a notification', () => {
    const { addNotification, removeNotification } = useNotificationStore.getState();

    addNotification('info', 'Test', 'Message');
    const { notifications } = useNotificationStore.getState();
    const notificationId = notifications[0].id;

    removeNotification(notificationId);

    const state = useNotificationStore.getState();
    expect(state.notifications).toHaveLength(0);
    expect(state.unreadCount).toBe(0);
  });

  it('should clear all notifications', () => {
    const { addNotification, clearAll } = useNotificationStore.getState();

    addNotification('info', 'Test 1', 'Message 1');
    addNotification('warning', 'Test 2', 'Message 2');

    clearAll();

    const state = useNotificationStore.getState();
    expect(state.notifications).toHaveLength(0);
    expect(state.unreadCount).toBe(0);
  });

  it('should include link in notification', () => {
    const { addNotification } = useNotificationStore.getState();

    addNotification('info', 'Test', 'Message', '/test-link');

    const { notifications } = useNotificationStore.getState();
    expect(notifications[0].link).toBe('/test-link');
  });

  it('should limit notifications to 50', () => {
    const { addNotification } = useNotificationStore.getState();

    // Add 55 notifications
    for (let i = 0; i < 55; i++) {
      addNotification('info', `Test ${i}`, `Message ${i}`);
    }

    const { notifications } = useNotificationStore.getState();
    expect(notifications).toHaveLength(50);
  });

  it('should not change unreadCount when removing read notification', () => {
    const { addNotification, markAsRead, removeNotification } = useNotificationStore.getState();

    addNotification('info', 'Test 1', 'Message 1');
    addNotification('info', 'Test 2', 'Message 2');

    const { notifications } = useNotificationStore.getState();
    const notificationId = notifications[0].id;

    markAsRead(notificationId);
    expect(useNotificationStore.getState().unreadCount).toBe(1);

    removeNotification(notificationId);
    expect(useNotificationStore.getState().unreadCount).toBe(1);
    expect(useNotificationStore.getState().notifications).toHaveLength(1);
  });
});

describe('notify helper', () => {
  beforeEach(() => {
    useNotificationStore.setState({
      notifications: [],
      unreadCount: 0,
    });
  });

  it('should create success notification', () => {
    notify.success('Success Title', 'Success message');

    const { notifications } = useNotificationStore.getState();
    expect(notifications).toHaveLength(1);
    expect(notifications[0].type).toBe('success');
    expect(notifications[0].title).toBe('Success Title');
  });

  it('should create error notification', () => {
    notify.error('Error Title', 'Error message');

    const { notifications } = useNotificationStore.getState();
    expect(notifications).toHaveLength(1);
    expect(notifications[0].type).toBe('error');
    expect(notifications[0].title).toBe('Error Title');
  });

  it('should create warning notification', () => {
    notify.warning('Warning Title', 'Warning message');

    const { notifications } = useNotificationStore.getState();
    expect(notifications).toHaveLength(1);
    expect(notifications[0].type).toBe('warning');
    expect(notifications[0].title).toBe('Warning Title');
  });

  it('should create info notification', () => {
    notify.info('Info Title', 'Info message');

    const { notifications } = useNotificationStore.getState();
    expect(notifications).toHaveLength(1);
    expect(notifications[0].type).toBe('info');
    expect(notifications[0].title).toBe('Info Title');
  });

  it('should create notification with link', () => {
    notify.success('Title', 'Message', '/dashboard');

    const { notifications } = useNotificationStore.getState();
    expect(notifications[0].link).toBe('/dashboard');
  });
});
