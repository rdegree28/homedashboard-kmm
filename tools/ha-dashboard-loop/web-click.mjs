import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';

const token = readFileSync('./.ha_token', 'utf8').trim();
const url = process.argv[2];
const x = Number(process.argv[3]);
const y = Number(process.argv[4]);

const b = await chromium.launch();
const ctx = await b.newContext({ viewport: { width: 500, height: 1000 }, deviceScaleFactor: 2 });
const page = await ctx.newPage();
await page.addInitScript((t) => {
  localStorage.setItem('ha_url', 'http://10.2.3.3:8123');
  localStorage.setItem('ha_token', t);
}, token);
await page.goto(url, { waitUntil: 'load' });
await page.waitForTimeout(9000); // connect + render
await page.mouse.click(x, y);
await page.waitForTimeout(3000);
await page.screenshot({ path: 'web_click.png' });
await b.close();
console.log(`clicked CSS (${x}, ${y})`);
