#!/usr/bin/env node
// Fails if any tracked text file matches a generic sensitive pattern or an entry
// from the private deny-list. The deny-list itself must never be committed:
// pass it with --denylist <file> or REDACTION_DENYLIST_FILE.
//
// Deny-list format: one pattern per line, '#' for comments.
//   literal text           -> case-insensitive substring match
//   re:<regex>             -> case-insensitive regular expression

import { execSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';

const BINARY = /\.(png|jpe?g|gif|webp|ico|svg|mp4|mov|pdf|zip|jar|class|woff2?)$/i;

const GENERIC_RULES = [
  { name: 'AWS account id in ARN', re: /arn:aws[a-z-]*:[a-z0-9-]+:[a-z0-9-]*:\d{12}:/i },
  { name: 'bare 12-digit number (possible AWS account id)', re: /(?<![\d.])\d{12}(?![\d.])/ },
  { name: 'RDS endpoint hostname', re: /[a-z0-9-]+\.[a-z0-9]+\.[a-z0-9-]+\.rds\.amazonaws\.com/i },
  { name: 'AWS access key id', re: /\b(AKIA|ASIA)[A-Z0-9]{16}\b/ },
  { name: 'Google API key', re: /\bAIza[0-9A-Za-z_-]{35}\b/ },
  { name: 'private key block', re: /-----BEGIN [A-Z ]*PRIVATE KEY-----/ },
  { name: 'Firebase hosting domain', re: /[a-z0-9-]+\.(firebaseapp\.com|web\.app)\b/i },
  { name: 'company subdomain (use api.example.com)', re: /\b[a-z0-9-]+\.backspacestudios\.com\b/i },
  { name: 'private IPv4 address', re: /\b(10\.\d{1,3}|192\.168|172\.(1[6-9]|2\d|3[01]))\.\d{1,3}\.\d{1,3}\b/ },
  {
    name: 'email address (only example.com / example.org allowed)',
    re: /\b[A-Za-z0-9._%+-]+@(?!example\.(com|org)\b)[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b/,
  },
];

function parseArgs() {
  const idx = process.argv.indexOf('--denylist');
  return idx > -1 ? process.argv[idx + 1] : process.env.REDACTION_DENYLIST_FILE;
}

function loadDenylist(file) {
  if (!file) return [];
  if (!existsSync(file)) {
    console.error(`Deny-list not found: ${file}`);
    process.exit(2);
  }
  return readFileSync(file, 'utf8')
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l && !l.startsWith('#'))
    .map((l, i) => ({
      name: `deny-list entry #${i + 1}`,
      re: l.startsWith('re:')
        ? new RegExp(l.slice(3), 'i')
        : new RegExp(l.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'i'),
    }));
}

function listFiles() {
  const out = execSync('git ls-files --cached --others --exclude-standard', { encoding: 'utf8' });
  return out.split('\n').filter((f) => f && !BINARY.test(f));
}

const denyFile = parseArgs();
const rules = [...GENERIC_RULES, ...loadDenylist(denyFile)];
let findings = 0;

for (const file of listFiles()) {
  if (!existsSync(file)) continue;
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, n) => {
    for (const rule of rules) {
      if (rule.re.test(line)) {
        findings += 1;
        // Never echo the matched text: the deny-list contents are private.
        console.log(`${file}:${n + 1}  ${rule.name}`);
      }
    }
  });
}

console.log(
  `\nRedaction check: ${findings} finding(s) across ${rules.length} rules` +
    (denyFile ? '' : ' (generic rules only; no deny-list supplied)'),
);
process.exit(findings ? 1 : 0);
