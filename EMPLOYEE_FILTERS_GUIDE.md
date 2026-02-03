# Employee Filters and Status Management - Implementation Guide

## New Features Added to Employees Page

### 1. Filter by Status ✅

**UI Location:** Filter section (dropdown)

**Options:**
- All Statuses
- Active
- Inactive
- Terminated

**How it works:**
- Select a status from the dropdown to filter employees
- Only employees with the selected status will be shown in the table
- Combines with search and department filters

**Backend Endpoint:**
```
GET /api/employees/status/{status}
```

---

### 2. Filter by Department ✅

**UI Location:** Filter section (dropdown)

**Options:**
- All Departments
- [List of all departments from database]

**How it works:**
- Select a department to see only employees in that department
- Combines with search and status filters
- Useful for department-wise reports

**Backend Endpoint:**
```
GET /api/employees/department/{departmentId}
```

---

### 3. Change Employee Status ✅

**UI Location:** Status column in the table (inline dropdown)

**Options:**
- ACTIVE - Currently working
- INACTIVE - Temporarily inactive (leave of absence, suspension)
- TERMINATED - No longer with company

**How it works:**
- Click the dropdown next to the status badge in the table
- Select a new status
- Employee status updates immediately
- Toast notification confirms the change

**Backend Endpoint:**
```
PUT /api/employees/{id}/status/{status}
```

**Example:**
```http
PUT http://localhost:8080/api/employees/1/status/INACTIVE
Authorization: Bearer YOUR_TOKEN
```

---

## Updated Stats Cards

The stats section now shows **4 cards** instead of 3:

| Card | Shows |
|------|-------|
| **Total Employees** | All employees (any status) |
| **Active** | Employees with status = ACTIVE |
| **Inactive** | Employees with status = INACTIVE |
| **Terminated** | Employees with status = TERMINATED |

---

## How to Use

### Scenario 1: View Only Active Employees

1. Go to Employees page
2. In the filters section, select **"Active"** from the Status dropdown
3. Table shows only active employees

### Scenario 2: View Engineering Department Employees

1. Go to Employees page
2. In the filters section, select **"Engineering"** from the Department dropdown
3. Table shows only employees in Engineering department

### Scenario 3: Combine Filters

1. Select **"Active"** from Status dropdown
2. Select **"Sales"** from Department dropdown
3. Type "john" in the search box
4. Table shows only active employees in Sales department whose name/email/code contains "john"

### Scenario 4: Change Employee Status

**Option A: Using the table dropdown**
1. Find the employee in the table
2. In the Status column, click the dropdown next to their status badge
3. Select new status (e.g., INACTIVE)
4. Status updates immediately

**Option B: Using the API directly**
```http
PUT http://localhost:8080/api/employees/5/status/TERMINATED
Authorization: Bearer YOUR_TOKEN
```

---

## Filter Logic

All three filters work together:

```javascript
filteredEmployees = employees.filter(employee => {
  // Must match search term (name, email, or code)
  const matchesSearch = /* search logic */;
  
  // Must match selected status (or "ALL")
  const matchesStatus = statusFilter === 'ALL' || employee.status === statusFilter;
  
  // Must match selected department (or "ALL")
  const matchesDepartment = departmentFilter === 'ALL' || 
                            employee.departmentId === departmentFilter;
  
  // Employee must pass ALL filters
  return matchesSearch && matchesStatus && matchesDepartment;
});
```

---

## API Functions Added

### In `hrApi.js`:

```javascript
// Get employees by department
getEmployeesByDepartment: async (departmentId) => {
  const response = await axios.get(`/employees/department/${departmentId}`);
  return response.data.data || response.data;
},

// Get employees by status
getEmployeesByStatus: async (status) => {
  const response = await axios.get(`/employees/status/${status}`);
  return response.data.data || response.data;
},

// Change employee status
changeEmployeeStatus: async (id, status) => {
  const response = await axios.put(`/employees/${id}/status/${status}`);
  return response.data.data || response.data;
},
```

---

## UI Components

### Filter Section

```jsx
<Card padding={false} className="p-4">
  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
    {/* Search */}
    <input type="text" placeholder="Search..." />
    
    {/* Status Filter */}
    <select value={statusFilter} onChange={...}>
      <option value="ALL">All Statuses</option>
      <option value="ACTIVE">Active</option>
      <option value="INACTIVE">Inactive</option>
      <option value="TERMINATED">Terminated</option>
    </select>
    
    {/* Department Filter */}
    <select value={departmentFilter} onChange={...}>
      <option value="ALL">All Departments</option>
      {departments.map(d => <option value={d.id}>{d.name}</option>)}
    </select>
  </div>
</Card>
```

### Status Column with Inline Dropdown

```jsx
{
  key: 'status',
  header: 'Status',
  render: (value, row) => (
    <div className="flex items-center gap-2">
      {/* Status Badge */}
      <Badge variant={value === 'ACTIVE' ? 'success' : 'default'}>
        {value}
      </Badge>
      
      {/* Status Change Dropdown */}
      <select
        value={value}
        onChange={(e) => handleStatusChange(row.id, e.target.value)}
      >
        <option value="ACTIVE">ACTIVE</option>
        <option value="INACTIVE">INACTIVE</option>
        <option value="TERMINATED">TERMINATED</option>
      </select>
    </div>
  ),
}
```

---

## Testing

### Test Filter by Status

1. Open browser: `http://localhost:5173/hr/employees`
2. Select "Active" from Status dropdown
3. Verify only active employees are shown
4. Select "Terminated" from Status dropdown
5. Verify only terminated employees are shown

### Test Filter by Department

1. Select a department from Department dropdown
2. Verify only employees in that department are shown
3. Select "All Departments"
4. Verify all employees are shown again

### Test Status Change

1. Find an active employee
2. Click the status dropdown in their row
3. Select "INACTIVE"
4. Verify:
   - Toast notification appears
   - Status badge updates to "INACTIVE"
   - Stats cards update (Active count decreases, Inactive count increases)

### Test Combined Filters

1. Select "Active" status
2. Select "Engineering" department
3. Type "john" in search
4. Verify only active Engineering employees with "john" in their name/email/code are shown

---

## Benefits

✅ **Better UX** - Users can quickly find employees by status or department  
✅ **Quick Status Changes** - No need to edit entire employee record  
✅ **Visual Stats** - See employee distribution at a glance  
✅ **Flexible Filtering** - Combine multiple filters for precise results  
✅ **Real-time Updates** - Changes reflect immediately with cache invalidation  

---

**All features are now live!** The frontend will hot-reload automatically with Vite.
