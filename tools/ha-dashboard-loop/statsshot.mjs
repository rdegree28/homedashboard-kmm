// Screenshots the running local wasm build against live HA, to eyeball a UI change without deploying.
//
// Serve the build first:
//   NODE_OPTIONS= ./gradlew :composeApp:wasmJsBrowserDistribution
//   node web-serve.mjs ../../composeApp/build/dist/wasmJs/productionExecutable 8090
//
// Then, from this directory (so node_modules resolves):
//   STEPS='[[369,228,"plants"],[250,245,"louie"],[417,214,"1y"]]' node statsshot.mjs /tmp/shots
//
// There's no DOM to query — the app is one Compose canvas — so navigation is blind mouse clicks.
// Each step is [x, y, screenshotName, scrollY?]; a non-zero scrollY wheels down after the click and
// takes a second "-scrolled" shot. Read the coordinates for the next step off the previous
// screenshot rather than assuming them; banners and alerts shift the layout.
//
// Usage: node statsshot.mjs [outDir]
import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';

const token = readFileSync(new URL('./.ha_token', import.meta.url), 'utf8').trim();
const env = readFileSync(new URL('./.ha_env', import.meta.url), 'utf8');
const haUrl = (env.match(/HA_URL=(.*)/)?.[1] || '').trim();
const out = process.argv[2] || '.';

const browser = await chromium.launch({
  args: ['--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader',
    '--disable-gpu-sandbox', '--in-process-gpu'],
});
const ctx = await browser.newContext({ viewport: { width: 500, height: 900 }, deviceScaleFactor: 1 });
await ctx.addInitScript(([url, tok]) => {
  localStorage.setItem('ha_url', url);
  localStorage.setItem('ha_token', tok);
  localStorage.setItem('auth_user', 'Rob');
}, [haUrl, token]);

const page = await ctx.newPage();
page.on('console', (m) => { if (m.type() === 'error') console.log('CONSOLE ERROR:', m.text()); });
await page.goto('http://localhost:8090/');
await page.waitForTimeout(14000);
await page.screenshot({ path: `${out}/00-home.png` });

// Blind clicks on the Compose canvas; coordinates come from the previous screenshot.
const click = async (x, y, label, wait = 3500) => {
  await page.mouse.click(x, y);
  await page.waitForTimeout(wait);
  await page.screenshot({ path: `${out}/${label}.png` });
};

for (const [x, y, label, scrollY = 0] of JSON.parse(process.env.STEPS || '[]')) {
  await click(x, y, label, scrollY ? 1500 : 3500);
  if (scrollY) {
    await page.mouse.move(250, 500);
    await page.mouse.wheel(0, scrollY);
    await page.waitForTimeout(2500);
    await page.screenshot({ path: `${out}/${label}-scrolled.png` });
  }
}

await browser.close();
