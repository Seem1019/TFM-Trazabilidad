import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Sidebar } from '../Sidebar';
import { useUIStore } from '@/store/uiStore';
import { useAuthStore } from '@/store/authStore';

// Mock the stores
vi.mock('@/store/uiStore', () => ({
  useUIStore: vi.fn(),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: vi.fn(),
}));

const mockUseUIStore = useUIStore as ReturnType<typeof vi.fn>;
const mockUseAuthStore = useAuthStore as ReturnType<typeof vi.fn>;

describe('Sidebar', () => {
  const defaultUIStore = {
    sidebarCollapsed: false,
    toggleSidebarCollapsed: vi.fn(),
  };

  const adminUser = {
    id: 1,
    email: 'admin@test.com',
    nombre: 'Admin',
    rol: 'ADMIN',
  };

  const operatorUser = {
    id: 2,
    email: 'operator@test.com',
    nombre: 'Operator',
    rol: 'OPERADOR_FINCA',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseUIStore.mockReturnValue(defaultUIStore);
    mockUseAuthStore.mockReturnValue({ user: adminUser });
  });

  const renderSidebar = () => {
    return render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );
  };

  describe('Rendering', () => {
    it('should render the sidebar with logo', () => {
      renderSidebar();

      // The logo text "Trazabilidad" appears in the header
      const logoText = screen.getAllByText('Trazabilidad');
      expect(logoText.length).toBeGreaterThanOrEqual(1);
    });

    it('should render all navigation groups when expanded', () => {
      renderSidebar();

      expect(screen.getByText('Principal')).toBeInTheDocument();
      expect(screen.getByText('Producción')).toBeInTheDocument();
      expect(screen.getByText('Empaque')).toBeInTheDocument();
      expect(screen.getByText('Logística')).toBeInTheDocument();
      // "Trazabilidad" appears both as logo and as nav group title
      const trazabilidadElements = screen.getAllByText('Trazabilidad');
      expect(trazabilidadElements.length).toBe(2); // Logo + nav group
      expect(screen.getByText('Administración')).toBeInTheDocument();
    });

    it('should render navigation items', () => {
      renderSidebar();

      // Production items
      expect(screen.getByText('Fincas')).toBeInTheDocument();
      expect(screen.getByText('Lotes')).toBeInTheDocument();
      expect(screen.getByText('Cosechas')).toBeInTheDocument();

      // Packaging items
      expect(screen.getByText('Recepciones')).toBeInTheDocument();
      expect(screen.getByText('Clasificación')).toBeInTheDocument();
      expect(screen.getByText('Etiquetas')).toBeInTheDocument();
      expect(screen.getByText('Pallets')).toBeInTheDocument();

      // Logistics items
      expect(screen.getByText('Envíos')).toBeInTheDocument();
    });
  });

  describe('Collapsed State', () => {
    it('should hide text labels when collapsed', () => {
      mockUseUIStore.mockReturnValue({
        ...defaultUIStore,
        sidebarCollapsed: true,
      });

      renderSidebar();

      // Group titles should not be visible when collapsed
      expect(screen.queryByText('Producción')).not.toBeInTheDocument();
      expect(screen.queryByText('Empaque')).not.toBeInTheDocument();
    });

    it('should toggle collapse state when button is clicked', () => {
      const toggleMock = vi.fn();
      mockUseUIStore.mockReturnValue({
        ...defaultUIStore,
        toggleSidebarCollapsed: toggleMock,
      });

      renderSidebar();

      const collapseButton = screen.getByRole('button', { name: /colapsar/i });
      fireEvent.click(collapseButton);

      expect(toggleMock).toHaveBeenCalledTimes(1);
    });
  });

  describe('Role-based Access', () => {
    it('should show admin-only items for admin users', () => {
      mockUseAuthStore.mockReturnValue({ user: adminUser });

      renderSidebar();

      expect(screen.getByText('Usuarios')).toBeInTheDocument();
    });

    it('should hide admin-only items for non-admin users', () => {
      mockUseAuthStore.mockReturnValue({ user: operatorUser });

      renderSidebar();

      expect(screen.queryByText('Usuarios')).not.toBeInTheDocument();
    });

    it('should show common items for all users', () => {
      mockUseAuthStore.mockReturnValue({ user: operatorUser });

      renderSidebar();

      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.getByText('Fincas')).toBeInTheDocument();
      expect(screen.getByText('Envíos')).toBeInTheDocument();
    });

    it('should handle null user', () => {
      mockUseAuthStore.mockReturnValue({ user: null });

      renderSidebar();

      // Should still render but without role-restricted items
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.queryByText('Usuarios')).not.toBeInTheDocument();
    });
  });

  describe('Navigation', () => {
    it('should have correct href for navigation items', () => {
      renderSidebar();

      expect(screen.getByRole('link', { name: /dashboard/i })).toHaveAttribute('href', '/');
      expect(screen.getByRole('link', { name: /fincas/i })).toHaveAttribute('href', '/fincas');
      expect(screen.getByRole('link', { name: /envíos/i })).toHaveAttribute('href', '/envios');
    });

    it('should render all expected links', () => {
      renderSidebar();

      const expectedLinks = [
        '/', // Dashboard
        '/fincas',
        '/lotes',
        '/cosechas',
        '/certificaciones',
        '/actividades',
        '/recepciones',
        '/clasificacion',
        '/etiquetas',
        '/pallets',
        '/control-calidad',
        '/envios',
        '/eventos',
        '/documentos',
        '/trazabilidad',
        '/usuarios', // Admin only
      ];

      expectedLinks.forEach((href) => {
        const link = document.querySelector(`a[href="${href}"]`);
        expect(link).toBeInTheDocument();
      });
    });
  });
});
