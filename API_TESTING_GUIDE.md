# Backend API Testing Guide

## Quick Start: Testing with Postman/Thunder Client/cURL

Your backend is running on `http://localhost:8080` and requires JWT authentication for most endpoints.

---

## Step 1: Login to Get JWT Token

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNjcwMDAwMCwiZXhwIjoxNzA2Nzg2NDAwfQ...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@erp.com",
  "roles": ["ROLE_ADMIN"]
}
```

**Copy the `token` value** - you'll need it for all other requests!

---

## Step 2: Use Token for Protected Endpoints

For **ALL** other API calls (except `/api/auth/*`), you must include the JWT token in the Authorization header.

### Example: Get All Employees

**Endpoint:** `GET http://localhost:8080/api/employees`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNjcwMDAwMCwiZXhwIjoxNzA2Nzg2NDAwfQ...
Content-Type: application/json
```

⚠️ **IMPORTANT:** Replace the token with YOUR actual token from Step 1!

---

## Step 3: Create/Update Data

### Example: Create Department

**Endpoint:** `POST http://localhost:8080/api/departments`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Engineering",
  "description": "Software Development Team",
  "managerId": null
}
```

---

## Testing with Different Tools

### Option 1: Postman

1. **Create a new request**
2. **Set method to POST** and URL to `http://localhost:8080/api/auth/login`
3. **Go to Body tab** → Select "raw" → Select "JSON"
4. **Paste the login JSON** (see Step 1)
5. **Click Send**
6. **Copy the token** from response
7. **For other requests:**
   - Go to "Authorization" tab
   - Select "Bearer Token"
   - Paste your token

### Option 2: VS Code Thunder Client Extension

1. **Install Thunder Client** extension
2. **Create New Request**
3. **Method:** POST
4. **URL:** `http://localhost:8080/api/auth/login`
5. **Body tab:** Select JSON and paste login credentials
6. **Send** and copy token
7. **For protected endpoints:**
   - Click "Auth" tab
   - Select "Bearer"
   - Paste token

### Option 3: cURL (Command Line)

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

**Use Token (replace YOUR_TOKEN):**
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

---

## Common API Endpoints

### HR Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/employees` | Get all employees | ✅ Yes |
| POST | `/api/employees` | Create employee | ✅ Yes |
| PUT | `/api/employees/{id}` | Update employee | ✅ Yes |
| DELETE | `/api/employees/{id}` | Delete employee | ✅ Yes |
| GET | `/api/departments` | Get all departments | ✅ Yes |
| POST | `/api/departments` | Create department | ✅ Yes |

### Finance Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/accounts` | Get all accounts | ✅ Yes |
| POST | `/api/accounts` | Create account | ✅ Yes |
| GET | `/api/expenses` | Get all expenses | ✅ Yes |
| POST | `/api/expenses` | Create expense | ✅ Yes |

### Inventory Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/products` | Get all products | ✅ Yes |
| POST | `/api/products` | Create product | ✅ Yes |
| GET | `/api/suppliers` | Get all suppliers | ✅ Yes |
| POST | `/api/suppliers` | Create supplier | ✅ Yes |

### Sales Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/customers` | Get all customers | ✅ Yes |
| POST | `/api/customers` | Create customer | ✅ Yes |
| GET | `/api/sales-orders` | Get all orders | ✅ Yes |
| POST | `/api/sales-orders` | Create order | ✅ Yes |

---

## Troubleshooting 401 Errors

### ❌ Error: "Full authentication is required"

**Cause:** Missing or invalid Authorization header

**Fix:**
1. Make sure you've logged in first
2. Copy the token from login response
3. Add header: `Authorization: Bearer YOUR_TOKEN`
4. Make sure there's a space between "Bearer" and the token

### ❌ Error: "JWT token is expired"

**Cause:** Token has expired (default: 24 hours)

**Fix:**
1. Login again to get a new token
2. Use the new token for subsequent requests

### ❌ Error: "Invalid JWT signature"

**Cause:** Token is corrupted or incomplete

**Fix:**
1. Make sure you copied the ENTIRE token
2. Don't add extra spaces or line breaks
3. Login again to get a fresh token

---

## Example: Complete Test Flow

### 1. Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin"
}
```

### 2. Create Department (using token)
```bash
POST http://localhost:8080/api/departments
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "name": "IT Department",
  "description": "Information Technology"
}
```

### 3. Get All Departments
```bash
GET http://localhost:8080/api/departments
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## Public Endpoints (No Auth Required)

These endpoints work WITHOUT a token:

- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register new user
- `GET /api/health` - Health check
- `GET /swagger-ui/index.html` - API Documentation

---

## Tips

1. **Save your token** - Store it temporarily while testing
2. **Token expires** - Default is 24 hours, login again if expired
3. **Use environment variables** - In Postman, save token as `{{jwt_token}}`
4. **Check backend logs** - Look at your Spring Boot console for error details
5. **CORS is enabled** - Frontend on localhost:5173 can access the API

---

## Quick Test Script (Save as test-api.http)

```http
### Login
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

### Get Employees (replace TOKEN)
GET http://localhost:8080/api/employees
Authorization: Bearer YOUR_TOKEN_HERE

### Create Department (replace TOKEN)
POST http://localhost:8080/api/departments
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json

{
  "name": "Sales",
  "description": "Sales Team"
}
```

---

**Need help?** Check your backend console for detailed error messages!
