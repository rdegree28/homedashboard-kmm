import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import { execFileSync } from 'node:child_process';
const require = createRequire(import.meta.url);
const yaml = require('js-yaml');
const cfg = yaml.load(readFileSync('./office.yaml','utf8'));   // parse the DELIVERABLE
const config = { title:'Scratch', views:[{ title:'Scratch', path:'scratch', cards: cfg.views[0].cards }] };
execFileSync('node',['ha.mjs',JSON.stringify({type:'lovelace/config/save',url_path:'scratch-pad',config})],{stdio:'inherit'});
