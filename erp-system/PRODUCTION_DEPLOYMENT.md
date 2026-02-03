# Frontend – Production Deployment

## Build

```bash
npm ci
npm run build
```

## Required environment

Set **before** building (Vite bakes these in at build time):

- **`VITE_API_URL`** – Backend API base URL, including `/api`.  
  Example: `https://api.yourerp.com/api`

Example:

```bash
export VITE_API_URL=https://api.yourerp.com/api
npm run build
```

Or create `.env.production`:

```
VITE_API_URL=https://api.yourerp.com/api
```

Then run `npm run build`. The built files are in `dist/` – serve them with any static host (Nginx, S3, Vercel, etc.).

## Checklist

- [ ] `VITE_API_URL` set to production API URL.
- [ ] Backend CORS includes your frontend origin (e.g. `https://yourerp.com`).
- [ ] Backend runs with HTTPS so the Secure cookie is valid.
- [ ] No secrets in repo; use env / CI for `VITE_*` in production.

## Optional

- Copy `.env.example` to `.env.local` for local overrides (do not commit `.env.local`).
