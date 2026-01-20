import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../ProtectedRoute';
import { useAuthStore } from '@/store/authStore';

// Mock the auth store
vi.mock('@/store/authStore', () => ({
  useAuthStore: vi.fn(),
}));

const mockUseAuthStore = useAuthStore as unknown as ReturnType<typeof vi.fn>;

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderWithRouter = (
    initialPath: string,
    isAuthenticated: boolean,
    user: { rol: string } | null = null,
    allowedRoles?: string[]
  ) => {
    mockUseAuthStore.mockReturnValue({
      isAuthenticated,
      user,
    });

    return render(
      <MemoryRouter initialEntries={[initialPath]}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/" element={<div>Home Page</div>} />
          <Route
            path="/protected"
            element={
              <ProtectedRoute allowedRoles={allowedRoles as any}>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );
  };

  it('redirects to login when not authenticated', () => {
    renderWithRouter('/protected', false);
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('renders children when authenticated without role restriction', () => {
    renderWithRouter('/protected', true, { rol: 'ADMIN' });
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('renders children when user has allowed role', () => {
    renderWithRouter('/protected', true, { rol: 'ADMIN' }, ['ADMIN', 'PRODUCTOR']);
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('redirects to home when user does not have allowed role', () => {
    renderWithRouter('/protected', true, { rol: 'OPERADOR_PLANTA' }, ['ADMIN', 'PRODUCTOR']);
    expect(screen.getByText('Home Page')).toBeInTheDocument();
  });

  it('renders children when no allowedRoles specified', () => {
    renderWithRouter('/protected', true, { rol: 'OPERADOR_PLANTA' });
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
});
