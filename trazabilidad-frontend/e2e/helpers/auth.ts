import { Page } from '@playwright/test';

/**
 * Helper functions for authentication in E2E tests.
 */

export interface LoginCredentials {
  email: string;
  password: string;
}

export const DEFAULT_ADMIN_CREDENTIALS: LoginCredentials = {
  email: 'admin@frutas.com',
  password: 'admin123',
};

export const DEFAULT_OPERATOR_CREDENTIALS: LoginCredentials = {
  email: 'operador@frutas.com',
  password: 'operador123',
};

/**
 * Login helper function.
 * @param page - Playwright page object
 * @param credentials - Login credentials (defaults to admin)
 */
export async function login(page: Page, credentials: LoginCredentials = DEFAULT_ADMIN_CREDENTIALS): Promise<void> {
  await page.goto('/login');
  await page.getByLabel(/email/i).fill(credentials.email);
  await page.getByLabel(/contraseña/i).fill(credentials.password);
  await page.getByRole('button', { name: /ingresar|iniciar/i }).click();
  await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });
}

/**
 * Logout helper function.
 * @param page - Playwright page object
 */
export async function logout(page: Page): Promise<void> {
  const userMenu = page.getByRole('button', { name: /usuario|perfil|admin/i });
  if (await userMenu.isVisible()) {
    await userMenu.click();
    await page.getByRole('menuitem', { name: /cerrar sesión|logout|salir/i }).click();
  } else {
    await page.getByRole('button', { name: /cerrar sesión|logout|salir/i }).click();
  }
  await page.waitForURL(/\/login/);
}

/**
 * Check if user is logged in.
 * @param page - Playwright page object
 */
export async function isLoggedIn(page: Page): Promise<boolean> {
  const token = await page.evaluate(() => localStorage.getItem('token'));
  return !!token;
}

/**
 * Get current user info from localStorage.
 * @param page - Playwright page object
 */
export async function getCurrentUser(page: Page): Promise<object | null> {
  const userJson = await page.evaluate(() => localStorage.getItem('user'));
  return userJson ? JSON.parse(userJson) : null;
}
