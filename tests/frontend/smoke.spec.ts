import { test, expect } from '@playwright/test';

test('страница админки отображает редактор, переключает локаль и редактирует граф', async ({ page }) => {
  await page.goto('/admin/');
  await expect(page.locator('#title')).toBeVisible();
  await expect(page.locator('#addNodeBtn')).toBeVisible();
  await expect(page.locator('#graph')).toBeVisible();

  await page.locator('#locale').selectOption('ru');
  await expect(page.locator('#title')).toContainText('Админка');

  await page.locator('#nodeId').fill('q-followup');
  await page.locator('#nodeText').fill('Need follow-up?');
  await page.locator('#addNodeBtn').click();
  await expect(page.locator('#edgeFrom')).toContainText('q-followup');

  await page.screenshot({ path: 'test-results/admin-frontend.png', fullPage: true });
});

test('страница чата отображает runtime-контролы и переключение локали', async ({ page }) => {
  await page.goto('/chat/');
  await expect(page.locator('#welcome')).toBeVisible();
  await expect(page.locator('#startBtn')).toBeVisible();
  await expect(page.locator('#sendBtn')).toBeVisible();

  await page.locator('#locale').selectOption('ru');
  await expect(page.locator('#welcome')).toContainText('Добро пожаловать');

  await page.screenshot({ path: 'test-results/chat-frontend.png', fullPage: true });
});
