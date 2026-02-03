# Backend – Production Deployment

## Required configuration

1. **Activate production profile**

   - Set `spring.profiles.active=prod` (env or command line).

2. **Secrets (must not use dev defaults)**

   - `erp.app.jwtSecret` – Set via env `JWT_SECRET` (min 64 chars for HS512).
   - `erp.admin.password` – Set via env `ADMIN_PASSWORD` for initial admin user.
   - Database URL, username, password – Set via Spring datasource env vars or `application-prod.properties`.

3. **CORS**

   - Set `CORS_ALLOWED_ORIGINS` (comma-separated) to your frontend URL(s), e.g. `https://yourerp.com`.
   - Or set `erp.app.cors.allowed-origins` in `application-prod.properties`.

4. **Cookie**

   - `erp.app.cookie.secure=true` is set in `application-prod.properties` (use HTTPS).

5. **Dev mode**
   - `erp.dev.mode=false` in prod profile (no default JWT secret, no default passwords).

## Example env vars (production)

```bash
export spring_profiles_active=prod
export JWT_SECRET=your-64-char-minimum-secret-key-here
export ADMIN_PASSWORD=secure-admin-password
export CORS_ALLOWED_ORIGINS=https://yourerp.com
# Database (example)
export spring_datasource_url=jdbc:mysql://your-db-host:3306/erp_database?useSSL=true
export spring_datasource_username=erp_user
export spring_datasource_password=secure-db-password
```

## Optional

- Reduce logging: already set in `application-prod.properties` (root=INFO, no SQL logging).
- Swagger: consider disabling or restricting in prod (`springdoc.swagger-ui.enabled=false` if desired).
