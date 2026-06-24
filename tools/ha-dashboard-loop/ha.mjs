#!/usr/bin/env node
// Minimal Home Assistant WebSocket client.
// Usage: node ha.mjs '<json-command>'
// Reads URL from .ha_env (HA_URL=...) and token from .ha_token.
import { readFileSync } from 'node:fs';

const token = readFileSync(new URL('./.ha_token', import.meta.url), 'utf8').trim();
const env = readFileSync(new URL('./.ha_env', import.meta.url), 'utf8');
const httpUrl = (env.match(/HA_URL=(.*)/)?.[1] || '').trim();
const wsUrl = httpUrl.replace(/^http/, 'ws') + '/api/websocket';

const cmd = JSON.parse(process.argv[2]);

const ws = new WebSocket(wsUrl);
let id = 1;

ws.addEventListener('message', (ev) => {
  const msg = JSON.parse(ev.data);
  if (msg.type === 'auth_required') {
    ws.send(JSON.stringify({ type: 'auth', access_token: token }));
  } else if (msg.type === 'auth_ok') {
    ws.send(JSON.stringify({ id: id, ...cmd }));
  } else if (msg.type === 'auth_invalid') {
    console.error('AUTH INVALID:', msg.message);
    process.exit(1);
  } else if (msg.type === 'result') {
    if (!msg.success) {
      console.error('CMD FAILED:', JSON.stringify(msg.error));
      process.exit(1);
    }
    console.log(JSON.stringify(msg.result, null, 2));
    ws.close();
    process.exit(0);
  }
});

ws.addEventListener('error', (e) => {
  console.error('WS ERROR:', e.message || e);
  process.exit(1);
});

setTimeout(() => { console.error('timeout'); process.exit(1); }, 15000);
