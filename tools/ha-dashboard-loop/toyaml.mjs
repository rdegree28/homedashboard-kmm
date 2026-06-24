import { readFileSync, writeFileSync } from 'node:fs';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const yaml = require('js-yaml');
const cfg = JSON.parse(readFileSync('./office_fixed.json','utf8'));
const out = yaml.dump(cfg, { lineWidth: -1, noRefs: true });
writeFileSync('./office.yaml', out);
console.log('wrote office.yaml,', out.split('\n').length, 'lines');
