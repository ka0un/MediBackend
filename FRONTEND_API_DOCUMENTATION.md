# MediBackend - Frontend API Documentation

**Version**: 1.0  
**Base URL**: `http://localhost:8080`  
**Last Updated**: October 16, 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Authentication & Security](#authentication--security)
4. [Common Patterns](#common-patterns)
5. [Error Handling](#error-handling)
6. [API Endpoints](#api-endpoints)
   - [UC-01: Appointment Management](#uc-01-appointment-management)
   - [UC-02: Patient Account Management](#uc-02-patient-account-management)
   - [UC-03: Statistical Reports](#uc-03-statistical-reports)
   - [UC-04: Medical Records Access](#uc-04-medical-records-access)
7. [Data Models](#data-models)
8. [Enumerations](#enumerations)
9. [Validation Rules](#validation-rules)
10. [CORS & Headers](#cors--headers)
11. [Example Workflows](#example-workflows)

---

## Overview

The MediBackend system is a comprehensive healthcare management API that provides:

- **Appointment Booking**: Browse providers, view availability, and book appointments
- **Patient Management**: Complete CRUD operations for patient accounts
- **Statistical Reporting**: Generate KPI reports with PDF/CSV export
- **Medical Records**: Access comprehensive patient medical records with audit trail

### System Architecture

```
Frontend Application
        ↓
  REST API (Spring Boot)
        ↓
   H2 Database (In-Memory)
```

### Key Features

- ✅ **No Authentication Required**: All endpoints are publicly accessible
- ✅ **CORS Enabled**: Works with any frontend origin
- ✅ **Real-time Data**: Concurrent booking protection prevents double-booking
- ✅ **Comprehensive Audit Trail**: All actions logged for compliance
- ✅ **Export Capabilities**: PDF and CSV report generation

---

## Getting Started

### Base URL

```
http://localhost:8080
```

### Content Type

All requests and responses use JSON format:

```
Content-Type: application/json
```

### Making Your First Request

```bash
# Get all healthcare providers
curl http://localhost:8080/api/appointments/providers
```

---

## Authentication & Security

### Authentication

**No authentication is required** for any endpoint. All endpoints are publicly accessible.

### Security Configuration

- **CSRF Protection**: Disabled
- **Authorization**: All requests permitted
- **Session Management**: Stateless

⚠️ **Note**: This configuration is suitable for development/demo purposes. Production systems should implement proper authentication and authorization.

---

## Common Patterns

### Successful Response

HTTP Status: `200 OK` or `201 Created`

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Error Response

HTTP Status: `500 Internal Server Error` (for all errors)

```json
{
  "timestamp": "2025-10-16T15:30:00",
  "message": "Error description here",
  "status": 500
}
```

### Pagination

Currently, the API does not implement pagination. All list endpoints return complete results.

### Date/Time Formats

- **Date**: ISO 8601 format `YYYY-MM-DD` (e.g., `2025-10-16`)
- **DateTime**: ISO 8601 format `YYYY-MM-DDTHH:mm:ss` (e.g., `2025-10-16T14:30:00`)

---

## Error Handling

### Common Error Scenarios

| Scenario | HTTP Status | Message Example |
|----------|-------------|-----------------|
| Resource Not Found | 500 | "Patient not found" |
| Validation Failed | 500 | "Email already exists" |
| Business Logic Error | 500 | "Time slot is no longer available" |
| Concurrent Booking | 500 | "Time slot is no longer available" |
| Invalid Card Number | 500 | "Patient records not found for card number: XXX" |
| Payment Required | 200 | Status field will be "PENDING_PAYMENT" |

### Error Response Structure

All errors follow this format:

```json
{
  "timestamp": "2025-10-16T15:30:00.123456",
  "message": "Detailed error message",
  "status": 500
}
```

### Handling Errors in Frontend

```javascript
try {
  const response = await fetch('http://localhost:8080/api/patients/1');
  const data = await response.json();
  
  if (!response.ok) {
    console.error('Error:', data.message);
    // Handle error
  } else {
    // Process data
  }
} catch (error) {
  console.error('Network error:', error);
}
```

---

## API Endpoints

## UC-01: Appointment Management

### 1.1 Get Healthcare Providers

Retrieve all healthcare providers or filter by medical specialty.

**Endpoint**: `GET /api/appointments/providers`

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| specialty | string | No | Filter by medical specialty (e.g., "Cardiology", "Dermatology") |

**Request Example**:

```bash
# Get all providers
GET http://localhost:8080/api/appointments/providers

# Filter by specialty
GET http://localhost:8080/api/appointments/providers?specialty=Cardiology
```

**Response**: `200 OK`

```json
[
  {
    "id": 1,
    "name": "Dr. Sarah Williams",
    "specialty": "Cardiology",
    "hospitalName": "City Government Hospital",
    "hospitalType": "GOVERNMENT"
  },
  {
    "id": 2,
    "name": "Dr. Michael Brown",
    "specialty": "Cardiology",
    "hospitalName": "Central Medical Center",
    "hospitalType": "PRIVATE"
  }
]
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique provider identifier |
| name | String | Provider's full name |
| specialty | String | Medical specialty |
| hospitalName | String | Hospital/clinic name |
| hospitalType | Enum | GOVERNMENT or PRIVATE |

**Error Scenarios**:

- No errors specific to this endpoint - returns empty array if no providers found

---

### 1.2 Get Available Time Slots

Get available appointment slots for a specific provider on a given date.

**Endpoint**: `GET /api/appointments/timeslots`

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| providerId | Long | Yes | ID of the healthcare provider |
| date | DateTime | Yes | Date in ISO 8601 format (YYYY-MM-DDTHH:mm:ss) |

**Request Example**:

```bash
GET http://localhost:8080/api/appointments/timeslots?providerId=1&date=2025-10-20T00:00:00
```

**Response**: `200 OK`

```json
[
  {
    "id": 1,
    "providerId": 1,
    "providerName": "Dr. Sarah Williams",
    "startTime": "2025-10-20T09:00:00",
    "endTime": "2025-10-20T10:00:00",
    "available": true
  },
  {
    "id": 2,
    "providerId": 1,
    "providerName": "Dr. Sarah Williams",
    "startTime": "2025-10-20T10:00:00",
    "endTime": "2025-10-20T11:00:00",
    "available": false
  }
]
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Time slot identifier |
| providerId | Long | Provider ID |
| providerName | String | Provider's name |
| startTime | DateTime | Slot start time |
| endTime | DateTime | Slot end time |
| available | Boolean | Whether slot can be booked |

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Invalid provider | "Provider not found" |
| Missing parameters | Application error |

---

### 1.3 Book Appointment

Book an appointment with a healthcare provider.

**Endpoint**: `POST /api/appointments/book`

**Request Body**:

```json
{
  "patientId": 1,
  "providerId": 1,
  "timeSlotId": 1
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| patientId | Long | Yes | Patient's ID |
| providerId | Long | Yes | Healthcare provider's ID |
| timeSlotId | Long | Yes | Desired time slot ID |

**Response**: `201 Created`

**Government Hospital (Free - No Payment Required)**:

```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "John Doe",
  "providerId": 1,
  "providerName": "Dr. Sarah Williams",
  "specialty": "Cardiology",
  "appointmentTime": "2025-10-20T09:00:00",
  "confirmationNumber": "APT-ABC12345",
  "status": "CONFIRMED",
  "paymentRequired": false,
  "hospitalName": "City Government Hospital"
}
```

**Private Hospital (Payment Required)**:

```json
{
  "id": 2,
  "patientId": 1,
  "patientName": "John Doe",
  "providerId": 3,
  "providerName": "Dr. Emily Davis",
  "specialty": "Dermatology",
  "appointmentTime": "2025-10-20T10:00:00",
  "confirmationNumber": "APT-XYZ67890",
  "status": "PENDING_PAYMENT",
  "paymentRequired": true,
  "hospitalName": "Advanced Private Clinic"
}
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Appointment ID |
| patientId | Long | Patient ID |
| patientName | String | Patient's name |
| providerId | Long | Provider ID |
| providerName | String | Provider's name |
| specialty | String | Medical specialty |
| appointmentTime | DateTime | Appointment date/time |
| confirmationNumber | String | Unique confirmation code (APT-XXXXXXXX) |
| status | Enum | CONFIRMED, PENDING_PAYMENT, CANCELLED, COMPLETED |
| paymentRequired | Boolean | Whether payment is needed |
| hospitalName | String | Hospital name |

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |
| Provider not found | "Provider not found" |
| Time slot unavailable | "Time slot is no longer available" |
| Concurrent booking | "Time slot is no longer available" |

**Important Notes**:

- Time slots are locked during booking to prevent double-booking
- Government hospital appointments are automatically CONFIRMED
- Private hospital appointments require payment (status: PENDING_PAYMENT)

---

### 1.4 Process Payment

Process payment for a private hospital appointment.

**Endpoint**: `POST /api/appointments/payment`

**Request Body**:

```json
{
  "appointmentId": 2,
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "1234567890123456",
  "cvv": "123"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| appointmentId | Long | Yes | Appointment ID to pay for |
| amount | BigDecimal | Yes | Payment amount |
| paymentMethod | Enum | Yes | CREDIT_CARD, DEBIT_CARD, ONLINE_BANKING, WALLET |
| cardNumber | String | Yes | Card number (for card payments) |
| cvv | String | Yes | CVV code (for card payments) |

**Response**: `200 OK`

```json
{
  "id": 2,
  "patientId": 1,
  "patientName": "John Doe",
  "providerId": 3,
  "providerName": "Dr. Emily Davis",
  "specialty": "Dermatology",
  "appointmentTime": "2025-10-20T10:00:00",
  "confirmationNumber": "APT-XYZ67890",
  "status": "CONFIRMED",
  "paymentRequired": true,
  "hospitalName": "Advanced Private Clinic"
}
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Appointment not found | "Appointment not found" |
| Payment not required | "Payment not required for this appointment" |
| Already paid | "Payment already processed" |

**Important Notes**:

- After successful payment, appointment status changes to CONFIRMED
- Payment generates a unique transaction ID
- Card details are NOT stored (demo system)

---

### 1.5 Get Appointment by Confirmation Number

Retrieve appointment details using the confirmation number.

**Endpoint**: `GET /api/appointments/confirmation/{confirmationNumber}`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| confirmationNumber | String | Confirmation code (e.g., APT-ABC12345) |

**Request Example**:

```bash
GET http://localhost:8080/api/appointments/confirmation/APT-ABC12345
```

**Response**: `200 OK`

```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "John Doe",
  "providerId": 1,
  "providerName": "Dr. Sarah Williams",
  "specialty": "Cardiology",
  "appointmentTime": "2025-10-20T09:00:00",
  "confirmationNumber": "APT-ABC12345",
  "status": "CONFIRMED",
  "paymentRequired": false,
  "hospitalName": "City Government Hospital"
}
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Invalid confirmation | "Appointment not found with confirmation number: XXX" |

---

### 1.6 Get Patient Appointments

Get all appointments for a specific patient.

**Endpoint**: `GET /api/appointments/patient/{patientId}`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID |

**Request Example**:

```bash
GET http://localhost:8080/api/appointments/patient/1
```

**Response**: `200 OK`

```json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "John Doe",
    "providerId": 1,
    "providerName": "Dr. Sarah Williams",
    "specialty": "Cardiology",
    "appointmentTime": "2025-10-20T09:00:00",
    "confirmationNumber": "APT-ABC12345",
    "status": "CONFIRMED",
    "paymentRequired": false,
    "hospitalName": "City Government Hospital"
  },
  {
    "id": 2,
    "patientId": 1,
    "patientName": "John Doe",
    "providerId": 3,
    "providerName": "Dr. Emily Davis",
    "specialty": "Dermatology",
    "appointmentTime": "2025-10-22T14:00:00",
    "confirmationNumber": "APT-XYZ67890",
    "status": "PENDING_PAYMENT",
    "paymentRequired": true,
    "hospitalName": "Advanced Private Clinic"
  }
]
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |

---

## UC-02: Patient Account Management

### 2.1 Create Patient Account

Create a new patient account in the system.

**Endpoint**: `POST /api/patients`

**Request Body**:

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "digitalHealthCardNumber": "DHC-2025-001",
  "address": "123 Main Street, City, State 12345",
  "dateOfBirth": "1990-05-15",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1234567899",
  "medicalHistory": "No significant medical history",
  "bloodType": "O+",
  "allergies": "Penicillin"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Patient's full name |
| email | String | Yes | Email address (must be unique) |
| phone | String | Yes | Phone number |
| digitalHealthCardNumber | String | Yes | Unique health card number (must be unique) |
| address | String | No | Home address |
| dateOfBirth | Date | No | Date of birth (YYYY-MM-DD) |
| emergencyContactName | String | No | Emergency contact name |
| emergencyContactPhone | String | No | Emergency contact phone |
| medicalHistory | String | No | Medical history (max 2000 chars) |
| bloodType | String | No | Blood type (e.g., "O+", "A-") |
| allergies | String | No | Known allergies |

**Response**: `201 Created`

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "digitalHealthCardNumber": "DHC-2025-001",
  "address": "123 Main Street, City, State 12345",
  "dateOfBirth": "1990-05-15",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1234567899",
  "medicalHistory": "No significant medical history",
  "bloodType": "O+",
  "allergies": "Penicillin"
}
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Duplicate email | "Email already exists" |
| Duplicate health card | "Digital health card number already exists" |
| Missing required field | "Field X is required" |

**Important Notes**:

- Email and digitalHealthCardNumber must be unique
- Account creation is logged in the audit trail
- All optional fields can be updated later

---

### 2.2 Get Patient Profile

Retrieve complete patient profile information.

**Endpoint**: `GET /api/patients/{patientId}`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID |

**Request Example**:

```bash
GET http://localhost:8080/api/patients/1
```

**Response**: `200 OK`

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "digitalHealthCardNumber": "DHC-2025-001",
  "address": "123 Main Street, City, State 12345",
  "dateOfBirth": "1990-05-15",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1234567899",
  "medicalHistory": "No significant medical history",
  "bloodType": "O+",
  "allergies": "Penicillin"
}
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |

---

### 2.3 Get All Patients

Retrieve all patient profiles in the system.

**Endpoint**: `GET /api/patients`

**Request Example**:

```bash
GET http://localhost:8080/api/patients
```

**Response**: `200 OK`

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "digitalHealthCardNumber": "DHC-2025-001",
    "address": "123 Main Street, City, State 12345",
    "dateOfBirth": "1990-05-15",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+1234567899",
    "medicalHistory": "No significant medical history",
    "bloodType": "O+",
    "allergies": "Penicillin"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "+1987654321",
    "digitalHealthCardNumber": "DHC-2025-002",
    "address": "456 Oak Avenue, Town, State 67890",
    "dateOfBirth": "1985-08-22",
    "emergencyContactName": "John Smith",
    "emergencyContactPhone": "+1987654320",
    "medicalHistory": "Asthma",
    "bloodType": "A+",
    "allergies": "None"
  }
]
```

---

### 2.4 Update Patient Profile

Update patient account information. Only provided fields will be updated.

**Endpoint**: `PUT /api/patients/{patientId}`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID |

**Request Body** (all fields optional):

```json
{
  "name": "John Updated Doe",
  "email": "john.updated@example.com",
  "phone": "+1999999999",
  "address": "789 New Street, City, State 11111",
  "dateOfBirth": "1990-05-15",
  "emergencyContactName": "Jane Updated Doe",
  "emergencyContactPhone": "+1888888888",
  "medicalHistory": "Updated medical history: Hypertension diagnosed 2024",
  "bloodType": "O+",
  "allergies": "Penicillin, Latex"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | No | Updated full name |
| email | String | No | Updated email (must be unique) |
| phone | String | No | Updated phone number |
| address | String | No | Updated address |
| dateOfBirth | Date | No | Updated date of birth |
| emergencyContactName | String | No | Updated emergency contact |
| emergencyContactPhone | String | No | Updated emergency phone |
| medicalHistory | String | No | Updated medical history |
| bloodType | String | No | Updated blood type |
| allergies | String | No | Updated allergies |

**Response**: `200 OK`

```json
{
  "id": 1,
  "name": "John Updated Doe",
  "email": "john.updated@example.com",
  "phone": "+1999999999",
  "digitalHealthCardNumber": "DHC-2025-001",
  "address": "789 New Street, City, State 11111",
  "dateOfBirth": "1990-05-15",
  "emergencyContactName": "Jane Updated Doe",
  "emergencyContactPhone": "+1888888888",
  "medicalHistory": "Updated medical history: Hypertension diagnosed 2024",
  "bloodType": "O+",
  "allergies": "Penicillin, Latex"
}
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |
| Duplicate email | "Email already exists" |

**Important Notes**:

- digitalHealthCardNumber CANNOT be updated
- All changes are logged in the audit trail
- Only provided fields are updated; omitted fields remain unchanged

---

### 2.5 Delete Patient Account

Delete a patient account from the system.

**Endpoint**: `DELETE /api/patients/{patientId}`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID to delete |

**Request Example**:

```bash
DELETE http://localhost:8080/api/patients/1
```

**Response**: `204 No Content`

No response body.

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |

**Important Notes**:

- Deletion is logged in the audit trail
- All related records (medications, prescriptions, etc.) are also deleted
- Action cannot be undone

---

## UC-03: Statistical Reports

### 3.1 Generate Statistical Report

Generate a comprehensive statistical report with KPIs, daily visits, and department breakdowns.

**Endpoint**: `GET /api/reports`

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| hospital | String | No | Filter by hospital name |
| department | String | No | Filter by department/specialty |
| startDate | Date | No | Start date (YYYY-MM-DD). Default: 30 days ago |
| endDate | Date | No | End date (YYYY-MM-DD). Default: today |
| reportType | String | No | Report type (currently unused) |
| granularity | String | No | Data granularity (currently unused) |

**Request Examples**:

```bash
# Default report (last 30 days)
GET http://localhost:8080/api/reports

# Filter by hospital
GET http://localhost:8080/api/reports?hospital=City%20Hospital

# Filter by department
GET http://localhost:8080/api/reports?department=Cardiology

# Filter by date range
GET http://localhost:8080/api/reports?startDate=2025-10-01&endDate=2025-10-16

# Multiple filters
GET http://localhost:8080/api/reports?hospital=City%20Hospital&department=Cardiology&startDate=2025-10-01&endDate=2025-10-16
```

**Response**: `200 OK`

```json
{
  "kpis": {
    "totalVisits": 150,
    "confirmedAppointments": 120,
    "pendingPayments": 15,
    "cancelledAppointments": 15,
    "totalRevenue": 15000.00,
    "averageWaitTime": 48.5,
    "appointmentCompletionRate": 80.0
  },
  "dailyVisits": [
    {
      "date": "2025-10-01",
      "visitCount": 10,
      "confirmedCount": 8,
      "cancelledCount": 2
    },
    {
      "date": "2025-10-02",
      "visitCount": 12,
      "confirmedCount": 10,
      "cancelledCount": 2
    }
  ],
  "departmentBreakdowns": [
    {
      "department": "Cardiology",
      "totalAppointments": 50,
      "confirmedAppointments": 42,
      "revenue": 5000.00,
      "completionRate": 84.0
    },
    {
      "department": "Dermatology",
      "totalAppointments": 40,
      "confirmedAppointments": 32,
      "revenue": 4000.00,
      "completionRate": 80.0
    }
  ],
  "filters": {
    "hospital": "City Hospital",
    "department": "Cardiology",
    "startDate": "2025-10-01",
    "endDate": "2025-10-16",
    "reportType": null,
    "granularity": null
  },
  "generatedAt": "2025-10-16T15:30:00"
}
```

**Response Fields**:

**KPIs Object**:

| Field | Type | Description |
|-------|------|-------------|
| totalVisits | Long | Total appointments in date range |
| confirmedAppointments | Long | Number of confirmed appointments |
| pendingPayments | Long | Appointments awaiting payment |
| cancelledAppointments | Long | Number of cancelled appointments |
| totalRevenue | BigDecimal | Total revenue from completed payments |
| averageWaitTime | BigDecimal | Average hours between booking and appointment |
| appointmentCompletionRate | Double | Percentage of confirmed appointments |

**Daily Visit Object**:

| Field | Type | Description |
|-------|------|-------------|
| date | String | Date (YYYY-MM-DD) |
| visitCount | Integer | Total visits on this date |
| confirmedCount | Integer | Confirmed visits |
| cancelledCount | Integer | Cancelled visits |

**Department Breakdown Object**:

| Field | Type | Description |
|-------|------|-------------|
| department | String | Department/specialty name |
| totalAppointments | Long | Total appointments |
| confirmedAppointments | Long | Confirmed appointments |
| revenue | BigDecimal | Revenue generated |
| completionRate | Double | Completion rate percentage |

**No Data Response**: `200 OK`

```json
{
  "kpis": {
    "totalVisits": 0,
    "confirmedAppointments": 0,
    "pendingPayments": 0,
    "cancelledAppointments": 0,
    "totalRevenue": 0,
    "averageWaitTime": 0,
    "appointmentCompletionRate": 0.0
  },
  "dailyVisits": [],
  "departmentBreakdowns": [],
  "filters": {
    "hospital": null,
    "department": null,
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "reportType": null,
    "granularity": null
  },
  "generatedAt": "2025-10-16T15:30:00",
  "message": "No data available for the selected filters and date range"
}
```

---

### 3.2 Export Report

Export a statistical report in PDF or CSV format.

**Endpoint**: `POST /api/reports/export`

**Request Body**:

```json
{
  "format": "PDF",
  "filters": {
    "hospital": "City Hospital",
    "department": "Cardiology",
    "startDate": "2025-10-01",
    "endDate": "2025-10-16",
    "reportType": null,
    "granularity": null
  }
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| format | String | No | "PDF" or "CSV". Default: "PDF" |
| filters | Object | No | Same filters as generate report |

**Response (PDF)**: `200 OK`

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="report_1729092000000.pdf"

[Binary PDF data]
```

**Response (CSV)**: `200 OK`

```
Content-Type: text/csv
Content-Disposition: attachment; filename="report_1729092000000.csv"

Healthcare Statistical Report
Generated: 2025-10-16T15:30:00

Key Performance Indicators
Total Visits,150
Confirmed Appointments,120
Pending Payments,15
Cancelled Appointments,15
Total Revenue,15000.00
Average Wait Time (hours),48.5
Completion Rate (%),80.0

Department Breakdown
Department,Total Appointments,Confirmed Appointments,Revenue,Completion Rate (%)
Cardiology,50,42,5000.00,84.0
Dermatology,40,32,4000.00,80.0

Daily Visits
Date,Total Visits,Confirmed,Cancelled
2025-10-01,10,8,2
2025-10-02,12,10,2
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Invalid format | "Unsupported format. Use PDF or CSV." |

**Important Notes**:

- Export is logged in the database
- PDF includes formatted tables and headers
- CSV is suitable for Excel/spreadsheet import
- Filename includes timestamp for uniqueness

---

## UC-04: Medical Records Access

### 4.1 Scan Digital Health Card

Access patient medical records by scanning their digital health card.

**Endpoint**: `POST /api/medical-records/scan-card`

**Request Body**:

```json
{
  "cardNumber": "DHC-2025-001",
  "staffId": "DR-SMITH-001",
  "purpose": "General consultation"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| cardNumber | String | Yes | Digital health card number |
| staffId | String | Yes | ID of staff accessing records |
| purpose | String | Yes | Reason for accessing records |

**Response**: `200 OK`

```json
{
  "patientId": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "digitalHealthCardNumber": "DHC-2025-001",
  "address": "123 Main Street, City",
  "dateOfBirth": "1990-05-15",
  "bloodType": "O+",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1234567899",
  "medicalHistory": "Hypertension diagnosed in 2018",
  "allergies": "Penicillin",
  "currentMedications": [
    {
      "id": 1,
      "medicationName": "Lisinopril",
      "dosage": "10mg",
      "frequency": "Once daily",
      "startDate": "2024-04-16",
      "endDate": null,
      "prescribedBy": "Dr. Sarah Williams",
      "notes": "Monitor blood pressure weekly",
      "active": true
    }
  ],
  "previousVisits": [
    {
      "appointmentId": 1,
      "visitDate": "2025-10-10T14:00:00",
      "providerName": "Dr. Sarah Williams",
      "specialty": "Cardiology",
      "hospitalName": "City Government Hospital",
      "status": "CONFIRMED"
    }
  ],
  "prescriptions": [
    {
      "id": 1,
      "prescribedBy": "Dr. Sarah Williams",
      "prescriptionDate": "2025-10-06T10:00:00",
      "diagnosis": "Hypertension management",
      "treatment": "Continue current medication, monitor blood pressure",
      "notes": "Patient responding well to treatment",
      "medications": "Lisinopril 10mg once daily",
      "followUpDate": "2026-01-06T10:00:00"
    }
  ],
  "testResults": [
    {
      "id": 1,
      "testName": "Blood Pressure",
      "testDate": "2025-10-09",
      "result": "120/80",
      "resultUnit": "mmHg",
      "referenceRange": "< 120/80",
      "orderedBy": "Dr. Sarah Williams",
      "performedBy": "Nurse Johnson",
      "notes": "Normal blood pressure reading"
    }
  ],
  "vaccinations": [
    {
      "id": 1,
      "vaccineName": "Influenza",
      "vaccinationDate": "2024-10-16",
      "batchNumber": "FLU2024-001",
      "manufacturer": "Pfizer",
      "administeredBy": "Nurse Williams",
      "nextDoseDate": "2025-10-16",
      "notes": "Annual flu vaccine"
    }
  ],
  "accessedAt": "2025-10-16T15:30:00",
  "accessedBy": "DR-SMITH-001"
}
```

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Invalid card number | "Patient records not found for card number: XXX" |
| Missing card number | Application error |

**Important Notes**:

- Access is logged in the audit trail
- Returns complete medical history
- Critical for emergency situations

---

### 4.2 Get Medical Records by Patient ID

Access patient medical records directly using patient ID.

**Endpoint**: `GET /api/medical-records/{patientId}`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID |

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| staffId | String | No | "STAFF_DEFAULT" | Staff member ID |
| purpose | String | No | "General consultation" | Access purpose |

**Request Example**:

```bash
GET http://localhost:8080/api/medical-records/1?staffId=DR-JONES-002&purpose=Follow-up%20visit
```

**Response**: `200 OK`

Same as Scan Card response (see section 4.1).

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |

---

### 4.3 Add Prescription / Treatment Notes

Add new prescription or treatment notes to a patient's medical record.

**Endpoint**: `POST /api/medical-records/prescriptions`

**Request Body**:

```json
{
  "patientId": 1,
  "staffId": "Dr. Johnson",
  "diagnosis": "Seasonal Allergies",
  "treatment": "Antihistamines and rest",
  "notes": "Review in 2 weeks",
  "medications": "Cetirizine 10mg once daily",
  "followUpDate": "2025-10-30T14:00:00"
}
```

**Request Fields**:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| patientId | Long | Yes | Patient's ID |
| staffId | String | Yes | Doctor/staff adding prescription |
| diagnosis | String | Yes | Medical diagnosis (max 2000 chars) |
| treatment | String | No | Treatment plan (max 2000 chars) |
| notes | String | No | Additional notes (max 2000 chars) |
| medications | String | No | Prescribed medications (max 2000 chars) |
| followUpDate | DateTime | No | Next appointment date/time |

**Response**: `201 Created`

Returns complete medical record response with the new prescription included (same format as section 4.1).

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |
| Missing required field | Application error |

**Important Notes**:

- Action is logged as "UPDATE" in access logs
- Prescription is immediately visible in medical records
- Follow-up date is optional but recommended

---

### 4.4 Download Medical Records as PDF

Download complete medical records as a formatted PDF document.

**Endpoint**: `GET /api/medical-records/{patientId}/download`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID |

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| staffId | String | No | "STAFF_DEFAULT" | Staff member downloading |
| purpose | String | No | "Patient copy" | Download purpose |

**Request Example**:

```bash
GET http://localhost:8080/api/medical-records/1/download?staffId=DR-SMITH-001&purpose=Referral
```

**Response**: `200 OK`

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="medical_record_1_20251016_153000.pdf"
Content-Length: [size]

[Binary PDF data]
```

**PDF Contents**:

- Patient demographics
- Emergency contacts
- Medical history and allergies
- Current medications
- Prescriptions and treatments
- Test results
- Vaccination records
- Previous visit history

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |

**Important Notes**:

- Download is logged as "DOWNLOAD" in access logs
- Filename format: `medical_record_{patientId}_{timestamp}.pdf`
- Suitable for printing or email attachment

---

### 4.5 Get Access Logs

Retrieve audit trail of all access attempts for a patient's medical records.

**Endpoint**: `GET /api/medical-records/{patientId}/access-logs`

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| patientId | Long | Patient's ID |

**Request Example**:

```bash
GET http://localhost:8080/api/medical-records/1/access-logs
```

**Response**: `200 OK`

```json
[
  {
    "id": 3,
    "patientId": 1,
    "staffId": "DR-SMITH-001",
    "accessType": "DOWNLOAD",
    "accessTimestamp": "2025-10-16T15:35:00",
    "purpose": "Patient copy",
    "accessGranted": true,
    "denialReason": null
  },
  {
    "id": 2,
    "patientId": 1,
    "staffId": "Dr. Johnson",
    "accessType": "UPDATE",
    "accessTimestamp": "2025-10-16T15:30:00",
    "purpose": "Added prescription - Diagnosis: Seasonal Allergies",
    "accessGranted": true,
    "denialReason": null
  },
  {
    "id": 1,
    "patientId": 1,
    "staffId": "DR-SMITH-001",
    "accessType": "VIEW",
    "accessTimestamp": "2025-10-16T15:25:00",
    "purpose": "General consultation",
    "accessGranted": true,
    "denialReason": null
  }
]
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Log entry ID |
| patientId | Long | Patient ID |
| staffId | String | Staff member who accessed records |
| accessType | Enum | VIEW, UPDATE, or DOWNLOAD |
| accessTimestamp | DateTime | When access occurred |
| purpose | String | Reason for access |
| accessGranted | Boolean | Whether access was granted |
| denialReason | String | Reason if access was denied |

**Error Scenarios**:

| Error | Message |
|-------|---------|
| Patient not found | "Patient not found" |

**Important Notes**:

- All access attempts are logged
- Supports HIPAA/GDPR compliance auditing
- Logs are immutable (cannot be deleted or modified)

---

## Data Models

### Patient

Complete patient information including demographics and medical data.

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "digitalHealthCardNumber": "DHC-2025-001",
  "address": "123 Main Street",
  "dateOfBirth": "1990-05-15",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1234567899",
  "medicalHistory": "Hypertension",
  "bloodType": "O+",
  "allergies": "Penicillin"
}
```

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Auto-generated | Unique identifier |
| name | String | Required | Full name |
| email | String | Required, Unique | Email address |
| phone | String | Required | Phone number |
| digitalHealthCardNumber | String | Required, Unique | Health card number |
| address | String | Optional | Home address |
| dateOfBirth | Date | Optional | Date of birth |
| emergencyContactName | String | Optional | Emergency contact |
| emergencyContactPhone | String | Optional | Emergency phone |
| medicalHistory | String | Optional, Max 2000 | Medical history |
| bloodType | String | Optional | Blood type |
| allergies | String | Optional | Known allergies |

---

### Healthcare Provider

Medical professionals and their affiliated hospitals.

```json
{
  "id": 1,
  "name": "Dr. Sarah Williams",
  "specialty": "Cardiology",
  "hospitalName": "City Government Hospital",
  "hospitalType": "GOVERNMENT"
}
```

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique identifier |
| name | String | Provider's name |
| specialty | String | Medical specialty |
| hospitalName | String | Hospital/clinic name |
| hospitalType | Enum | GOVERNMENT or PRIVATE |

---

### Time Slot

Available appointment time slots.

```json
{
  "id": 1,
  "providerId": 1,
  "providerName": "Dr. Sarah Williams",
  "startTime": "2025-10-20T09:00:00",
  "endTime": "2025-10-20T10:00:00",
  "available": true
}
```

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique identifier |
| providerId | Long | Associated provider |
| providerName | String | Provider's name |
| startTime | DateTime | Slot start time |
| endTime | DateTime | Slot end time |
| available | Boolean | Booking availability |

---

### Appointment

Booked appointments with status tracking.

```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "John Doe",
  "providerId": 1,
  "providerName": "Dr. Sarah Williams",
  "specialty": "Cardiology",
  "appointmentTime": "2025-10-20T09:00:00",
  "confirmationNumber": "APT-ABC12345",
  "status": "CONFIRMED",
  "paymentRequired": false,
  "hospitalName": "City Government Hospital"
}
```

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique identifier |
| patientId | Long | Patient reference |
| patientName | String | Patient's name |
| providerId | Long | Provider reference |
| providerName | String | Provider's name |
| specialty | String | Medical specialty |
| appointmentTime | DateTime | Appointment date/time |
| confirmationNumber | String | Unique confirmation code |
| status | Enum | Appointment status |
| paymentRequired | Boolean | Payment requirement |
| hospitalName | String | Hospital name |

---

### Medication

Current and historical medications.

```json
{
  "id": 1,
  "medicationName": "Lisinopril",
  "dosage": "10mg",
  "frequency": "Once daily",
  "startDate": "2024-04-16",
  "endDate": null,
  "prescribedBy": "Dr. Sarah Williams",
  "notes": "Monitor blood pressure",
  "active": true
}
```

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique identifier |
| medicationName | String | Medication name |
| dosage | String | Dosage amount |
| frequency | String | Frequency of use |
| startDate | Date | Start date |
| endDate | Date | End date (null if ongoing) |
| prescribedBy | String | Prescribing doctor |
| notes | String | Additional notes (max 1000) |
| active | Boolean | Currently active |

---

### Prescription

Treatment prescriptions and notes.

```json
{
  "id": 1,
  "prescribedBy": "Dr. Sarah Williams",
  "prescriptionDate": "2025-10-06T10:00:00",
  "diagnosis": "Hypertension management",
  "treatment": "Continue current medication",
  "notes": "Patient responding well",
  "medications": "Lisinopril 10mg once daily",
  "followUpDate": "2026-01-06T10:00:00"
}
```

| Field | Type | Max Length | Description |
|-------|------|------------|-------------|
| id | Long | - | Unique identifier |
| prescribedBy | String | - | Doctor's name |
| prescriptionDate | DateTime | - | Prescription date |
| diagnosis | String | 2000 | Medical diagnosis |
| treatment | String | 2000 | Treatment plan |
| notes | String | 2000 | Additional notes |
| medications | String | 2000 | Prescribed medications |
| followUpDate | DateTime | - | Next appointment |

---

### Test Result

Laboratory and diagnostic test results.

```json
{
  "id": 1,
  "testName": "Blood Pressure",
  "testDate": "2025-10-09",
  "result": "120/80",
  "resultUnit": "mmHg",
  "referenceRange": "< 120/80",
  "orderedBy": "Dr. Sarah Williams",
  "performedBy": "Nurse Johnson",
  "notes": "Normal reading"
}
```

| Field | Type | Max Length | Description |
|-------|------|------------|-------------|
| id | Long | - | Unique identifier |
| testName | String | - | Test name |
| testDate | Date | - | Test date |
| result | String | 2000 | Test result |
| resultUnit | String | - | Unit of measurement |
| referenceRange | String | - | Normal range |
| orderedBy | String | - | Ordering doctor |
| performedBy | String | - | Lab technician |
| notes | String | 1000 | Additional notes |

---

### Vaccination

Immunization records.

```json
{
  "id": 1,
  "vaccineName": "Influenza",
  "vaccinationDate": "2024-10-16",
  "batchNumber": "FLU2024-001",
  "manufacturer": "Pfizer",
  "administeredBy": "Nurse Williams",
  "nextDoseDate": "2025-10-16",
  "notes": "Annual flu vaccine"
}
```

| Field | Type | Max Length | Description |
|-------|------|------------|-------------|
| id | Long | - | Unique identifier |
| vaccineName | String | - | Vaccine name |
| vaccinationDate | Date | - | Vaccination date |
| batchNumber | String | - | Batch/lot number |
| manufacturer | String | - | Manufacturer name |
| administeredBy | String | - | Healthcare worker |
| nextDoseDate | Date | - | Next dose date |
| notes | String | 1000 | Additional notes |

---

## Enumerations

### AppointmentStatus

```
PENDING_PAYMENT - Awaiting payment (private hospitals)
CONFIRMED       - Appointment confirmed
CANCELLED       - Appointment cancelled
COMPLETED       - Appointment completed
```

**Usage**: Appointment status tracking

**State Transitions**:
- `PENDING_PAYMENT` → `CONFIRMED` (after payment)
- `CONFIRMED` → `CANCELLED` (by user/admin)
- `CONFIRMED` → `COMPLETED` (after appointment)

---

### HospitalType

```
GOVERNMENT - Government hospital (free appointments)
PRIVATE    - Private hospital (paid appointments)
```

**Usage**: Determines payment requirement

**Payment Logic**:
- `GOVERNMENT`: paymentRequired = false, status = CONFIRMED
- `PRIVATE`: paymentRequired = true, status = PENDING_PAYMENT

---

### PaymentMethod

```
CREDIT_CARD    - Credit card payment
DEBIT_CARD     - Debit card payment
ONLINE_BANKING - Online banking transfer
WALLET         - Digital wallet payment
```

**Usage**: Payment processing

**Required Fields**:
- All methods: appointmentId, amount, paymentMethod
- Card methods: cardNumber, cvv

---

### PaymentStatus

```
PENDING   - Payment initiated
COMPLETED - Payment successful
FAILED    - Payment failed
REFUNDED  - Payment refunded
```

**Usage**: Payment tracking

---

### AccessType

```
VIEW     - Viewing medical records
UPDATE   - Adding/modifying records
DOWNLOAD - Downloading records as PDF
```

**Usage**: Medical record access logging

**Typical Scenarios**:
- `VIEW`: Scanning health card, viewing patient records
- `UPDATE`: Adding prescriptions, updating medical history
- `DOWNLOAD`: Generating PDF reports

---

## Validation Rules

### Patient Account

| Field | Validation |
|-------|-----------|
| name | Required, not empty |
| email | Required, unique, valid email format |
| phone | Required, not empty |
| digitalHealthCardNumber | Required, unique |
| medicalHistory | Max 2000 characters |

### Appointments

| Field | Validation |
|-------|-----------|
| patientId | Must exist in database |
| providerId | Must exist in database |
| timeSlotId | Must exist and be available |

**Business Rules**:
- Time slots locked during booking (prevents double-booking)
- Government hospitals: automatic confirmation
- Private hospitals: requires payment

### Prescriptions

| Field | Validation |
|-------|-----------|
| patientId | Must exist in database |
| staffId | Required |
| diagnosis | Required, max 2000 characters |
| treatment | Max 2000 characters |
| notes | Max 2000 characters |
| medications | Max 2000 characters |

### Reports

| Field | Validation |
|-------|-----------|
| startDate | Must be before endDate |
| endDate | Cannot be in the future |
| format | Must be "PDF" or "CSV" |

---

## CORS & Headers

### CORS Configuration

The API is configured to accept requests from any origin:

```
Allowed Origins: * (all)
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: * (all)
Allow Credentials: false
```

### Required Headers

**All Requests**:
```
Content-Type: application/json
```

**No Additional Headers Required**:
- No authentication tokens
- No API keys
- No custom headers

### Response Headers

**JSON Responses**:
```
Content-Type: application/json
```

**PDF Downloads**:
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="report_xxx.pdf"
Content-Length: [size]
```

**CSV Exports**:
```
Content-Type: text/csv
Content-Disposition: attachment; filename="report_xxx.csv"
```

---

## Example Workflows

### Workflow 1: Book a Government Hospital Appointment

```javascript
// Step 1: Get available providers
const providers = await fetch(
  'http://localhost:8080/api/appointments/providers?specialty=Cardiology'
).then(r => r.json());

const governmentProvider = providers.find(p => p.hospitalType === 'GOVERNMENT');

// Step 2: Get available time slots
const slots = await fetch(
  `http://localhost:8080/api/appointments/timeslots?providerId=${governmentProvider.id}&date=2025-10-20T00:00:00`
).then(r => r.json());

const availableSlot = slots.find(s => s.available);

// Step 3: Book appointment
const booking = await fetch(
  'http://localhost:8080/api/appointments/book',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      patientId: 1,
      providerId: governmentProvider.id,
      timeSlotId: availableSlot.id
    })
  }
).then(r => r.json());

// Result: booking.status === 'CONFIRMED' (no payment needed)
console.log('Confirmation:', booking.confirmationNumber);
```

---

### Workflow 2: Book a Private Hospital Appointment with Payment

```javascript
// Step 1-3: Same as above, but select private provider

const privateProvider = providers.find(p => p.hospitalType === 'PRIVATE');

const booking = await fetch(
  'http://localhost:8080/api/appointments/book',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      patientId: 1,
      providerId: privateProvider.id,
      timeSlotId: availableSlot.id
    })
  }
).then(r => r.json());

// Result: booking.status === 'PENDING_PAYMENT'

// Step 4: Process payment
const confirmedBooking = await fetch(
  'http://localhost:8080/api/appointments/payment',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      appointmentId: booking.id,
      amount: 100.00,
      paymentMethod: 'CREDIT_CARD',
      cardNumber: '1234567890123456',
      cvv: '123'
    })
  }
).then(r => r.json());

