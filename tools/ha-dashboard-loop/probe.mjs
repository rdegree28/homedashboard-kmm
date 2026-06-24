import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';
const token=readFileSync('./.ha_token','utf8').trim();
const base=readFileSync('./.ha_env','utf8').match(/HA_URL=(.*)/)[1].trim();
const t={access_token:token,token_type:'Bearer',expires_in:1800,hassUrl:base,clientId:base+'/',expires:Date.now()+3.15e11,refresh_token:'x'};
const b=await chromium.launch();const c=await b.newContext({viewport:{width:500,height:1000}});const p=await c.newPage();
await p.addInitScript(x=>localStorage.setItem('hassTokens',JSON.stringify(x)),t);
await p.goto(base+'/scratch-pad/scratch',{waitUntil:'load'});await p.waitForTimeout(3500);
const r=await p.evaluate(()=>{
  const ft=(n,tag,d)=>{if(d>18)return null;const k=[...(n.shadowRoot?n.shadowRoot.children:[]),...n.children];for(const e of k){if(e.tagName.toLowerCase()===tag)return e;const x=ft(e,tag,d+1);if(x)return x;}return null;};
  const sic=ft(document.body,'stack-in-card',0);const ha=sic.shadowRoot.querySelector('ha-card');const h1=ha.shadowRoot.querySelector('h1.card-header');
  return {haBg:getComputedStyle(ha).backgroundColor, headerH:Math.round(h1.getBoundingClientRect().height), headerFont:getComputedStyle(h1).fontSize};
});
console.log('ha-card background:',r.haBg);
console.log('header height     :',r.headerH,'  font:',r.headerFont);
await b.close();
