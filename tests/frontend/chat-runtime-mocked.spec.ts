import { test, expect } from '@playwright/test';

test('chat starts session and sends answer with mocked runtime API', async ({ page }) => {
  await page.route('**/api/runtime/scenarios/**/branch-selection-config', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ mode: 'BEFORE', branches: ['north', 'south'] })
    });
  });

  await page.route('**/api/runtime/scenarios/**/sessions', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        sessionId: 'sess-123',
        questionText: 'Choose service',
        outputMessages: []
      })
    });
  });

  await page.route('**/api/runtime/sessions/**/answers', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        questionText: 'Done',
        outputMessages: [{ text: 'Visit created' }]
      })
    });
  });

  await page.goto('/chat/');
  await page.locator('#branchId').fill('north');
  await page.locator('#startBtn').click();

  await expect(page.locator('#messages')).toContainText('north');
  await expect(page.locator('#messages')).toContainText('Session started: sess-123');
  await expect(page.locator('#messages')).toContainText('Choose service');

  await page.locator('#answerCode').fill('doctor');
  await page.locator('#sendBtn').click();

  await expect(page.locator('#messages')).toContainText('doctor');
  await expect(page.locator('#messages')).toContainText('Visit created');
  await expect(page.locator('#messages')).toContainText('Done');
});
