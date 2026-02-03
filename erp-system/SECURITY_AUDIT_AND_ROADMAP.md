# ERP Frontend – Security Audit & Feature Roadmap

**Audit date:** February 2025  
**Scope:** Frontend (erp-system) security, data exposure, and recommended updates.

---

## 1. Security – What’s Already Good

| Area                 | Status | Notes                                                                                                                      |
| -------------------- | ------ | -------------------------------------------------------------------------------------------------------------------------- |
| **JWT storage**      | OK     | Token in HttpOnly cookie only; not in localStorage (XSS-safe).                                                             |
| **Auth gate**        | OK     | All app routes (except `/login`, `/register`) behind `ProtectedRoute`; unauthenticated users redirected to login.          |
| **Role checks**      | OK     | UI hides actions by role (`hasRole` / `hasAnyRole`); backend must and does enforce permissions (403 on forbidden actions). |
| **No DOM XSS**       | OK     | No `dangerouslySetInnerHTML`, `innerHTML`, or `eval()` found.                                                              |
| **Sensitive inputs** | OK     | Password fields use `type="password"`.                                                                                     |
| **Credentials**      | OK     | `withCredentials: true` for cookie; no token in JS.                                                                        |

---

## 2. Vulnerabilities & Risks (Frontend)

### 2.1 High / Medium

| Issue                             | Risk                                                                                           | Recommendation                                                                                                                          |
| --------------------------------- | ---------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **API base URL hardcoded**        | Wrong backend in prod; harder to deploy per environment.                                       | Use env variable (e.g. `VITE_API_URL`) and `import.meta.env.VITE_API_URL` in `axiosConfig.js`; set in build/deploy.                     |
| **localStorage user object**      | User can tamper (e.g. add `ROLE_ADMIN`). UI may show admin options; backend still returns 403. | Acceptable for UX; keep. Optionally re-fetch profile on load (`GET /api/auth/me`) and sync to state so tampering is overwritten.        |
| **401 redirect loses return URL** | After session expiry, user is sent to `/login` with no “return here after login.”              | Store `returnTo` (e.g. `window.location.pathname + search`) before `window.location.href = '/login'` and use it after successful login. |
| **index.html theme script**       | Was broken (typos); theme could fail on first paint.                                           | Fixed in codebase (correct `document.documentElement.classList` usage).                                                                 |

### 2.2 Low / Informational

| Issue                           | Risk                                                                       | Recommendation                                                                                         |
| ------------------------------- | -------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| **Console.error in production** | Error objects may contain stack traces or API details in browser devtools. | Strip in build or use a logger that disables detailed logs when `import.meta.env.PROD`.                |
| **“Forgot password” link**      | Points to `href="#"`; no real flow.                                        | Either implement forgot-password (backend + UI) or remove/replace with “Contact admin.”                |
| **Error messages from API**     | `err.response?.data?.message` shown in toasts.                             | Backend should avoid leaking internal details; frontend is fine as long as API messages are user-safe. |
| **CORS**                        | Handled by backend (e.g. allowed origins).                                 | Ensure production origin is added in backend CORS config.                                              |

---

## 3. Important Information Exposure

| Data                            | Location                 | Accessible?            | Notes                                                 |
| ------------------------------- | ------------------------ | ---------------------- | ----------------------------------------------------- |
| JWT                             | Cookie (HttpOnly)        | No (not from JS)       | Correct.                                              |
| User id, username, email, roles | localStorage `user` + UI | Yes (same-origin)      | Needed for UI; not secret. Roles enforced on backend. |
| Theme / sidebar / settings      | localStorage             | Yes                    | Preferences only; low sensitivity.                    |
| API responses                   | Memory / network tab     | Yes (same-origin)      | Normal; backend should not return secrets.            |
| List/table data                 | Rendered in tables       | Yes when authenticated | By design; protect with auth + RBAC on backend.       |

**Conclusion:** No critical secrets (passwords, tokens, keys) are exposed through the frontend. Sensitive operations are protected by authentication and should be authorized on the backend.

---

## 4. Access Control Checklist

