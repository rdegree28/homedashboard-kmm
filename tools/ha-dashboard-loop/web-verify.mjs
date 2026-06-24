import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';

const token = readFileSync('./.ha_token', 'utf8').trim();
const url = process.argv[2] || 'http://localhost:8090';

const b = await chromium.launch();
const ctx = await b.newContext({ viewport: { width: 500, height: 1000 }, deviceScaleFactor: 2 });
const page = await ctx.newPage();

const logs = [];
page.on('console', (m) => logs.push(`[${m.type()}] ${m.text()}`));
page.on('pageerror', (e) => logs.push(`[pageerror] ${e.message}`));

// Pre-seed localStorage (same keys StorageSettings/ConfigStore use) so the app
// connects directly without typing into the Compose canvas. Pass "noseed" to skip
// (to test the build's baked-in WebDefaults default config instead).
const mode = process.argv[3];
if (mode === 'badseed') {
  // Simulate a stale/broken config saved from an earlier Settings attempt.
  await page.addInitScript(() => {
    localStorage.setItem('ha_url', 'http://10.99.99.99:8123'); // unreachable
    localStorage.setItem('ha_token', 'broken-token');
  });
} else if (mode !== 'noseed') {
  await page.addInitScript((t) => {
    localStorage.setItem('ha_url', 'http://10.2.3.3:8123');
    localStorage.setItem('ha_token', t);
  }, token);
}

await page.goto(url, { waitUntil: 'load' });
await page.waitForTimeout(11000); // wasm init + WS connect + state seed + history fetch
await page.screenshot({ path: 'web_office.png' });
// scroll down (Compose canvas handles wheel) to reveal the power graph
await page.mouse.move(250, 500);
await page.mouse.wheel(0, 1400);
await page.waitForTimeout(1500);
await page.screenshot({ path: 'web_graph.png' });

console.log('=== browser console (last 40) ===');
console.log(logs.slice(-40).join('\n') || '(no console output)');
await b.close();
