import { test, expect } from '@playwright/test';

test('chat shows structured API error from start session request', async ({ page }) => {
  await page.route('**/api/runtime/scenarios/**/branch-selection-config', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ mode: 'NONE', branches: [] })
    });
  });

  await page.route('**/api/runtime/scenarios/**/sessions', async route => {
    await route.fulfill({
      status: 400,
      contentType: 'application/json',
      body: JSON.stringify({
        message: 'Scenario is disabled',
        errorCode: 'SCENARIO_DISABLED',
        details: { scenarioCode: 'medical-registration' }
      })
    });
  });

  await page.goto('/chat/');
  await page.locator('#startBtn').click();

  await expect(page.locator('#messages')).toContainText('Scenario is disabled');
});