// Result: confirmedBooking.status === 'CONFIRMED'
console.log('Payment successful! Confirmation:', confirmedBooking.confirmationNumber);
```

---

### Workflow 3: Create Patient and Update Profile

```javascript
// Step 1: Create patient
const patient = await fetch(
  'http://localhost:8080/api/patients',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: 'John Doe',
      email: 'john.doe@example.com',
      phone: '+1234567890',
      digitalHealthCardNumber: 'DHC-2025-001',
      dateOfBirth: '1990-05-15',
      bloodType: 'O+'
    })
  }
).then(r => r.json());

console.log('Patient created with ID:', patient.id);

// Step 2: Update patient info later
const updated = await fetch(
  `http://localhost:8080/api/patients/${patient.id}`,
  {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      address: '123 New Street',
      allergies: 'Penicillin'
    })
  }
).then(r => r.json());

console.log('Patient updated:', updated);
```

---

### Workflow 4: Access Medical Records and Add Prescription

```javascript
// Step 1: Scan health card to access records
const medicalRecord = await fetch(
  'http://localhost:8080/api/medical-records/scan-card',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      cardNumber: 'DHC-2025-001',
      staffId: 'DR-SMITH-001',
      purpose: 'General consultation'
    })
  }
).then(r => r.json());

console.log('Patient allergies:', medicalRecord.allergies);
console.log('Current medications:', medicalRecord.currentMedications);

