import { test, expect } from '@playwright/test';

test('страница админки отображает Phase 0 React Flow controls и позволяет собрать ветку', async ({ page }) => {
  await page.goto('/admin/');
  await expect(page.getByText('Client Journey Admin')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Add QUESTION' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Add RESULT' })).toBeVisible();

  await page.getByRole('button', { name: 'Add QUESTION' }).click();
  await page.getByRole('button', { name: 'Add RESULT' }).click();

  await page.getByRole('button', { name: 'Quick: add next QUESTION' }).click();
  await page.getByRole('button', { name: 'Quick: add final RESULT' }).click();

  const nodeSelect = page.locator('label:has-text("From node") + select');
  await expect(nodeSelect.locator('option')).toHaveCount(7); // 3 default + 4 newly created

  await page.locator('label:has-text("Answer code") + input').first().fill('phase0');
  await page.locator('label:has-text("Answer label") + input').first().fill('Phase 0 route');
  await page.getByRole('button', { name: 'Add answer path' }).click();
  await expect(page.getByText(/Selected node:/)).toBeVisible();

  await page.getByRole('button', { name: 'Export + Copy JSON' }).click();
  const flowJsonTextarea = page.locator('label:has-text("Flow JSON (ScenarioGraph)") + textarea');
  await expect(flowJsonTextarea).toContainText('"nodes"');
  await expect(flowJsonTextarea).toContainText('"edges"');
  await page.getByRole('button', { name: 'Import JSON' }).click();
  await expect(page.getByText(/Imported with validation error|Flow imported successfully/)).toBeVisible();

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

test('чат в widget/white-label режиме применяет embed-конфиг', async ({ page }) => {
  await page.goto('/chat/?mode=widget&whiteLabel=true&themePreset=high-contrast&title=Corp%20Assistant&configVersion=v2026.05');
  const launcher = page.locator('#widgetLauncher');
  await expect(launcher).toBeVisible();
  await launcher.click();

  await expect(page.locator('#welcome')).toContainText('Corp Assistant');
  await expect(page.locator('#chatVersion')).toContainText('chat-ui-v1');
  await expect(page.locator('#chatVersion')).toContainText('cfg:v2026.05');
  await expect(page.locator('#locale')).toBeHidden();
  await expect(page.locator('#hint')).toBeHidden();

  await page.keyboard.press('Escape');
  await expect(page.locator('#widgetShell')).toBeHidden();
});
