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
  await page.getByLabel('Correo electrónico').fill(credentials.email);
  await page.getByLabel('Contraseña').fill(credentials.password);
  await page.getByRole('button', { name: 'Iniciar Sesión' }).click();
  await page.waitForURL('/', { timeout: 10000 });
}

/**
 * Logout helper function.
 * @param page - Playwright page object
 */
export async function logout(page: Page): Promise<void> {
  const logoutButton = page.getByRole('button', { name: /cerrar sesión|logout|salir/i });
  const userMenuButton = page.getByRole('button', { name: /usuario|perfil|admin/i });

  if (await logoutButton.isVisible({ timeout: 2000 }).catch(() => false)) {
    await logoutButton.click();
  } else if (await userMenuButton.isVisible({ timeout: 2000 }).catch(() => false)) {
    await userMenuButton.click();
    await page.getByRole('menuitem', { name: /cerrar sesión|logout|salir/i }).click();
  } else {
    await page.locator('[data-testid="logout"], [aria-label*="logout"], [aria-label*="salir"]').first().click();
  }
  await page.waitForURL(/\/login/);
}

/**
 * Check if user is logged in.
 * @param page - Playwright page object
 */
export async function isLoggedIn(page: Page): Promise<boolean> {
  const storage = await page.evaluate(() => localStorage.getItem('auth-storage'));
  if (!storage) return false;
  try {
    const parsed = JSON.parse(storage);
    return !!(parsed.state?.token || parsed.state?.user);
  } catch {
    return false;
  }
}

/**
 * Get current user info from localStorage.
 * @param page - Playwright page object
 */
export async function getCurrentUser(page: Page): Promise<object | null> {
  const storage = await page.evaluate(() => localStorage.getItem('auth-storage'));
  if (!storage) return null;
  try {
    const parsed = JSON.parse(storage);
    return parsed.state?.user || null;
  } catch {
    return null;
  }
}
