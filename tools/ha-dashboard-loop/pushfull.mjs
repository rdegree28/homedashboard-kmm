import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
const cfg = JSON.parse(readFileSync('./office_fixed.json','utf8'));
const config = { title:'Scratch', views:[{ title:'Scratch', path:'scratch', cards: cfg.views[0].cards }] };
execFileSync('node',['ha.mjs',JSON.stringify({type:'lovelace/config/save',url_path:'scratch-pad',config})],{stdio:'inherit'});
