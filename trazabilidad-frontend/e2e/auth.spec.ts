import { test, expect } from '@playwright/test';

/**
 * E2E tests for the authentication flow.
 * Tests login, logout, and session management.
 */

test.describe('Authentication Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the login page before each test
    await page.goto('/login');
  });

  test.describe('Login Page', () => {
    test('should display login form', async ({ page }) => {
      // Check that login form elements are visible
      await expect(page.getByRole('heading', { name: /iniciar sesión/i })).toBeVisible();
      await expect(page.getByLabel(/email/i)).toBeVisible();
      await expect(page.getByLabel(/contraseña/i)).toBeVisible();
      await expect(page.getByRole('button', { name: /ingresar|iniciar/i })).toBeVisible();
    });

    test('should show validation errors for empty form', async ({ page }) => {
      // Click login without filling form
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();

      // Check for validation messages
      await expect(page.getByText(/email.*obligatorio|requerido/i)).toBeVisible();
      await expect(page.getByText(/contraseña.*obligatoria|requerida/i)).toBeVisible();
    });

    test('should show error for invalid email format', async ({ page }) => {
      // Fill invalid email
      await page.getByLabel(/email/i).fill('invalid-email');
      await page.getByLabel(/contraseña/i).fill('password123');
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();

      // Check for email validation error
      await expect(page.getByText(/email.*válido|formato/i)).toBeVisible();
    });

    test('should show error for invalid credentials', async ({ page }) => {
      // Fill with wrong credentials
      await page.getByLabel(/email/i).fill('wrong@email.com');
      await page.getByLabel(/contraseña/i).fill('wrongpassword');
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();

      // Check for error message
      await expect(page.getByText(/credenciales.*inválidas|incorrectas/i)).toBeVisible({ timeout: 10000 });
    });

    test('should login successfully with valid credentials', async ({ page }) => {
      // Fill with valid credentials (test user)
      await page.getByLabel(/email/i).fill('admin@frutas.com');
      await page.getByLabel(/contraseña/i).fill('admin123');
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();

      // Should redirect to dashboard
      await expect(page).toHaveURL(/\/dashboard|\/$/);

      // Should show user info in header/sidebar
      await expect(page.getByText(/admin|dashboard/i)).toBeVisible({ timeout: 10000 });
    });

    test('should show password toggle functionality', async ({ page }) => {
      const passwordInput = page.getByLabel(/contraseña/i);

      // Initially password should be hidden
      await expect(passwordInput).toHaveAttribute('type', 'password');

      // Click toggle button if exists
      const toggleButton = page.getByRole('button', { name: /mostrar|ocultar/i });
      if (await toggleButton.isVisible()) {
        await toggleButton.click();
        await expect(passwordInput).toHaveAttribute('type', 'text');
      }
    });
  });

  test.describe('Protected Routes', () => {
    test('should redirect to login when accessing protected route without auth', async ({ page }) => {
      // Try to access dashboard directly
      await page.goto('/dashboard');

      // Should redirect to login
      await expect(page).toHaveURL(/\/login/);
    });

    test('should redirect to login when accessing fincas without auth', async ({ page }) => {
      await page.goto('/produccion/fincas');
      await expect(page).toHaveURL(/\/login/);
    });

    test('should redirect to login when accessing envios without auth', async ({ page }) => {
      await page.goto('/logistica/envios');
      await expect(page).toHaveURL(/\/login/);
    });
  });

  test.describe('Session Management', () => {
    test('should persist session after page reload', async ({ page }) => {
      // Login first
      await page.getByLabel(/email/i).fill('admin@frutas.com');
      await page.getByLabel(/contraseña/i).fill('admin123');
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();

      // Wait for redirect to dashboard
      await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });

      // Reload the page
      await page.reload();

      // Should still be on dashboard (session persisted)
      await expect(page).toHaveURL(/\/dashboard|\/$/, { timeout: 10000 });
    });

    test('should logout successfully', async ({ page }) => {
      // Login first
      await page.getByLabel(/email/i).fill('admin@frutas.com');
      await page.getByLabel(/contraseña/i).fill('admin123');
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();

      // Wait for redirect to dashboard
      await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });

      // Click on user menu and logout
      const userMenu = page.getByRole('button', { name: /usuario|perfil|admin/i });
      if (await userMenu.isVisible()) {
        await userMenu.click();
        await page.getByRole('menuitem', { name: /cerrar sesión|logout|salir/i }).click();
      } else {
        // Try alternative logout button
        await page.getByRole('button', { name: /cerrar sesión|logout|salir/i }).click();
      }

      // Should redirect to login
      await expect(page).toHaveURL(/\/login/);
    });

    test('should clear storage on logout', async ({ page }) => {
      // Login
      await page.getByLabel(/email/i).fill('admin@frutas.com');
      await page.getByLabel(/contraseña/i).fill('admin123');
      await page.getByRole('button', { name: /ingresar|iniciar/i }).click();
      await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });

      // Check token exists
      const token = await page.evaluate(() => localStorage.getItem('token'));
      expect(token).toBeTruthy();

      // Logout
      const userMenu = page.getByRole('button', { name: /usuario|perfil|admin/i });
      if (await userMenu.isVisible()) {
        await userMenu.click();
        await page.getByRole('menuitem', { name: /cerrar sesión|logout|salir/i }).click();
      } else {
        await page.getByRole('button', { name: /cerrar sesión|logout|salir/i }).click();
      }

      // Check token is cleared
      const tokenAfterLogout = await page.evaluate(() => localStorage.getItem('token'));
      expect(tokenAfterLogout).toBeFalsy();
    });
  });
});

test.describe('Password Reset Flow', () => {
  test('should navigate to password reset page', async ({ page }) => {
    await page.goto('/login');

    // Click on forgot password link
    const forgotPasswordLink = page.getByRole('link', { name: /olvidé.*contraseña|recuperar|forgot/i });
    if (await forgotPasswordLink.isVisible()) {
      await forgotPasswordLink.click();
      await expect(page).toHaveURL(/\/password-reset|\/forgot-password/);
    }
  });

  test('should show validation error for invalid email in reset form', async ({ page }) => {
    await page.goto('/password-reset');

    // Fill invalid email
    const emailInput = page.getByLabel(/email/i);
    if (await emailInput.isVisible()) {
      await emailInput.fill('invalid-email');
      await page.getByRole('button', { name: /enviar|recuperar|submit/i }).click();
      await expect(page.getByText(/email.*válido|formato/i)).toBeVisible();
    }
  });
});
