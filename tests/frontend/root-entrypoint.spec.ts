import { test, expect } from '@playwright/test';

test('корневая точка входа показывает навигацию в admin/chat/swagger', async ({ page }) => {
  await page.goto('/');

  await expect(page.getByRole('heading', { name: 'Client Journey Platform' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Открыть Admin UI (/admin/)' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Открыть Chat UI (/chat/)' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Открыть Swagger UI (/swagger-ui/)' })).toBeVisible();

  await page.getByRole('link', { name: 'Открыть Admin UI (/admin/)' }).click();
  await expect(page).toHaveURL(/\/admin\//);

  await page.goto('/');
  await page.getByRole('link', { name: 'Открыть Chat UI (/chat/)' }).click();
  await expect(page).toHaveURL(/\/chat\//);
});
