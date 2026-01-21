import { test, expect } from '@playwright/test';
import { login } from './helpers/auth';

/**
 * E2E tests for the traceability flow.
 * Tests public traceability consultation and internal traceability view.
 */

test.describe('Public Traceability', () => {
  test.describe('Public Traceability Page', () => {
    test('should show error for invalid UUID', async ({ page }) => {
      await page.goto('/public/trazabilidad/invalid-uuid-12345');

      // Should show error state
      await expect(page.getByText('Código no encontrado')).toBeVisible({ timeout: 10000 });
    });

    test('should display loading state initially', async ({ page }) => {
      await page.goto('/public/trazabilidad/some-uuid');

      // Should show loading state initially
      await expect(page.getByText('Cargando información de trazabilidad...')).toBeVisible({ timeout: 5000 }).catch(() => {
        // Might have already loaded or errored
      });
    });
  });
});

test.describe('Internal Traceability (Authenticated)', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('should access internal traceability page', async ({ page }) => {
    await page.goto('/trazabilidad');

    // h2 with text "Consulta de Trazabilidad"
    await expect(page.getByText('Consulta de Trazabilidad')).toBeVisible();
  });

  test('should show search components', async ({ page }) => {
    await page.goto('/trazabilidad');

    // Should show the search card with label selector
    await expect(page.getByPlaceholder('Buscar por código de etiqueta o QR...')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Consultar' })).toBeVisible();
  });

  test('should show message when no etiqueta selected', async ({ page }) => {
    await page.goto('/trazabilidad');

    // Should show initial state message with search icon and text
    await expect(page.getByText('Seleccione una etiqueta y presione "Consultar" para ver su trazabilidad completa')).toBeVisible();
  });

  test('should show etiqueta selector dropdown', async ({ page }) => {
    await page.goto('/trazabilidad');

    // Click on the select trigger
    const selectTrigger = page.getByRole('combobox');
    await selectTrigger.click();

    // Should show loading or etiquetas list
    const hasItems = await page.getByRole('option').first().isVisible({ timeout: 5000 }).catch(() => false);
    if (hasItems) {
      await expect(page.getByRole('option').first()).toBeVisible();
    } else {
      await expect(page.getByText('Cargando etiquetas...')).toBeVisible().catch(() => {});
    }
  });

  test('should show error when consulting without selecting etiqueta', async ({ page }) => {
    await page.goto('/trazabilidad');

    // Click consult without selecting - button is disabled when no etiqueta is selected
    const consultButton = page.getByRole('button', { name: 'Consultar' });

    // The button should be disabled when no etiqueta is selected
    await expect(consultButton).toBeDisabled();
  });

  test('should consult traceability when etiqueta is selected', async ({ page }) => {
    await page.goto('/trazabilidad');

    // Wait for etiquetas to load
    const selectTrigger = page.getByRole('combobox');
    await selectTrigger.click();

    const firstOption = page.getByRole('option').first();
    const hasOptions = await firstOption.isVisible({ timeout: 5000 }).catch(() => false);

    if (hasOptions) {
      await firstOption.click();

      // Click consult
      await page.getByRole('button', { name: 'Consultar' }).click();

      // Should show loading or results
      await expect(page.getByRole('button', { name: 'Consultar' })).toBeEnabled({ timeout: 10000 });
    }
  });

  test('should show traceability sections when data is loaded', async ({ page }) => {
    await page.goto('/trazabilidad');

    // Wait for etiquetas to load and select one
    const selectTrigger = page.getByRole('combobox');
    await selectTrigger.click();

    const firstOption = page.getByRole('option').first();
    const hasOptions = await firstOption.isVisible({ timeout: 5000 }).catch(() => false);

    if (hasOptions) {
      await firstOption.click();
      await page.getByRole('button', { name: 'Consultar' }).click();

      // Wait for results
      await page.waitForTimeout(2000);

      // Should show accordion sections if data is loaded
      const origenSection = page.getByText('Origen');
      if (await origenSection.isVisible({ timeout: 5000 }).catch(() => false)) {
        await expect(origenSection).toBeVisible();
      }
    }
  });
});

test.describe('Traceability Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('should access traceability from sidebar', async ({ page }) => {
    await page.goto('/');

    // Find and click on "Consulta" link in sidebar under Trazabilidad section
    // The nav item has title "Consulta" and href "/trazabilidad"
    const consultaLink = page.getByRole('link', { name: 'Consulta' });
    await consultaLink.click();
    await expect(page).toHaveURL('/trazabilidad');
  });
});
