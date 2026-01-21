import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';

/**
 * E2E tests for pallet management.
 * Tests CRUD operations and label assignment.
 */

test.describe('Pallet Management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test.describe('Pallet List', () => {
    test('should display pallets page', async ({ page }) => {
      await page.goto('/pallets');

      await expect(page.getByRole('heading', { name: 'Pallets' })).toBeVisible();
    });

    test('should show create pallet button', async ({ page }) => {
      await page.goto('/pallets');

      await expect(page.getByRole('button', { name: 'Nuevo Pallet' })).toBeVisible();
    });

    test('should show table when pallets exist', async ({ page }) => {
      await page.goto('/pallets');

      // Wait for data to load - either table or empty state
      const table = page.locator('table');
      const emptyState = page.getByText('No hay pallets registrados');

      await expect(table.or(emptyState)).toBeVisible({ timeout: 10000 });
    });

    test('should show search input', async ({ page }) => {
      await page.goto('/pallets');

      await expect(page.getByPlaceholder('Buscar por código, fruta, calidad o destino...')).toBeVisible();
    });
  });

  test.describe('Create Pallet', () => {
    test('should open create pallet form', async ({ page }) => {
      await page.goto('/pallets');
      await page.getByRole('button', { name: 'Nuevo Pallet' }).click();

      // Dialog should open with form fields
      const dialog = page.getByRole('dialog');
      await expect(dialog).toBeVisible();
      await expect(page.getByLabel('Código Pallet *')).toBeVisible();
    });

    test('should validate required fields', async ({ page }) => {
      await page.goto('/pallets');
      await page.getByRole('button', { name: 'Nuevo Pallet' }).click();

      // Clear date field that has default value
      await page.getByLabel('Fecha Paletizado *').clear();

      // Try to submit empty form
      await page.getByRole('button', { name: 'Crear' }).click();

      // Should show validation errors
      await expect(page.getByText('El código es requerido')).toBeVisible({ timeout: 5000 });
    });

    test('should create pallet with valid data', async ({ page }) => {
      await page.goto('/pallets');
      await page.getByRole('button', { name: 'Nuevo Pallet' }).click();

      // Fill form with unique code
      const uniqueCode = `PAL-TEST-${Date.now()}`;
      await page.getByLabel('Código Pallet *').fill(uniqueCode);
      await page.getByLabel('Número de Cajas *').fill('80');

      // Submit
      await page.getByRole('button', { name: 'Crear' }).click();

      // Should show success message
      await expect(page.getByText('Pallet creado correctamente')).toBeVisible({ timeout: 5000 });
    });
  });

  test.describe('Edit Pallet', () => {
    test('should show edit option in actions menu', async ({ page }) => {
      await page.goto('/pallets');

      // Wait for data
      const table = page.locator('table');
      const hasTable = await table.isVisible({ timeout: 5000 }).catch(() => false);

      if (hasTable) {
        // Click on first row's actions button
        const actionsButton = page.locator('table tbody tr').first().getByRole('button');
        await actionsButton.click();

        // Editar option should be visible
        await expect(page.getByRole('menuitem', { name: 'Editar' })).toBeVisible();
      }
    });

    test('should open edit form with existing data', async ({ page }) => {
      await page.goto('/pallets');

      const table = page.locator('table');
      const hasTable = await table.isVisible({ timeout: 5000 }).catch(() => false);

      if (hasTable) {
        const actionsButton = page.locator('table tbody tr').first().getByRole('button');
        await actionsButton.click();
        await page.getByRole('menuitem', { name: 'Editar' }).click();

        // Dialog should open with "Editar" title
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.getByText('Editar Pallet', { exact: true })).toBeVisible();
      }
    });
  });

  test.describe('State Changes', () => {
    test('should show state change options in menu', async ({ page }) => {
      await page.goto('/pallets');

      const table = page.locator('table');
      const hasTable = await table.isVisible({ timeout: 5000 }).catch(() => false);

      if (hasTable) {
        const actionsButton = page.locator('table tbody tr').first().getByRole('button');
        await actionsButton.click();

        // State change options may be visible depending on current state
        const menu = page.getByRole('menu');
        await expect(menu).toBeVisible();
      }
    });
  });

  test.describe('Delete Pallet', () => {
    test('should show delete option in actions menu', async ({ page }) => {
      await page.goto('/pallets');

      const table = page.locator('table');
      const hasTable = await table.isVisible({ timeout: 5000 }).catch(() => false);

      if (hasTable) {
        const actionsButton = page.locator('table tbody tr').first().getByRole('button');
        await actionsButton.click();

        await expect(page.getByRole('menuitem', { name: 'Eliminar' })).toBeVisible();
      }
    });

    test('should show confirmation dialog when deleting', async ({ page }) => {
      await page.goto('/pallets');

      const table = page.locator('table');
      const hasTable = await table.isVisible({ timeout: 5000 }).catch(() => false);

      if (hasTable) {
        const actionsButton = page.locator('table tbody tr').first().getByRole('button');
        await actionsButton.click();
        await page.getByRole('menuitem', { name: 'Eliminar' }).click();

        // Confirmation dialog should appear
        await expect(page.getByRole('alertdialog')).toBeVisible();
        await expect(page.getByText('¿Eliminar pallet?')).toBeVisible();
      }
    });

    test('should cancel deletion when clicking cancel', async ({ page }) => {
      await page.goto('/pallets');

      const table = page.locator('table');
      const hasTable = await table.isVisible({ timeout: 5000 }).catch(() => false);

      if (hasTable) {
        const actionsButton = page.locator('table tbody tr').first().getByRole('button');
        await actionsButton.click();
        await page.getByRole('menuitem', { name: 'Eliminar' }).click();

        // Cancel
        await page.getByRole('button', { name: 'Cancelar' }).click();

        // Dialog should close
        await expect(page.getByRole('alertdialog')).not.toBeVisible();
      }
    });
  });
});

test.describe('Etiquetas Management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('should display etiquetas page', async ({ page }) => {
    await page.goto('/etiquetas');

    await expect(page.getByRole('heading', { name: 'Etiquetas' })).toBeVisible();
  });

  test('should show create etiqueta button', async ({ page }) => {
    await page.goto('/etiquetas');

    await expect(page.getByRole('button', { name: /nueva.*etiqueta/i })).toBeVisible();
  });
});
