#!/usr/bin/env node
// Push scratch_card.json as the sole card of the scratch-pad dashboard.
import { readFileSync } from 'node:fs';
const card = JSON.parse(readFileSync(new URL('./scratch_card.json', import.meta.url), 'utf8'));
const config = { title: 'Scratch', views: [{ title: 'Scratch', path: 'scratch', cards: [card] }] };
const { execFileSync } = await import('node:child_process');
const cmd = JSON.stringify({ type: 'lovelace/config/save', url_path: 'scratch-pad', config });
execFileSync('node', ['ha.mjs', cmd], { stdio: 'inherit', cwd: new URL('.', import.meta.url) });
