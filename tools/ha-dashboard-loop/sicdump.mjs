import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';
const token = readFileSync('./.ha_token','utf8').trim();
const base = readFileSync('./.ha_env','utf8').match(/HA_URL=(.*)/)[1].trim();
const hassTokens={access_token:token,token_type:'Bearer',expires_in:1800,hassUrl:base,clientId:base+'/',expires:Date.now()+3.15e11,refresh_token:'x'};
const b=await chromium.launch();const c=await b.newContext({viewport:{width:500,height:1000}});const p=await c.newPage();
await p.addInitScript(t=>localStorage.setItem('hassTokens',JSON.stringify(t)),hassTokens);
await p.goto(base+'/scratch-pad/scratch',{waitUntil:'load'});await p.waitForTimeout(3500);
const res=await p.evaluate(()=>{
  const findTag=(node,tag,d)=>{if(d>18)return null;const kids=[...(node.shadowRoot?node.shadowRoot.children:[]),...node.children];
    for(const el of kids){if(el.tagName.toLowerCase()===tag)return el;const r=findTag(el,tag,d+1);if(r)return r;}return null;};
  const sic=findTag(document.body,'stack-in-card',0);
  if(!sic)return 'no stack-in-card';
  const root=sic.shadowRoot||sic;
  // find any element whose direct text is "Lights"
  let titleEl=null;
  const walk=(n,d)=>{if(d>10||titleEl)return;const kids=[...(n.shadowRoot?n.shadowRoot.children:[]),...n.children];
    for(const el of kids){const own=[...el.childNodes].filter(x=>x.nodeType===3).map(x=>x.textContent.trim()).join('');
      if(own==='Lights'){titleEl=el;return;}walk(el,d+1);}};
  walk(sic,0);
  const info=(el)=>{if(!el)return null;const r=el.getBoundingClientRect();const cs=getComputedStyle(el);
    return {tag:el.tagName.toLowerCase(),cls:''+el.className,y:Math.round(r.top),h:Math.round(r.height),font:cs.fontSize,pad:cs.padding,mar:cs.margin};};
  return JSON.stringify({sicShadowHTML:root.innerHTML.replace(/<!--.*?-->/g,'').slice(0,300), titleEl:info(titleEl)},null,2);
});
console.log(res);
await b.close();
