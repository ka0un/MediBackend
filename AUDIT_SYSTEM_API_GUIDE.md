# Audit System API Documentation

## Overview

The MediBackend Audit System provides comprehensive audit logging across all service operations. Every significant action is logged with a unique 6-character hash identifier, actor information, timestamp, and detailed metadata.

## Base URL

```
/api/audit
```

## Authentication

All audit endpoints require authentication. Include the appropriate authentication headers with your requests.

---

## Endpoints

### 1. Query Audit Logs

Retrieve audit logs with flexible filtering and pagination support.

**Endpoint:** `GET /api/audit`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | No | Filter by user ID |
| `username` | String | No | Filter by username |
| `action` | String | No | Filter by action type (e.g., "PATIENT_CREATED", "USER_LOGIN") |
| `entityType` | String | No | Filter by entity type (e.g., "Patient", "Appointment", "User") |
| `entityId` | String | No | Filter by entity ID |
| `startDate` | ISO-8601 DateTime | No | Filter logs from this date/time (format: `2024-01-01T00:00:00`) |
| `endDate` | ISO-8601 DateTime | No | Filter logs until this date/time (format: `2024-12-31T23:59:59`) |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 20, max: 100) |
| `sortBy` | String | No | Sort field (default: "timestamp") |
| `sortDirection` | String | No | Sort direction: "ASC" or "DESC" (default: "DESC") |

**Example Request:**

```http
GET /api/audit?userId=123&action=PATIENT_CREATED&page=0&size=20&sortDirection=DESC
```

**Example Response:**

```json
{
  "content": [
    {
      "id": 1001,
      "auditHash": "aB3xY9",
      "action": "PATIENT_CREATED",
      "entityType": "Patient",
      "entityId": "456",
      "userId": 123,
      "username": "admin_user",
      "details": "Patient account created: John Doe (Email: john@example.com, Card: HC123456)",
      "metadata": "{\"email\":\"john@example.com\",\"phone\":\"+1234567890\"}",
      "timestamp": "2024-01-15T14:30:00",
      "ipAddress": "192.168.1.100",
      "correlationId": null
    },
    {
      "id": 1000,
      "auditHash": "x7Km2P",
      "action": "USER_LOGIN",
      "entityType": "User",
      "entityId": "123",
      "userId": 123,
      "username": "admin_user",
      "details": "User logged in: admin_user (Role: ADMIN)",
      "metadata": "{\"userId\":123,\"role\":\"ADMIN\",\"patientId\":null}",
      "timestamp": "2024-01-15T14:25:00",
      "ipAddress": "192.168.1.100",
      "correlationId": null
    }
  ],
  "currentPage": 0,
  "totalItems": 2,
  "totalPages": 1,
  "pageSize": 20,
  "hasNext": false,
  "hasPrevious": false
}
```

---

### 2. Get Audit Log by Hash

Retrieve a specific audit log entry using its unique 6-character hash.

**Endpoint:** `GET /api/audit/{auditHash}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `auditHash` | String | Yes | 6-character unique audit identifier |

**Example Request:**

```http
GET /api/audit/aB3xY9
```

**Example Response:**

```json
{
  "id": 1001,
  "auditHash": "aB3xY9",
  "action": "PATIENT_CREATED",
  "entityType": "Patient",
  "entityId": "456",
  "userId": 123,
  "username": "admin_user",
  "details": "Patient account created: John Doe (Email: john@example.com, Card: HC123456)",
  "metadata": "{\"email\":\"john@example.com\",\"phone\":\"+1234567890\"}",
  "timestamp": "2024-01-15T14:30:00",
  "ipAddress": "192.168.1.100",
  "correlationId": null
}
```

**Error Response (404):**

```json
{
  "error": "Not Found",
  "message": "Audit log not found with hash: aB3xY9"
}
```

---

### 3. Get Audit Logs by Entity

Retrieve all audit logs for a specific entity (e.g., all logs for a particular patient or appointment).

**Endpoint:** `GET /api/audit/entity/{entityType}/{entityId}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `entityType` | String | Yes | Type of entity (e.g., "Patient", "Appointment", "User") |
| `entityId` | String | Yes | Entity identifier |

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 20) |

**Example Request:**

```http
GET /api/audit/entity/Patient/456?page=0&size=10
```

**Example Response:**

