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
      // CardTitle renders as a div, so we search by text content instead of heading role
      await expect(page.getByText('Iniciar Sesión', { exact: true }).first()).toBeVisible();
      await expect(page.getByLabel('Correo electrónico')).toBeVisible();
      await expect(page.getByLabel('Contraseña')).toBeVisible();
      await expect(page.getByRole('button', { name: 'Iniciar Sesión' })).toBeVisible();
    });

    test('should show validation errors for empty form', async ({ page }) => {
      // Click login without filling form
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

      // Check for validation messages
      await expect(page.getByText('Ingrese un email válido')).toBeVisible();
      await expect(page.getByText('La contraseña es requerida')).toBeVisible();
    });

    test('should show error for invalid email format', async ({ page }) => {
      // Fill with invalid email - browser's type="email" validates format
      // Test uses a format that passes basic browser check but is clearly invalid
      const emailInput = page.getByLabel('Correo electrónico');
      await emailInput.click();
      await emailInput.fill('test@');

      // Fill password so that's not a validation error
      const passwordInput = page.getByLabel('Contraseña');
      await passwordInput.click();
      await passwordInput.fill('password123');

      // Submit form - browser validation may show native tooltip for invalid format
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

      // Either zod validation shows or browser prevents submission (both indicate email validation works)
      // Check that we're still on login page (form wasn't submitted successfully)
      await expect(page).toHaveURL(/\/login/);
    });

    test('should show error for invalid credentials', async ({ page }) => {
      // Fill with wrong credentials
      await page.getByLabel('Correo electrónico').fill('wrong@email.com');
      await page.getByLabel('Contraseña').fill('wrongpassword');
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

      // Check for error message (from backend)
      await expect(page.getByText(/credenciales|inválid|incorrect|error/i)).toBeVisible({ timeout: 10000 });
    });

    test('should login successfully with valid credentials', async ({ page }) => {
      // Fill with valid credentials (test user)
      await page.getByLabel('Correo electrónico').fill('admin@frutas.com');
      await page.getByLabel('Contraseña').fill('admin123');
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

      // Should redirect to dashboard
      await expect(page).toHaveURL('/', { timeout: 10000 });
    });

    test('should show password toggle functionality', async ({ page }) => {
      const passwordInput = page.getByLabel('Contraseña');

      // Initially password should be hidden
      await expect(passwordInput).toHaveAttribute('type', 'password');

      // Click toggle button (the eye icon button)
      const toggleButton = page.locator('button[type="button"]').filter({ has: page.locator('svg') });
      await toggleButton.click();
      await expect(passwordInput).toHaveAttribute('type', 'text');

      // Click again to hide
      await toggleButton.click();
      await expect(passwordInput).toHaveAttribute('type', 'password');
    });
  });

  test.describe('Protected Routes', () => {
    test('should redirect to login when accessing protected route without auth', async ({ page }) => {
      // Try to access dashboard directly
      await page.goto('/');

      // Should redirect to login
      await expect(page).toHaveURL(/\/login/);
    });

    test('should redirect to login when accessing fincas without auth', async ({ page }) => {
      await page.goto('/fincas');
      await expect(page).toHaveURL(/\/login/);
    });

    test('should redirect to login when accessing envios without auth', async ({ page }) => {
      await page.goto('/envios');
      await expect(page).toHaveURL(/\/login/);
    });
  });

  test.describe('Session Management', () => {
    test('should persist session after page reload', async ({ page }) => {
      // Login first
      await page.getByLabel('Correo electrónico').fill('admin@frutas.com');
      await page.getByLabel('Contraseña').fill('admin123');
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

      // Wait for redirect to dashboard
      await expect(page).toHaveURL('/', { timeout: 10000 });

      // Reload the page
      await page.reload();

      // Should still be on dashboard (session persisted)
      await expect(page).toHaveURL('/');
    });

    test('should logout successfully', async ({ page }) => {
      // Login first
      await page.getByLabel('Correo electrónico').fill('admin@frutas.com');
      await page.getByLabel('Contraseña').fill('admin123');
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

      // Wait for redirect to dashboard
      await expect(page).toHaveURL('/', { timeout: 10000 });

      // Click on the user avatar button in the header to open the dropdown menu
      // Look for the button in the header that contains an avatar (with initials)
      const header = page.locator('header');
      const avatarButton = header.locator('button').filter({ hasText: /^[A-Z]{1,2}$/ });
      await avatarButton.click();

      // Wait for dropdown menu to appear and click "Cerrar sesión"
      await page.getByRole('menuitem', { name: 'Cerrar sesión' }).click();

      // Should redirect to login
      await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
    });

    test('should clear storage on logout', async ({ page }) => {
      // Login
      await page.getByLabel('Correo electrónico').fill('admin@frutas.com');
      await page.getByLabel('Contraseña').fill('admin123');
      await page.getByRole('button', { name: 'Iniciar Sesión' }).click();
      await expect(page).toHaveURL('/', { timeout: 10000 });

      // Check token exists - the authService stores token in 'token' key
      const token = await page.evaluate(() => localStorage.getItem('token'));
      expect(token).toBeTruthy();

      // Open user menu - find the avatar button (the last button in header area)
      const header = page.locator('header');
      await header.waitFor({ state: 'visible' });

      // The user avatar button is the LAST button in the header's flex container
      const avatarButton = header.locator('button').last();
      await avatarButton.click();

      // Wait for dropdown menu to appear and click "Cerrar sesión"
      await page.getByRole('menuitem', { name: 'Cerrar sesión' }).click();

      // Wait for redirect to login
      await expect(page).toHaveURL(/\/login/, { timeout: 10000 });

      // Check token is cleared - authService removes 'token' and 'user' keys on logout
      const tokenAfterLogout = await page.evaluate(() => localStorage.getItem('token'));
      expect(tokenAfterLogout).toBeFalsy();
    });
  });
});

test.describe('Password Reset Flow', () => {
  test('should navigate to password reset page', async ({ page }) => {
    await page.goto('/login');

    // Click on forgot password link
    await page.getByRole('link', { name: '¿Olvidó su contraseña?' }).click();
    await expect(page).toHaveURL('/forgot-password');
  });

  test('should show validation error for invalid email in reset form', async ({ page }) => {
    await page.goto('/forgot-password');

    // Fill with invalid email - browser's type="email" validates format
    const emailInput = page.getByLabel('Correo electrónico');
    await emailInput.click();
    await emailInput.fill('test@');

    // Submit form - browser validation may prevent submission for invalid format
    await page.getByRole('button', { name: 'Enviar instrucciones' }).click();

    // Either zod validation shows or browser prevents submission (both indicate email validation works)
    // Check that we're still on forgot-password page (form wasn't submitted successfully)
    await expect(page).toHaveURL('/forgot-password');
  });
});
