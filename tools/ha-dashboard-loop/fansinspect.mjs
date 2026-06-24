import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';
const token=readFileSync('./.ha_token','utf8').trim();
const base=readFileSync('./.ha_env','utf8').match(/HA_URL=(.*)/)[1].trim();
const t={access_token:token,token_type:'Bearer',expires_in:1800,hassUrl:base,clientId:base+'/',expires:Date.now()+3.15e11,refresh_token:'x'};
const b=await chromium.launch();const c=await b.newContext({viewport:{width:500,height:1000}});const p=await c.newPage();
await p.addInitScript(x=>localStorage.setItem('hassTokens',JSON.stringify(x)),t);
await p.goto(base+'/scratch-pad/scratch',{waitUntil:'load'});await p.waitForTimeout(3500);
const r=await p.evaluate(()=>{
  // find hui-entities-card
  const ft=(n,tag,d)=>{if(d>20)return null;const k=[...(n.shadowRoot?n.shadowRoot.children:[]),...n.children];for(const e of k){if(e.tagName.toLowerCase()===tag)return e;const x=ft(e,tag,d+1);if(x)return x;}return null;};
  const ent=ft(document.body,'hui-entities-card',0);
  if(!ent)return 'no hui-entities-card';
  const ha=ent.shadowRoot?ent.shadowRoot.querySelector('ha-card'):null;
  const hdr=ha?ha.querySelector('.card-header'):null;
  // is there a card-mod injected style in ha-card shadow? check styles count
  const styleTags=ha?[...ha.querySelectorAll('style'),...(ha.shadowRoot?ha.shadowRoot.querySelectorAll('style'):[])]:[];
  return JSON.stringify({
    haFound: !!ha,
    headerSelectorLightDom: !!hdr,
    headerHTML: ha? (ha.querySelector('.card-header')?.outerHTML.slice(0,160) || 'no .card-header in ha-card light dom') : 'no ha-card',
    haChildrenTags: ha? [...ha.children].map(x=>x.tagName.toLowerCase()+(x.className?'.'+x.className:'')).slice(0,6) : [],
    cardModStyles: styleTags.map(s=>s.textContent.slice(0,40)),
  },null,2);
});
console.log(r);
await b.close();
