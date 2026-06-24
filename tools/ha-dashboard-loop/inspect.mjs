#!/usr/bin/env node
// Dump the real (shadow-DOM-pierced) geometry + box styles of the scratch card.
import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';

const token = readFileSync(new URL('./.ha_token', import.meta.url), 'utf8').trim();
const base = readFileSync(new URL('./.ha_env', import.meta.url), 'utf8').match(/HA_URL=(.*)/)[1].trim();
const path = process.argv[2] || '/scratch-pad/scratch';

const hassTokens = { access_token: token, token_type: 'Bearer', expires_in: 1800, hassUrl: base, clientId: base + '/', expires: Date.now() + 3.15e11, refresh_token: 'x' };

const browser = await chromium.launch();
const ctx = await browser.newContext({ viewport: { width: 500, height: 1000 }, deviceScaleFactor: 1 });
const page = await ctx.newPage();
await page.addInitScript((t) => localStorage.setItem('hassTokens', JSON.stringify(t)), hassTokens);
await page.goto(base + path, { waitUntil: 'load' });
await page.waitForTimeout(3500);

const tree = await page.evaluate(() => {
  const out = [];
  const interesting = (el) => {
    const t = el.tagName.toLowerCase();
    return t.includes('card') || t.includes('chip') || t === 'ha-card' ||
           (el.className && typeof el.className === 'string' && /container|chips|header|name/.test(el.className));
  };
  const walk = (node, depth) => {
    if (depth > 14) return;
    const kids = [];
    if (node.shadowRoot) kids.push(...node.shadowRoot.children);
    kids.push(...node.children);
    for (const el of kids) {
      if (interesting(el)) {
        const r = el.getBoundingClientRect();
        const cs = getComputedStyle(el);
        out.push({
          tag: el.tagName.toLowerCase(),
          cls: (typeof el.className === 'string' ? el.className : '').slice(0, 30),
          y: Math.round(r.top), h: Math.round(r.height),
          pad: cs.padding, mar: cs.margin,
        });
      }
      walk(el, depth + 1);
    }
  };
  walk(document.body, 0);
  return out;
});

console.log('tag'.padEnd(26), 'y'.padStart(5), 'h'.padStart(5), '  padding'.padEnd(22), 'margin');
for (const n of tree) {
  console.log(
    (n.tag + (n.cls ? '.' + n.cls : '')).padEnd(26),
    String(n.y).padStart(5), String(n.h).padStart(5), ' ',
    n.pad.padEnd(20), n.mar
  );
}
await browser.close();