```json
{
  "content": [
    {
      "id": 1050,
      "auditHash": "pQ8vN1",
      "action": "PATIENT_PROFILE_UPDATED",
      "entityType": "Patient",
      "entityId": "456",
      "userId": 123,
      "username": "admin_user",
      "details": "Email changed from 'old@example.com' to 'new@example.com'; Phone changed;",
      "metadata": "{\"email\":{\"old\":\"old@example.com\",\"new\":\"new@example.com\"},\"phone\":{\"old\":\"+1111111111\",\"new\":\"+1234567890\"}}",
      "timestamp": "2024-01-16T10:00:00",
      "ipAddress": "192.168.1.100",
      "correlationId": null
    },
    {
      "id": 1001,
      "auditHash": "aB3xY9",
      "action": "PATIENT_CREATED",
      "entityType": "Patient",
      "entityId": "456",
      "userId": 123,
      "username": "admin_user",
      "details": "Patient account created: John Doe (Email: john@example.com, Card: HC123456)",
      "metadata": "{\"email\":\"john@example.com\",\"phone\":\"+1234567890\"}",
      "timestamp": "2024-01-15T14:30:00",
      "ipAddress": "192.168.1.100",
      "correlationId": null
    }
  ],
  "currentPage": 0,
  "totalItems": 2,
  "totalPages": 1
}
```

---

### 4. Get Audit Logs by User

Retrieve all audit logs for a specific user.

**Endpoint:** `GET /api/audit/user/{userId}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | Long | Yes | User identifier |

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 20) |

**Example Request:**

```http
GET /api/audit/user/123?page=0&size=20
```

**Example Response:**

```json
{
  "content": [
    {
      "id": 1002,
      "auditHash": "mN5tR2",
      "action": "APPOINTMENT_BOOKED",
      "entityType": "Appointment",
      "entityId": "789",
      "userId": 123,
      "username": "admin_user",
      "details": "Appointment booked: Patient John Doe with Provider Dr. Smith on 2024-01-20T09:00:00 (Confirmation: APT-ABC123)",
      "metadata": "{\"appointmentId\":789,\"patientId\":456,\"providerId\":12,\"timeSlotId\":34,\"confirmationNumber\":\"APT-ABC123\",\"paymentRequired\":true}",
      "timestamp": "2024-01-15T15:00:00",
      "ipAddress": "192.168.1.100",
      "correlationId": null
    }
  ],
  "currentPage": 0,
  "totalItems": 1,
  "totalPages": 1
}
```

---

## Common Action Types

Here are the most common action types you'll encounter in audit logs:

### Authentication & Authorization
- `USER_LOGIN` - User successfully logged in
- `LOGIN_FAILED` - Failed login attempt
- `USER_REGISTERED` - New user registration
- `REGISTRATION_FAILED` - Failed registration attempt

### Patient Management
- `PATIENT_CREATED` - New patient account created
- `PATIENT_PROFILE_VIEWED` - Patient profile accessed
- `PATIENT_PROFILE_UPDATED` - Patient profile information updated
- `PATIENT_DELETED` - Patient account deleted
- `PATIENTS_LIST_VIEWED` - Patient list accessed

### Appointments
- `APPOINTMENT_BOOKED` - New appointment created
- `APPOINTMENT_CANCELLED` - Appointment cancelled
- `APPOINTMENT_STATUS_UPDATED` - Appointment status changed
- `APPOINTMENT_PAYMENT_PROCESSED` - Payment processed for appointment

### Medical Records
- `MEDICAL_RECORD_VIEWED` - Medical record accessed
- `MEDICAL_RECORD_UPDATED` - Medical record modified
- `PRESCRIPTION_ADDED` - New prescription added
- `MEDICAL_RECORD_DOWNLOADED` - Medical record downloaded as PDF

### Healthcare Providers
- `PROVIDER_CREATED` - New healthcare provider added
- `PROVIDER_UPDATED` - Provider information updated
- `PROVIDER_DELETED` - Provider removed

---

## Entity Types

Common entity types in the system:

- `User` - User accounts
- `Patient` - Patient records
- `Appointment` - Medical appointments
- `Prescription` - Prescriptions
- `HealthcareProvider` - Healthcare providers
- `TimeSlot` - Appointment time slots
- `Payment` - Payment transactions
- `MedicalRecord` - Medical records
- `OtpVerification` - OTP verification records

---

## Pagination

All list endpoints support pagination with the following response structure:

```json
{
  "content": [...],           // Array of audit log entries
  "currentPage": 0,           // Current page number (0-indexed)
  "totalItems": 150,          // Total number of items
  "totalPages": 8,            // Total number of pages
  "pageSize": 20,             // Number of items per page
  "hasNext": true,            // Whether there's a next page
  "hasPrevious": false        // Whether there's a previous page
}
```

---

## Filtering Best Practices

### 1. Filter by Date Range

To get audit logs for a specific time period:

```http
GET /api/audit?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

