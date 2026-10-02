#!/usr/bin/env node
// Regenerates diagrams/README.md from the .mmd sources so the index can never drift.
// Usage: node scripts/build-diagram-index.mjs
import { readdirSync, readFileSync, writeFileSync, existsSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const dir = join(root, 'diagrams');

const captions = {
  '01-system-context': ['System context', 'Who uses Flame and which external services it depends on.', '02-architecture.md'],
  '02-containers': ['Containers', 'Web and mobile clients, shared packages, the API and its data stores.', '02-architecture.md'],
  '03-context-map': ['Bounded-context map', 'The twelve backend modules and the direction of their dependencies.', '04-domain-model.md'],
  '04-auth-token-exchange': ['Identity token exchange', 'Firebase proves identity; the API issues its own JWT and resolves roles per request.', '08-security-model.md'],
  '05-request-lifecycle': ['Request lifecycle', 'From a screen through TanStack Query, Axios, Spring Security and back.', '05-frontend-architecture.md'],
  '06-season-lifecycle': ['Season lifecycle', 'Draft to completed, with the readiness gate in front of activation.', 'domains/02-competition.md'],
  '07-policy-resolution': ['Policy resolution', 'League pack, division and season bindings resolved into one effective rule set.', 'domains/07-policy.md'],
  '08-match-week-readiness': ['Match-week readiness', 'Fixtures, referees and lineups converging before kickoff.', 'domains/05-match.md'],
  '09-lineup-lock': ['Lineup lock decision', 'One definition of "locked" shared by endpoints and the scheduler.', 'domains/05-match.md'],
  '10-referee-appointment-and-report': ['Referee appointment and report', 'Appointment, acceptance, the match report and its downstream effects.', 'domains/06-referee.md'],
  '11-discipline-engine': ['Discipline engine', 'Cards in a report become suspensions and fines idempotently.', 'domains/11-finance-discipline.md'],
  '12-obligation-lifecycle': ['Obligation lifecycle', 'Fees and fines from creation through payment, waiver or cancellation.', 'domains/11-finance-discipline.md'],
  '13-aws-topology': ['AWS topology', 'Fargate in private subnets reaching AWS services through VPC endpoints, no NAT.', '07-cloud-and-devops.md'],
  '14-ci-cd': ['CI/CD', 'GitHub Actions with OIDC for the API image and the static web build.', '07-cloud-and-devops.md'],
  '15-hibernate-mode': ['Hibernate mode', 'One Terraform variable scales the platform to near-zero cost and back.', '07-cloud-and-devops.md'],
};

const files = readdirSync(dir).filter((f) => f.endsWith('.mmd')).sort();
const lines = [
  '# Diagrams',
  '',
  'Every diagram is a Mermaid source (`*.mmd`), so it can be reviewed in a pull request like code. GitHub renders the blocks below directly; pre-rendered SVGs live in [`svg/`](svg/).',
  '',
  'CI re-renders every source with `@mermaid-js/mermaid-cli` on each push, so a broken diagram fails the build.',
  '',
  '| # | Diagram | Shows | Discussed in |',
  '|---|---|---|---|',
];

for (const f of files) {
  const id = f.replace(/\.mmd$/, '');
  const [title, blurb, doc] = captions[id] ?? [id, '', ''];
  const docLink = doc && existsSync(join(root, 'docs', doc)) ? `[${doc}](../docs/${doc})` : '';
  lines.push(`| ${id.slice(0, 2)} | [${title}](#${id}) | ${blurb} | ${docLink} |`);
}

for (const f of files) {
  const id = f.replace(/\.mmd$/, '');
  const [title, blurb] = captions[id] ?? [id, ''];
  const src = readFileSync(join(dir, f), 'utf8').replace(/^\uFEFF/, '').trimEnd();
  lines.push('', `<a id="${id}"></a>`, '', `## ${id.slice(0, 2)}. ${title}`, '', blurb, '', '```mermaid', src, '```', '');
  lines.push(`Source: [\`${f}\`](${f})${existsSync(join(dir, 'svg', `${id}.svg`)) ? ` · SVG: [\`svg/${id}.svg\`](svg/${id}.svg)` : ''}`);
}

writeFileSync(join(dir, 'README.md'), lines.join('\n') + '\n', 'utf8');
console.log(`diagrams/README.md written (${files.length} diagrams)`);