// Step 2: Add new prescription
const updatedRecord = await fetch(
  'http://localhost:8080/api/medical-records/prescriptions',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      patientId: medicalRecord.patientId,
      staffId: 'DR-SMITH-001',
      diagnosis: 'Seasonal Allergies',
      treatment: 'Antihistamines and rest',
      medications: 'Cetirizine 10mg once daily',
      notes: 'Review in 2 weeks',
      followUpDate: '2025-10-30T14:00:00'
    })
  }
).then(r => r.json());

console.log('Prescription added successfully');
```

---

### Workflow 5: Generate and Export Report

```javascript
// Step 1: Generate report
const report = await fetch(
  'http://localhost:8080/api/reports?hospital=City%20Hospital&startDate=2025-10-01&endDate=2025-10-16'
).then(r => r.json());

console.log('Total visits:', report.kpis.totalVisits);
console.log('Revenue:', report.kpis.totalRevenue);

// Step 2: Export as PDF
const pdfBlob = await fetch(
  'http://localhost:8080/api/reports/export',
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      format: 'PDF',
      filters: {
        hospital: 'City Hospital',
        startDate: '2025-10-01',
        endDate: '2025-10-16'
      }
    })
  }
).then(r => r.blob());

// Download PDF
const url = window.URL.createObjectURL(pdfBlob);
const a = document.createElement('a');
a.href = url;
a.download = 'report.pdf';
a.click();
```

---

## Additional Notes

### Database

- **Type**: H2 In-Memory Database
- **URL**: `jdbc:h2:mem:medidb`
- **Console**: Accessible at `http://localhost:8080/h2-console`
- **Credentials**: Username: `sa`, Password: (empty)
- **Persistence**: Data is lost on application restart

### Sample Data

The application initializes with:
- 2 Patients
- 4 Healthcare Providers (2 Government, 2 Private)
- 20 Time Slots (5 per provider)
- Sample medications, prescriptions, test results, and vaccinations

### Rate Limiting

Currently, no rate limiting is implemented. All endpoints accept unlimited requests.

### Performance Considerations

- No pagination on list endpoints
- All related data is loaded (no lazy loading on API responses)
- Reports with large date ranges may take longer to generate

### Security Warnings

⚠️ **This is a demonstration system**:
- No authentication/authorization
- No data encryption
- No input sanitization beyond basic validation
- Not suitable for production use with real patient data

### Future Enhancements

Potential features for production:
- Authentication & authorization (OAuth2/JWT)
- Role-based access control
- Data encryption at rest and in transit
- Pagination for list endpoints
- Rate limiting
- Email/SMS notifications
- Appointment cancellation/rescheduling
- Real payment gateway integration
- Chart visualizations for reports

---

## Support

For issues or questions:
- Check error messages for specific details
- Verify request format matches examples
- Ensure all required fields are provided
- Check the H2 console for database state

**API Version**: 1.0  
**Last Updated**: October 16, 2025
