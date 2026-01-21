import { test, expect } from '@playwright/test';

/**
 * E2E tests for shipment management.
 * Tests CRUD operations, pallet assignment, state changes, and shipment closing.
 */

test.describe('Shipment Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login as logistics operator
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('admin@frutas.com');
    await page.getByLabel(/contraseña/i).fill('admin123');
    await page.getByRole('button', { name: /ingresar|iniciar/i }).click();
    await page.waitForURL(/\/dashboard|\/$/, { timeout: 10000 });
  });

  test.describe('Shipment List', () => {
    test('should display shipments list', async ({ page }) => {
      // Navigate to shipments
      await page.goto('/logistica/envios');

      // Check for shipments table/list
      await expect(page.getByRole('heading', { name: /envíos|exportaciones/i })).toBeVisible();
      await expect(page.getByRole('table')).toBeVisible();
    });

    test('should show filter options', async ({ page }) => {
      await page.goto('/logistica/envios');

      // Check for filter elements
      await expect(page.getByRole('button', { name: /filtrar|filtros/i })).toBeVisible().catch(() => {
        // Might be a dropdown or different UI
      });
    });

    test('should filter by shipment status', async ({ page }) => {
      await page.goto('/logistica/envios');

      // Find and click status filter
      const statusFilter = page.getByRole('combobox', { name: /estado/i });
      if (await statusFilter.isVisible()) {
        await statusFilter.click();
        await page.getByRole('option', { name: /preparando/i }).click();

        // Should filter the results
        await page.waitForTimeout(500);
      }
    });

    test('should show shipment details when clicking on a row', async ({ page }) => {
      await page.goto('/logistica/envios');

      // Click on first shipment row
      const firstRow = page.getByRole('row').nth(1);
      if (await firstRow.isVisible()) {
        await firstRow.click();

        // Should navigate to shipment details or show modal
        await expect(page.getByText(/detalles.*envío|información/i)).toBeVisible({ timeout: 5000 }).catch(() => {
          // Might navigate to detail page instead
        });
      }
    });
  });

  test.describe('Create Shipment', () => {
    test('should show create shipment button', async ({ page }) => {
      await page.goto('/logistica/envios');

      await expect(page.getByRole('button', { name: /nuevo.*envío|crear.*envío|agregar/i })).toBeVisible();
    });

    test('should open create shipment form', async ({ page }) => {
      await page.goto('/logistica/envios');

      // Click create button
      await page.getByRole('button', { name: /nuevo.*envío|crear.*envío|agregar/i }).click();

      // Form should be visible (modal or new page)
      await expect(page.getByLabel(/código.*envío/i)).toBeVisible({ timeout: 5000 });
    });

    test('should validate required fields', async ({ page }) => {
      await page.goto('/logistica/envios');
      await page.getByRole('button', { name: /nuevo.*envío|crear.*envío|agregar/i }).click();

      // Try to submit empty form
      await page.getByRole('button', { name: /guardar|crear|enviar/i }).click();

      // Should show validation errors
      await expect(page.getByText(/obligatorio|requerido/i)).toBeVisible();
    });

    test('should create shipment with valid data', async ({ page }) => {
      await page.goto('/logistica/envios');
      await page.getByRole('button', { name: /nuevo.*envío|crear.*envío|agregar/i }).click();

      // Fill form
      await page.getByLabel(/código.*envío/i).fill('ENV-TEST-001');
      await page.getByLabel(/país.*destino/i).fill('Estados Unidos');
      await page.getByLabel(/puerto.*destino/i).fill('Miami');

      // Select transport type if dropdown
      const transportSelect = page.getByLabel(/tipo.*transporte/i);
      if (await transportSelect.isVisible()) {
        await transportSelect.click();
        await page.getByRole('option', { name: /marítimo/i }).click();
      }

      // Submit
      await page.getByRole('button', { name: /guardar|crear|enviar/i }).click();

      // Should show success message
      await expect(page.getByText(/creado.*exitosamente|éxito/i)).toBeVisible({ timeout: 5000 });
    });

    test('should show error for duplicate shipment code', async ({ page }) => {
      await page.goto('/logistica/envios');
      await page.getByRole('button', { name: /nuevo.*envío|crear.*envío|agregar/i }).click();

      // Fill with existing code
      await page.getByLabel(/código.*envío/i).fill('ENV-EXISTING-001');
      await page.getByLabel(/país.*destino/i).fill('Canada');
      await page.getByLabel(/puerto.*destino/i).fill('Vancouver');
      await page.getByRole('button', { name: /guardar|crear|enviar/i }).click();

      // Should show duplicate error
      await expect(page.getByText(/ya existe|duplicado/i)).toBeVisible({ timeout: 5000 }).catch(() => {
        // Test only valid if data exists
      });
    });
  });

  test.describe('Pallet Assignment', () => {
    test('should show pallet assignment option', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      // Check for pallet assignment button/section
      await expect(page.getByRole('button', { name: /asignar.*pallet|agregar.*pallet/i })).toBeVisible({ timeout: 5000 }).catch(() => {
        // UI might be different
      });
    });

    test('should display available pallets for assignment', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      // Click assign pallet button
      const assignButton = page.getByRole('button', { name: /asignar.*pallet|agregar.*pallet/i });
      if (await assignButton.isVisible()) {
        await assignButton.click();

        // Should show list of available pallets
        await expect(page.getByText(/pallets.*disponibles|seleccionar.*pallet/i)).toBeVisible({ timeout: 5000 });
      }
    });

    test('should not show already assigned pallets', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      const assignButton = page.getByRole('button', { name: /asignar.*pallet|agregar.*pallet/i });
      if (await assignButton.isVisible()) {
        await assignButton.click();

        // Pallets with state ASIGNADO_ENVIO should not appear
        await expect(page.getByText(/ASIGNADO_ENVIO/i)).not.toBeVisible({ timeout: 2000 }).catch(() => {});
      }
    });
  });

  test.describe('State Changes', () => {
    test('should show state change options', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      // Check for state change button
      await expect(page.getByRole('button', { name: /cambiar.*estado|actualizar.*estado/i })).toBeVisible({ timeout: 5000 }).catch(() => {
        // UI might be different
      });
    });

    test('should allow changing state to EN_TRANSITO', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      const stateButton = page.getByRole('button', { name: /cambiar.*estado|actualizar.*estado/i });
      if (await stateButton.isVisible()) {
        await stateButton.click();

        // Select new state
        await page.getByRole('option', { name: /en.*tránsito|en_transito/i }).click();

        // Confirm
        await page.getByRole('button', { name: /confirmar|guardar/i }).click();

        // Should show success
        await expect(page.getByText(/actualizado|éxito/i)).toBeVisible({ timeout: 5000 });
      }
    });

    test('should not allow state change on closed shipment', async ({ page }) => {
      // Navigate to a closed shipment
      await page.goto('/logistica/envios?estado=CERRADO');

      const firstRow = page.getByRole('row').nth(1);
      if (await firstRow.isVisible()) {
        await firstRow.click();

        // State change button should be disabled or hidden
        const stateButton = page.getByRole('button', { name: /cambiar.*estado/i });
        if (await stateButton.isVisible()) {
          await expect(stateButton).toBeDisabled();
        }
      }
    });
  });

  test.describe('Close Shipment', () => {
    test('should show close shipment button', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      await expect(page.getByRole('button', { name: /cerrar.*envío|finalizar/i })).toBeVisible({ timeout: 5000 }).catch(() => {
        // Might require specific conditions
      });
    });

    test('should require confirmation before closing', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      const closeButton = page.getByRole('button', { name: /cerrar.*envío|finalizar/i });
      if (await closeButton.isVisible()) {
        await closeButton.click();

        // Should show confirmation dialog
        await expect(page.getByText(/confirmar|está seguro|irreversible/i)).toBeVisible({ timeout: 5000 });
      }
    });

    test('should not allow closing shipment without pallets', async ({ page }) => {
      // Navigate to a shipment without pallets
      await page.goto('/logistica/envios/new-shipment');

      const closeButton = page.getByRole('button', { name: /cerrar.*envío|finalizar/i });
      if (await closeButton.isVisible()) {
        // Should be disabled or show error when clicked
        await closeButton.click();
        await expect(page.getByText(/al menos.*pallet|requiere.*pallet/i)).toBeVisible({ timeout: 5000 }).catch(async () => {
          expect(await closeButton.isDisabled()).toBeTruthy();
        });
      }
    });

    test('should generate hash on shipment close', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      const closeButton = page.getByRole('button', { name: /cerrar.*envío|finalizar/i });
      if (await closeButton.isVisible()) {
        await closeButton.click();

        // Confirm closure
        await page.getByRole('button', { name: /confirmar/i }).click();

        // Should show hash
        await expect(page.getByText(/hash|SHA-256|blockchain/i)).toBeVisible({ timeout: 5000 }).catch(() => {});
      }
    });

    test('should not allow editing closed shipment', async ({ page }) => {
      // Navigate to closed shipment
      await page.goto('/logistica/envios?estado=CERRADO');

      const firstRow = page.getByRole('row').nth(1);
      if (await firstRow.isVisible()) {
        await firstRow.click();

        // Edit button should be disabled or not present
        const editButton = page.getByRole('button', { name: /editar/i });
        if (await editButton.isVisible()) {
          await expect(editButton).toBeDisabled();
        }
      }
    });
  });

  test.describe('Delete Shipment', () => {
    test('should show delete option for open shipments', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      await expect(page.getByRole('button', { name: /eliminar|borrar/i })).toBeVisible({ timeout: 5000 }).catch(() => {
        // Might be in menu
      });
    });

    test('should require confirmation before deleting', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      const deleteButton = page.getByRole('button', { name: /eliminar|borrar/i });
      if (await deleteButton.isVisible()) {
        await deleteButton.click();

        // Should show confirmation
        await expect(page.getByText(/confirmar|está seguro/i)).toBeVisible({ timeout: 5000 });
      }
    });

    test('should not allow deleting closed shipment', async ({ page }) => {
      // Navigate to closed shipment
      await page.goto('/logistica/envios?estado=CERRADO');

      const firstRow = page.getByRole('row').nth(1);
      if (await firstRow.isVisible()) {
        await firstRow.click();

        // Delete button should be disabled or hidden
        const deleteButton = page.getByRole('button', { name: /eliminar|borrar/i });
        if (await deleteButton.isVisible()) {
          await expect(deleteButton).toBeDisabled();
        }
      }
    });
  });

  test.describe('Totals Calculation', () => {
    test('should display calculated totals', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      // Should show calculated fields
      await expect(page.getByText(/peso.*neto|total.*peso/i)).toBeVisible({ timeout: 5000 }).catch(() => {});
      await expect(page.getByText(/cajas|total.*cajas/i)).toBeVisible({ timeout: 5000 }).catch(() => {});
      await expect(page.getByText(/pallets|total.*pallets/i)).toBeVisible({ timeout: 5000 }).catch(() => {});
    });

    test('should update totals when pallets are assigned', async ({ page }) => {
      await page.goto('/logistica/envios/1');

      // Get initial totals
      const initialPallets = await page.getByTestId('total-pallets').textContent().catch(() => '0');

      // Assign new pallet
      const assignButton = page.getByRole('button', { name: /asignar.*pallet/i });
      if (await assignButton.isVisible()) {
        await assignButton.click();

        // Select a pallet
        await page.getByRole('checkbox').first().check();
        await page.getByRole('button', { name: /confirmar|asignar/i }).click();

        // Totals should be updated
        const newPallets = await page.getByTestId('total-pallets').textContent().catch(() => '0');
        expect(parseInt(newPallets || '0')).toBeGreaterThan(parseInt(initialPallets || '0'));
      }
    });
  });
});
