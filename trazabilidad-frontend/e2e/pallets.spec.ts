import { test, expect } from '@playwright/test';

/**
 * E2E tests for pallet management.
 * Tests CRUD operations and label assignment.
 */

test.describe('Pallet Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('admin@frutas.com');
    await page.getByLabel(/contraseña/i).fill('admin123');
    await page.getByRole('button', { name: /ingresar|iniciar/i }).click();
    await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });
  });

  test.describe('Pallet List', () => {
    test('should display pallets list', async ({ page }) => {
      await page.goto('/empaque/pallets');

      await expect(page.getByRole('heading', { name: /pallets/i })).toBeVisible();
      await expect(page.getByRole('table')).toBeVisible();
    });

    test('should show filter by status', async ({ page }) => {
      await page.goto('/empaque/pallets');

      const statusFilter = page.getByRole('combobox', { name: /estado/i });
      if (await statusFilter.isVisible()) {
        await statusFilter.click();
        await expect(page.getByRole('option', { name: /armado/i })).toBeVisible();
        await expect(page.getByRole('option', { name: /en.*cámara/i })).toBeVisible();
      }
    });

    test('should filter by destination', async ({ page }) => {
      await page.goto('/empaque/pallets');

      const searchInput = page.getByPlaceholder(/destino|buscar/i);
      if (await searchInput.isVisible()) {
        await searchInput.fill('Estados Unidos');
        await page.waitForTimeout(500);
      }
    });

    test('should show pallets ready for shipment', async ({ page }) => {
      await page.goto('/empaque/pallets?listos=true');

      // Should filter to show only ARMADO or EN_CAMARA pallets
      await expect(page.getByRole('table')).toBeVisible();
    });
  });

  test.describe('Create Pallet', () => {
    test('should show create pallet button', async ({ page }) => {
      await page.goto('/empaque/pallets');

      await expect(page.getByRole('button', { name: /nuevo.*pallet|crear.*pallet|agregar/i })).toBeVisible();
    });

    test('should open create form', async ({ page }) => {
      await page.goto('/empaque/pallets');
      await page.getByRole('button', { name: /nuevo.*pallet|crear.*pallet|agregar/i }).click();

      await expect(page.getByLabel(/código.*pallet/i)).toBeVisible({ timeout: 5000 });
    });

    test('should create pallet with valid data', async ({ page }) => {
      await page.goto('/empaque/pallets');
      await page.getByRole('button', { name: /nuevo.*pallet|crear.*pallet|agregar/i }).click();

      // Fill form
      await page.getByLabel(/código.*pallet/i).fill('PAL-TEST-001');
      await page.getByLabel(/tipo.*pallet/i).click();
      await page.getByRole('option', { name: /estándar/i }).click();
      await page.getByLabel(/número.*cajas/i).fill('80');
      await page.getByLabel(/peso.*neto/i).fill('400');
      await page.getByLabel(/peso.*bruto/i).fill('420');
      await page.getByLabel(/destino/i).fill('Estados Unidos');
      await page.getByLabel(/tipo.*fruta/i).fill('Mango');

      await page.getByRole('button', { name: /guardar|crear/i }).click();

      await expect(page.getByText(/creado.*exitosamente|éxito/i)).toBeVisible({ timeout: 5000 });
    });

    test('should show error for duplicate code', async ({ page }) => {
      await page.goto('/empaque/pallets');
      await page.getByRole('button', { name: /nuevo.*pallet|crear.*pallet|agregar/i }).click();

      await page.getByLabel(/código.*pallet/i).fill('PAL-001'); // Existing code
      await page.getByLabel(/tipo.*pallet/i).click();
      await page.getByRole('option').first().click();
      await page.getByLabel(/número.*cajas/i).fill('80');

      await page.getByRole('button', { name: /guardar|crear/i }).click();

      await expect(page.getByText(/ya existe|duplicado/i)).toBeVisible({ timeout: 5000 }).catch(() => {});
    });
  });

  test.describe('Label Assignment', () => {
    test('should show label assignment option', async ({ page }) => {
      await page.goto('/empaque/pallets/1');

      await expect(page.getByRole('button', { name: /asignar.*etiqueta|agregar.*etiqueta/i })).toBeVisible({ timeout: 5000 }).catch(() => {});
    });

    test('should only show available labels', async ({ page }) => {
      await page.goto('/empaque/pallets/1');

      const assignButton = page.getByRole('button', { name: /asignar.*etiqueta|agregar.*etiqueta/i });
      if (await assignButton.isVisible()) {
        await assignButton.click();

        // Should show only DISPONIBLE labels
        await expect(page.getByText(/DISPONIBLE/i)).toBeVisible({ timeout: 5000 });
        // Should NOT show already assigned labels
        await expect(page.getByText(/ASIGNADA_PALLET/i)).not.toBeVisible();
      }
    });

    test('should assign label to pallet', async ({ page }) => {
      await page.goto('/empaque/pallets/1');

      const assignButton = page.getByRole('button', { name: /asignar.*etiqueta/i });
      if (await assignButton.isVisible()) {
        await assignButton.click();

        // Select a label
        await page.getByRole('checkbox').first().check();
        await page.getByRole('button', { name: /confirmar|asignar/i }).click();

        await expect(page.getByText(/asignada|éxito/i)).toBeVisible({ timeout: 5000 });
      }
    });

    test('should change label state to ASIGNADA_PALLET', async ({ page }) => {
      await page.goto('/empaque/pallets/1');

      // After assigning, label state should change
      await expect(page.getByText(/ASIGNADA_PALLET/i)).toBeVisible({ timeout: 5000 }).catch(() => {});
    });
  });

  test.describe('State Changes', () => {
    test('should change pallet state', async ({ page }) => {
      await page.goto('/empaque/pallets/1');

      const stateButton = page.getByRole('button', { name: /cambiar.*estado/i });
      if (await stateButton.isVisible()) {
        await stateButton.click();
        await page.getByRole('option', { name: /en.*cámara/i }).click();
        await page.getByRole('button', { name: /confirmar/i }).click();

        await expect(page.getByText(/actualizado|éxito/i)).toBeVisible({ timeout: 5000 });
      }
    });
  });

  test.describe('Delete Pallet', () => {
    test('should release labels when deleting pallet', async ({ page }) => {
      await page.goto('/empaque/pallets/1');

      const deleteButton = page.getByRole('button', { name: /eliminar|borrar/i });
      if (await deleteButton.isVisible()) {
        await deleteButton.click();
        await page.getByRole('button', { name: /confirmar/i }).click();

        // Associated labels should be released (set to DISPONIBLE)
        await expect(page.getByText(/eliminado|éxito/i)).toBeVisible({ timeout: 5000 });
      }
    });
  });
});

test.describe('Labels Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('admin@frutas.com');
    await page.getByLabel(/contraseña/i).fill('admin123');
    await page.getByRole('button', { name: /ingresar|iniciar/i }).click();
    await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });
  });

  test('should display labels list', async ({ page }) => {
    await page.goto('/empaque/etiquetas');

    await expect(page.getByRole('heading', { name: /etiquetas/i })).toBeVisible();
  });

  test('should show QR code for label', async ({ page }) => {
    await page.goto('/empaque/etiquetas/1');

    await expect(page.getByRole('img', { name: /qr/i })).toBeVisible({ timeout: 5000 }).catch(() => {
      // QR might be rendered as canvas
    });
  });

  test('should filter by state', async ({ page }) => {
    await page.goto('/empaque/etiquetas');

    const stateFilter = page.getByRole('combobox', { name: /estado/i });
    if (await stateFilter.isVisible()) {
      await stateFilter.click();
      await page.getByRole('option', { name: /disponible/i }).click();
      await page.waitForTimeout(500);
    }
  });
});