### 2. Filter by Action and Entity

To track all patient creations:

```http
GET /api/audit?action=PATIENT_CREATED&entityType=Patient
```

### 3. Track User Activity

To see all actions performed by a user:

```http
GET /api/audit/user/123
```

### 4. Monitor Specific Entity

To view complete audit trail for a patient:

```http
GET /api/audit/entity/Patient/456
```

### 5. Combine Multiple Filters

To find specific events:

```http
GET /api/audit?action=PATIENT_PROFILE_UPDATED&startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&page=0&size=50
```

---

## Metadata Field

The `metadata` field contains structured JSON data with additional context about the action:

**Example for PATIENT_PROFILE_UPDATED:**
```json
{
  "email": {
    "old": "old@example.com",
    "new": "new@example.com"
  },
  "phone": {
    "old": "+1111111111",
    "new": "+1234567890"
  }
}
```

**Example for APPOINTMENT_BOOKED:**
```json
{
  "appointmentId": 789,
  "patientId": 456,
  "providerId": 12,
  "timeSlotId": 34,
  "confirmationNumber": "APT-ABC123",
  "paymentRequired": true
}
```

**Example for USER_LOGIN:**
```json
{
  "userId": 123,
  "role": "ADMIN",
  "patientId": null
}
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid date format. Use ISO-8601 format: 2024-01-01T00:00:00"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions to access audit logs"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Audit log not found with hash: aB3xY9"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

- Default rate limit: 100 requests per minute per user
- Exceeding the limit returns HTTP 429 (Too Many Requests)

---

## Data Retention

- Audit logs are retained for 7 years by default
- Logs older than 7 years are automatically archived
- Archived logs can be retrieved through the archive API (contact system administrator)

---

## Security Considerations

1. **Access Control**: Only authorized users can access audit logs
2. **PII Protection**: Sensitive data is redacted in audit logs
3. **Immutability**: Audit logs cannot be modified or deleted
4. **Encryption**: All audit data is encrypted at rest and in transit

---

## Sample Integration Code

### JavaScript/TypeScript (React)

```javascript
async function fetchAuditLogs(filters) {
  const params = new URLSearchParams({
    page: filters.page || 0,
    size: filters.size || 20,
    sortDirection: 'DESC'
  });

  if (filters.userId) params.append('userId', filters.userId);
  if (filters.action) params.append('action', filters.action);
  if (filters.startDate) params.append('startDate', filters.startDate);
  if (filters.endDate) params.append('endDate', filters.endDate);

  const response = await fetch(`/api/audit?${params}`, {
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    throw new Error('Failed to fetch audit logs');
  }

  return await response.json();
}

// Usage
const auditLogs = await fetchAuditLogs({
  userId: 123,
  action: 'PATIENT_CREATED',
  startDate: '2024-01-01T00:00:00',
  endDate: '2024-01-31T23:59:59',
  page: 0,
  size: 20
});

console.log('Total logs:', auditLogs.totalItems);
console.log('Logs:', auditLogs.content);
```

### Python

```python
import requests

def fetch_audit_logs(base_url, token, filters):
    params = {
        'page': filters.get('page', 0),
        'size': filters.get('size', 20),
        'sortDirection': 'DESC'
    }
    
    if filters.get('userId'):
        params['userId'] = filters['userId']
    if filters.get('action'):
        params['action'] = filters['action']
    if filters.get('startDate'):
        params['startDate'] = filters['startDate']
    if filters.get('endDate'):
        params['endDate'] = filters['endDate']
    
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }
    
    response = requests.get(f'{base_url}/api/audit', params=params, headers=headers)
    response.raise_for_status()
    
    return response.json()

# Usage
audit_data = fetch_audit_logs(
    base_url='https://api.example.com',
    token='your_access_token',
    filters={
        'userId': 123,
        'action': 'PATIENT_CREATED',
        'startDate': '2024-01-01T00:00:00',
        'endDate': '2024-01-31T23:59:59'
    }
)

print(f"Total logs: {audit_data['totalItems']}")
for log in audit_data['content']:
    print(f"{log['auditHash']}: {log['action']} - {log['details']}")
```

---

## Support

For questions or issues with the Audit API, please contact:
- Technical Support: support@medibackend.com
- API Documentation: https://docs.medibackend.com
