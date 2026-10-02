#!/usr/bin/env node
// Prints aggregate size metrics for the private Flame repositories.
// Output is counts only, safe to paste into docs/09-engineering-metrics.md.
//
//   node scripts/collect-metrics.mjs --frontend <path> --backend <path>

import { execSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

function arg(name) {
  const i = process.argv.indexOf(`--${name}`);
  if (i === -1 || !process.argv[i + 1]) {
    console.error(`missing --${name} <path>`);
    process.exit(2);
  }
  return process.argv[i + 1];
}

const frontend = arg('frontend');
const backend = arg('backend');

function tracked(repo, pathspec) {
  return execSync(`git -C "${repo}" ls-files -- ${pathspec}`, { encoding: 'utf8' })
    .split('\n')
    .filter(Boolean);
}

function loc(repo, files) {
  return files.reduce((n, f) => n + readFileSync(join(repo, f), 'utf8').split('\n').length, 0);
}

function count(repo, files, re) {
  return files.reduce((n, f) => n + (readFileSync(join(repo, f), 'utf8').match(re) || []).length, 0);
}

const code = (files) => files.filter((f) => /\.(ts|tsx)$/.test(f) && !f.endsWith('.d.ts'));
const commits = (repo) => execSync(`git -C "${repo}" rev-list --count HEAD`, { encoding: 'utf8' }).trim();

const web = code(tracked(frontend, 'apps/web/src'));
const mobile = code(tracked(frontend, 'apps/mobile/src apps/mobile/App.tsx'));
const pkgs = code(tracked(frontend, 'packages'));
const hooks = code(tracked(frontend, 'packages/shared-hooks/src'));
const types = code(tracked(frontend, 'packages/shared-types/src'));
const apiMods = tracked(frontend, 'packages/shared-api/src').filter((f) => /Api\.ts$/.test(f));

const java = tracked(backend, 'src/main/java').filter((f) => f.endsWith('.java'));
const tests = tracked(backend, 'src/test/java').filter((f) => f.endsWith('.java'));
const inDir = (dir) => java.filter((f) => f.split('/').at(-2) === dir).length;

const rows = [
  ['Frontend TypeScript LOC (apps + packages)', loc(frontend, [...web, ...mobile, ...pkgs])],
  ['  web app LOC', loc(frontend, web)],
  ['  mobile app LOC', loc(frontend, mobile)],
  ['  shared packages LOC', loc(frontend, pkgs)],
  ['Web pages', web.filter((f) => /Page\.tsx$/.test(f)).length],
  ['Mobile screens', mobile.filter((f) => /Screen\.tsx$/.test(f)).length],
  ['Shared data hooks', count(frontend, hooks, /export (function|const) use[A-Z]/g)],
  ['Shared domain types', count(frontend, types, /^export (interface|type) /gm)],
  ['Shared API modules', apiMods.length],
  ['Backend Java LOC', loc(backend, java)],
  ['Backend Java files', java.length],
  ['JPA entities', count(backend, java, /^@Entity/gm)],
  ['Controllers', java.filter((f) => f.endsWith('Controller.java')).length],
  ['Services', inDir('service')],
  ['Repositories', inDir('repository')],
  ['DTOs', inDir('dto')],
  ['Enums', inDir('enums')],
  ['REST endpoints', count(backend, java, /@(Get|Post|Put|Patch|Delete)Mapping/g)],
  ['@PreAuthorize rules', count(backend, java, /@PreAuthorize/g)],
  ['Scheduled jobs', count(backend, java, /@Scheduled/g)],
  ['Backend test methods', count(backend, tests, /@Test\b/g)],
  ['Commits (frontend / backend)', `${commits(frontend)} / ${commits(backend)}`],
];

const width = Math.max(...rows.map(([k]) => k.length));
for (const [k, v] of rows) console.log(`${k.padEnd(width)}  ${typeof v === 'number' ? v.toLocaleString('en-US') : v}`);
