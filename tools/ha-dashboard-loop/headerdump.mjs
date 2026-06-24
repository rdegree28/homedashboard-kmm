import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';
const token = readFileSync('./.ha_token','utf8').trim();
const base = readFileSync('./.ha_env','utf8').match(/HA_URL=(.*)/)[1].trim();
const hassTokens={access_token:token,token_type:'Bearer',expires_in:1800,hassUrl:base,clientId:base+'/',expires:Date.now()+3.15e11,refresh_token:'x'};
const b=await chromium.launch();const c=await b.newContext({viewport:{width:500,height:1000}});const p=await c.newPage();
await p.addInitScript(t=>localStorage.setItem('hassTokens',JSON.stringify(t)),hassTokens);
await p.goto(base+'/scratch-pad/scratch',{waitUntil:'load'});await p.waitForTimeout(3500);
const html=await p.evaluate(()=>{
  const find=(node,d)=>{if(d>16)return null;const kids=[...(node.shadowRoot?node.shadowRoot.children:[]),...node.children];
    for(const el of kids){if(el.className&&/header/.test(''+el.className))return el;const r=find(el,d+1);if(r)return r;}return null;};
  const h=find(document.body,0); if(!h)return 'NO HEADER';
  const titleEl=h.querySelector('*'); const cs=titleEl?getComputedStyle(titleEl):null;
  return JSON.stringify({headerHTML:h.outerHTML.slice(0,400), firstChildTag:titleEl?titleEl.tagName.toLowerCase():null, firstChildClass:titleEl?''+titleEl.className:null, fontSize:cs?cs.fontSize:null, pad:cs?cs.padding:null},null,2);
});
console.log(html);
await b.close();
