import { test, expect } from '@playwright/test';

test('панель ops в админке загружает мок-данные dashboard', async ({ page }) => {
  await page.route('**/api/system/dashboard', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        health: 'UP',
        readiness: 'READY',
        liveness: 'ALIVE',
        metrics: { 'requests.metrics': 7 }
      })
    });
  });

  await page.goto('/admin/');
  await page.locator('#loadOpsBtn').click();

  await expect(page.locator('#opsOutput')).toContainText('"health": "UP"');
  await expect(page.locator('#opsOutput')).toContainText('"requests.metrics": 7');
});


test('админка определяет baseUrl отделения через мок API', async ({ page }) => {
  await page.route('**/api/admin/scenarios/**/visit-creation-settings/resolve**', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ branchId: 'north', baseUrl: 'http://north-vm:8080' })
    });
  });

  await page.goto('/admin/');
  await page.locator('#resolveScenarioId').fill('11111111-1111-1111-1111-111111111111');
  await page.locator('#resolveBranchId').fill('north');
  await page.locator('#resolveBaseUrlBtn').click();

  await expect(page.locator('#resolveOutput')).toContainText('http://north-vm:8080');
});

test('админка запускает dry-run проверку настроек визита через мок API', async ({ page }) => {
  await page.route('**/api/admin/scenarios/**/visit-creation-settings/test', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: true, message: 'dry-run success' })
    });
  });

  await page.goto('/admin/');
  await page.locator('#resolveScenarioId').fill('11111111-1111-1111-1111-111111111111');
  await page.locator('#visitSettingsTestBtn').click();

  await expect(page.locator('#visitSettingsTestOutput')).toContainText('"ok": true');
  await expect(page.locator('#visitSettingsTestOutput')).toContainText('dry-run success');
});


test('админка проходит сценарий branch-selection + dry-run через мок API', async ({ page }) => {
  await page.route('**/api/admin/scenarios/**/visit-creation-settings/resolve**', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ branchId: 'north', baseUrl: 'http://north-vm:8080' })
    });
  });

  await page.route('**/api/admin/scenarios/**/visit-creation-settings/test', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: true, message: 'dry-run success for north branch' })
    });
  });

  await page.goto('/admin/');
  await page.locator('#resolveScenarioId').fill('11111111-1111-1111-1111-111111111111');
  await page.locator('#resolveBranchId').fill('north');

  await page.locator('#resolveBaseUrlBtn').click();
  await expect(page.locator('#resolveOutput')).toContainText('http://north-vm:8080');

  await page.locator('#visitSettingsTestBtn').click();
  await expect(page.locator('#visitSettingsTestOutput')).toContainText('dry-run success for north branch');
});