- **Routes:** All app routes except `/login` and `/register` are behind `ProtectedRoute` (checks `user` from context). Direct URL access to e.g. `/profile`, `/settings`, `/hr/employees` still requires “logged-in” state (and cookie for API).
- **Actions (buttons/tables):** Delete/create/edit visibility is driven by `hasRole` / `hasAnyRole`. Actual enforcement is on the server; 403 is expected if a user tampers client state.
- **Profile / Settings:** Under same layout and `ProtectedRoute`; no extra exposure.

**Recommendation:** Keep backend as single source of truth for authorization; frontend role checks are for UX only.

---

## 5. Fixes Applied in This Audit

1. **index.html** – Theme script corrected (syntax and `document.documentElement.classList` usage) so dark/light/system theme applies on first paint.
2. **API base URL** – Now uses `import.meta.env.VITE_API_URL` with fallback `http://localhost:8080/api` for dev. Set `VITE_API_URL` in production build.
3. **Return URL on 401** – Axios interceptor stores current path in `sessionStorage` before redirecting to `/login`; Login page redirects back to that path after successful login.
4. **Production hardening (frontend):** `.env.example` added; `src/utils/logger.js` added so `logger.error()` in production does not log error objects (avoids leaking stack/response); all `console.error` replaced with `logger.error`; "Forgot password?" replaced with "Forgot password? Contact admin"; AuthContext re-validates user on load via `GET /api/auth/me` and syncs/clears state to prevent tampered localStorage.
5. **Production hardening (backend):** CORS origins configurable via `erp.app.cors.allowed-origins` (comma-separated); JWT cookie `Secure` flag via `erp.app.cookie.secure`; `application-prod.properties` added (cookie secure, CORS from env, reduced logging, `erp.dev.mode=false`). See `enterprise-system/PRODUCTION_DEPLOYMENT.md`.

---

## 6. Recommended Code Changes (Short Term)

1. **Environment-based API URL**

   - In `vite.config.js`: ensure `envPrefix: 'VITE_'` (default).
   - In `src/api/axiosConfig.js`: set `baseURL` to `import.meta.env.VITE_API_URL || 'http://localhost:8080/api'`.
   - In production build, set `VITE_API_URL` to the real API base URL.

2. **Optional: Re-validate user on load**

   - After restoring `user` from localStorage, call `GET /api/auth/me` and replace context state with response (or redirect to login if 401). Reduces impact of localStorage tampering.

3. **Optional: Return URL on 401**
   - In axios response interceptor, before `window.location.href = '/login'`, set e.g. `sessionStorage.setItem('returnTo', window.location.pathname + window.location.search)` and in Login page after successful login `navigate(returnTo || '/')`.

---

## 7. Features / Improvements to Consider

| Priority | Feature                                   | Notes                                                        |
| -------- | ----------------------------------------- | ------------------------------------------------------------ |
| High     | **Env-based API URL**                     | Required for staging/production.                             |
| High     | **Forgot password**                       | Either implement (backend + email/link) or remove dead link. |
| Medium   | **Return URL after login**                | Better UX when session expires.                              |
| Medium   | **Rate limiting / loading states**        | Prevent double-submit and give feedback on slow networks.    |
| Medium   | **Audit log (backend)**                   | Who did what and when; not frontend-only.                    |
| Low      | **Stricter input validation**             | Match backend (length, format) to reduce unnecessary errors. |
| Low      | **Remove or guard console.error in prod** | Avoid leaking stack traces.                                  |
| Low      | **CSP / security headers**                | Configure on server (or hosting) that serves the SPA.        |
| Low      | **Dependency audit**                      | Run `npm audit` regularly and fix critical/high.             |

---

## 8. Summary

- **Security posture:** Good for a 90%-done ERP frontend: no token in JS, routes protected, no DOM XSS patterns, roles used for UI with backend enforcement.
- **Main gaps:** Hardcoded API URL (use env), optional return-URL on 401, and either implement or remove “Forgot password.”
- **No critical information** (passwords, tokens, API keys) is exposed through the frontend; important data is only available to authenticated users and should remain authorized on the backend.

Applying the env-based API URL and (optionally) return-URL and profile re-fetch will address the most important remaining items before production.
