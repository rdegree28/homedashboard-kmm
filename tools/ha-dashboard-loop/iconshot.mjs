import { chromium } from 'playwright';
const b = await chromium.launch();
const ctx = await b.newContext({ deviceScaleFactor: 2 });
const p = await ctx.newPage();
await p.goto('file://' + process.cwd() + '/iconpreview.html');
await p.waitForTimeout(300);
await p.screenshot({ path: 'icon_preview.png' });
await b.close();
console.log('icon preview rendered');
