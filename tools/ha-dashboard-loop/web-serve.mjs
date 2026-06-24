import http from 'node:http';
import { readFile } from 'node:fs/promises';
import { existsSync, statSync } from 'node:fs';
import { join, extname } from 'node:path';

const ROOT = process.argv[2];
const PORT = Number(process.argv[3] || 8090);
const MIME = {
  '.html': 'text/html', '.js': 'application/javascript', '.mjs': 'application/javascript',
  '.wasm': 'application/wasm', '.css': 'text/css', '.map': 'application/json',
  '.json': 'application/json', '.txt': 'text/plain',
};

http.createServer(async (req, res) => {
  let p = decodeURIComponent((req.url || '/').split('?')[0]);
  if (p === '/' || p === '') p = '/index.html';
  let f = join(ROOT, p);
  if (!existsSync(f) || statSync(f).isDirectory()) f = join(ROOT, 'index.html');
  try {
    const data = await readFile(f);
    res.writeHead(200, { 'Content-Type': MIME[extname(f)] || 'application/octet-stream' });
    res.end(data);
  } catch {
    res.writeHead(404);
    res.end('not found');
  }
}).listen(PORT, () => console.log(`serving ${ROOT} on http://localhost:${PORT}`));
