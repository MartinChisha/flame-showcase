#!/usr/bin/env node
// Fails if any relative Markdown link points at a file or heading anchor that does not exist.
// Usage: node scripts/check-links.mjs
import { readFileSync, existsSync, statSync } from 'node:fs';
import { execSync } from 'node:child_process';
import { join, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const mdFiles = execSync('git ls-files --cached --others --exclude-standard -- "*.md"', { cwd: root, encoding: 'utf8' })
  .split('\n').filter((f) => f && !f.includes('node_modules/'));

const slug = (heading) => heading.trim().toLowerCase().replace(/[^\p{L}\p{N}\s-]/gu, '').replace(/\s/g, '-');

const anchorsCache = new Map();
function anchorsOf(file) {
  if (!anchorsCache.has(file)) {
    const text = readFileSync(file, 'utf8').replace(/```[\s\S]*?```/g, '');
    const set = new Set();
    for (const m of text.matchAll(/^#{1,6}\s+(.+)$/gm)) set.add(slug(m[1]));
    for (const m of text.matchAll(/<a id="([^"]+)"/g)) set.add(m[1]);
    anchorsCache.set(file, set);
  }
  return anchorsCache.get(file);
}

let broken = 0;
for (const rel of mdFiles) {
  const file = join(root, rel);
  const text = readFileSync(file, 'utf8').replace(/```[\s\S]*?```/g, '').replace(/`[^`\n]*`/g, '');
  for (const m of text.matchAll(/\]\(([^)\s]+)\)/g)) {
    const target = m[1];
    if (/^(https?:|mailto:)/.test(target)) continue;
    const [pathPart, anchor] = target.split('#');
    const dest = pathPart ? resolve(dirname(file), decodeURIComponent(pathPart)) : file;
    if (!existsSync(dest)) { console.error(`${rel}: missing ${target}`); broken++; continue; }
    if (anchor && statSync(dest).isFile() && dest.endsWith('.md') && !anchorsOf(dest).has(anchor)) {
      console.error(`${rel}: missing anchor ${target}`); broken++;
    }
  }
}
console.log(`Link check: ${broken} broken link(s) across ${mdFiles.length} files`);
process.exit(broken ? 1 : 0);
