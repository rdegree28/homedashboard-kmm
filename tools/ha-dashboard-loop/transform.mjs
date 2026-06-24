#!/usr/bin/env node
// Apply the spacing-fix pattern to the whole Office config -> office_fixed.json
import { readFileSync, writeFileSync } from 'node:fs';

const cfg = JSON.parse(readFileSync('./office_config.json', 'utf8'));

const HEAD_STYLE =
  "ha-card {\n  box-shadow: none !important;\n  border: none !important;\n  background: none !important;\n  padding: 10px 12px 0 !important;\n}\n" +
  ".container {\n  padding: 0 !important;\n}\n" +
  ".primary {\n  font-size: 15px !important;\n  font-weight: 600 !important;\n  letter-spacing: 0.02em;\n}\n";

const HEADER_SHRINK =
  "\n.card-header {\n  font-size: 15px !important;\n  font-weight: 600 !important;\n  padding: 10px 16px 4px !important;\n  letter-spacing: 0.02em;\n}\n";

const headingCard = (title) => ({
  type: 'custom:mushroom-template-card',
  primary: title,
  card_mod: { style: HEAD_STYLE },
});

// append a string snippet onto an existing card_mod string style (or create one)
const appendStyle = (card, snippet) => {
  if (!card.card_mod) card.card_mod = { style: '' };
  if (typeof card.card_mod.style === 'string') {
    card.card_mod.style += snippet;
  } // object styles (chips use {$:...}) are left alone
};

const walk = (card) => {
  if (!card || typeof card !== 'object') return;
  // stack-in-card: replace title with a heading child card
  if (card.type === 'custom:stack-in-card' && card.title) {
    const title = card.title;
    delete card.title;
    card.cards = [headingCard(title), ...(card.cards || [])];
  }
  // history-graph with a title (Power Usage): shrink its .card-header (fires when nested)
  if (card.type === 'history-graph' && card.title) {
    appendStyle(card, HEADER_SHRINK);
  }
  // recurse
  for (const k of ['cards']) if (Array.isArray(card[k])) card[k].forEach(walk);
};

cfg.views.forEach((v) => {
  v.cards = (v.cards || []).map((card) => {
    walk(card);
    // top-level plain entities card: card-mod won't fire standalone, so wrap in
    // a stack-in-card (where card-mod does process children) and shrink its header.
    if (card.type === 'entities' && card.title) {
      appendStyle(card, HEADER_SHRINK);
      return { type: 'custom:stack-in-card', cards: [card] };
    }
    return card;
  });
});
writeFileSync('./office_fixed.json', JSON.stringify(cfg, null, 2));
console.log('wrote office_fixed.json; top-level cards:', cfg.views[0].cards.map((c) => c.type).join(', '));
