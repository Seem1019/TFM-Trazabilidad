import { describe, it, expect, beforeEach } from 'vitest';
import { useUIStore } from '../uiStore';

describe('uiStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useUIStore.setState({
      sidebarOpen: true,
      sidebarCollapsed: false,
    });
  });

  describe('initial state', () => {
    it('should have sidebar open by default', () => {
      const state = useUIStore.getState();
      expect(state.sidebarOpen).toBe(true);
    });

    it('should have sidebar not collapsed by default', () => {
      const state = useUIStore.getState();
      expect(state.sidebarCollapsed).toBe(false);
    });
  });

  describe('toggleSidebar', () => {
    it('should toggle sidebar open state from open to closed', () => {
      const { toggleSidebar } = useUIStore.getState();

      toggleSidebar();
      expect(useUIStore.getState().sidebarOpen).toBe(false);
    });

    it('should toggle sidebar open state from closed to open', () => {
      useUIStore.setState({ sidebarOpen: false });
      const { toggleSidebar } = useUIStore.getState();

      toggleSidebar();
      expect(useUIStore.getState().sidebarOpen).toBe(true);
    });
  });

  describe('setSidebarOpen', () => {
    it('should set sidebar open to true', () => {
      useUIStore.setState({ sidebarOpen: false });
      const { setSidebarOpen } = useUIStore.getState();

      setSidebarOpen(true);
      expect(useUIStore.getState().sidebarOpen).toBe(true);
    });

    it('should set sidebar open to false', () => {
      const { setSidebarOpen } = useUIStore.getState();

      setSidebarOpen(false);
      expect(useUIStore.getState().sidebarOpen).toBe(false);
    });
  });

  describe('toggleSidebarCollapsed', () => {
    it('should toggle sidebar collapsed state', () => {
      const { toggleSidebarCollapsed } = useUIStore.getState();

      toggleSidebarCollapsed();
      expect(useUIStore.getState().sidebarCollapsed).toBe(true);

      toggleSidebarCollapsed();
      expect(useUIStore.getState().sidebarCollapsed).toBe(false);
    });
  });

  it('should set sidebar collapsed directly', () => {
    useUIStore.setState({ sidebarCollapsed: true });
    expect(useUIStore.getState().sidebarCollapsed).toBe(true);

    useUIStore.setState({ sidebarCollapsed: false });
    expect(useUIStore.getState().sidebarCollapsed).toBe(false);
  });
});
