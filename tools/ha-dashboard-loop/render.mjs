#!/usr/bin/env node
// Render an authenticated HA dashboard path to a screenshot.
// Usage: node render.mjs <dashboard-path> <out.png> [viewportWidth]
import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';

const token = readFileSync(new URL('./.ha_token', import.meta.url), 'utf8').trim();
const env = readFileSync(new URL('./.ha_env', import.meta.url), 'utf8');
const base = env.match(/HA_URL=(.*)/)[1].trim();

const path = process.argv[2] || '/lovelace-office/office';
const out = process.argv[3] || 'shot.png';
const width = parseInt(process.argv[4] || '500', 10);

const hassTokens = {
  access_token: token,
  token_type: 'Bearer',
  expires_in: 1800,
  hassUrl: base,
  clientId: base + '/',
  expires: Date.now() + 10 * 365 * 24 * 3600 * 1000,
  refresh_token: 'llat-no-refresh',
};

const browser = await chromium.launch();
const ctx = await browser.newContext({ viewport: { width, height: 1000 }, deviceScaleFactor: 2 });
const page = await ctx.newPage();
await page.addInitScript((tok) => {
  window.localStorage.setItem('hassTokens', JSON.stringify(tok));
  window.localStorage.setItem('selectedTheme', JSON.stringify({ dark: true }));
}, hassTokens);

await page.goto(base + path, { waitUntil: 'load' });
await page.waitForTimeout(3500); // let custom cards + templates render
await page.screenshot({ path: out, fullPage: true });
console.log('final url:', page.url());
console.log('title  :', await page.title());
await browser.close();
