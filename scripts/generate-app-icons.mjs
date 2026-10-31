// Rasterizes the existing TiqueMark geometry; rsvg-convert is a development tool only.
import { mkdirSync, writeFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
const dir = new URL('../assets/branding/', import.meta.url);
mkdirSync(dir, { recursive: true });
const glyph = color => `<g fill="none" stroke="${color}" stroke-width="1"><rect x="-9" y="-9" width="18" height="18"/><rect x="-9" y="-9" width="18" height="18" transform="rotate(45)"/></g><rect x="-2.5" y="-2.5" width="5" height="5" fill="${color}" transform="rotate(45)"/>`;
writeFileSync(new URL('mark.svg',dir), `<svg xmlns="http://www.w3.org/2000/svg" viewBox="-14 -14 28 28">${glyph('#FFB020')}</svg>\n`);
for (const [name, color, scale, background] of [
  ['icon','#FFB020',26,true], ['foreground','#FFB020',19,false], ['monochrome','#FFFFFF',19,false],
]) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">${background ? '<rect width="1024" height="1024" fill="#080B10"/>' : ''}<g transform="translate(512 512) scale(${scale})">${glyph(color)}</g></svg>\n`;
  const source = new URL(`${name}.svg`,dir), target = new URL(`${name}.png`,dir);
  writeFileSync(source, svg);
  execFileSync('rsvg-convert',['--output',target.pathname,source.pathname]);
}
