# Security

## What this repository is (and is not)

This is a **showcase** of the Flame platform. It deliberately contains:

- **No production source code.** Everything under `samples/` was written from scratch to illustrate a pattern. None of it is copied from the production codebase, and none of it is wired to production systems.
- **No credentials.** There are no API keys, service-account files, passwords, tokens or `.env` files.
- **No infrastructure identifiers.** Account numbers, ARNs, resource IDs, hostnames and bucket names are replaced with placeholders such as `<aws-account-id>` or `api.example.com`.
- **No customer data.** Organisations, clubs, people and places in examples, diagrams and screenshots are fictional.

## Safeguards

- Every push and pull request runs [gitleaks](https://github.com/gitleaks/gitleaks) and a redaction check against a private deny-list, which is kept as a repository secret and never committed. See `.github/workflows/docs-lint.yml`.
- GitHub secret scanning and push protection are enabled on this repository.
- The repository was created from an empty `git init`. It shares no history with the production repositories.

## Reporting a vulnerability

If you think you have found a security issue in Flame itself, or something sensitive in this repository, **do not open a public issue**. Contact BackSpace Studios privately through [backspacestudios.com](https://backspacestudios.com). We will acknowledge within 3 business days.
