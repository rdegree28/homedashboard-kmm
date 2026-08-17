// Decode two PNGs in a headless browser canvas and report where they differ.
import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';
const [a, b] = process.argv.slice(2);
const enc = (p) => 'data:image/png;base64,' + readFileSync(p).toString('base64');
const browser = await chromium.launch();
const page = await browser.newPage();
const res = await page.evaluate(async ([da, db]) => {
  const load = (src) => new Promise((r) => { const i = new Image(); i.onload = () => r(i); i.src = src; });
  const [ia, ib] = await Promise.all([load(da), load(db)]);
  const px = (img) => { const c = document.createElement('canvas'); c.width = img.width; c.height = img.height;
    c.getContext('2d').drawImage(img, 0, 0); return c.getContext('2d').getImageData(0, 0, c.width, c.height).data; };
  const pa = px(ia), pb = px(ib);
  let n = 0, minX = 1e9, maxX = -1, minY = 1e9, maxY = -1;
  for (let i = 0; i < pa.length; i += 4) {
    if (Math.abs(pa[i] - pb[i]) > 6 || Math.abs(pa[i+1] - pb[i+1]) > 6 || Math.abs(pa[i+2] - pb[i+2]) > 6) {
      n++; const p = (i / 4), x = p % ia.width, y = (p / ia.width) | 0;
      if (x < minX) minX = x; if (x > maxX) maxX = x; if (y < minY) minY = y; if (y > maxY) maxY = y;
    }
  }
  return { n, total: pa.length / 4, box: maxX < 0 ? null : [minX, minY, maxX, maxY] };
}, [enc(a), enc(b)]);
console.log(JSON.stringify(res));
await browser.close();
