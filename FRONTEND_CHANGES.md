# Frontend Implementation Guide - Authentication & Authorization System

## Overview
This document provides comprehensive guidance for implementing the frontend for the newly added authentication and authorization features in the MediBackend system. The backend now supports two user roles: **ADMIN** and **PATIENT**, with role-based access control.

## Table of Contents
1. [New API Endpoints](#new-api-endpoints)
2. [Authentication Flow](#authentication-flow)
3. [User Roles & Permissions](#user-roles--permissions)
4. [Page Requirements](#page-requirements)
5. [Updated Endpoints](#updated-endpoints)
6. [Error Handling](#error-handling)
7. [Sample Implementation](#sample-implementation)

---

## New API Endpoints

### 1. Authentication Endpoints

#### POST `/api/auth/register`
**Purpose:** Register a new patient account

**Request Body:**
```json
{
  "username": "string (required, unique)",
  "password": "string (required)",
  "name": "string (required)",
  "email": "string (required, unique)",
  "phone": "string (required)",
  "digitalHealthCardNumber": "string (required, unique)",
  "address": "string (optional)",
  "dateOfBirth": "string (optional, format: YYYY-MM-DD)",
  "emergencyContactName": "string (optional)",
  "emergencyContactPhone": "string (optional)",
  "bloodType": "string (optional)",
  "allergies": "string (optional)",
  "medicalHistory": "string (optional)"
}
```

**Success Response (201 Created):**
```json
{
  "userId": 1,
  "username": "john.doe",
  "role": "PATIENT",
  "patientId": 1,
  "message": "Registration successful"
}
```

**Error Responses:**
- `400 Bad Request`: Username, email, or digital health card number already exists
```json
{
  "error": "Username already exists"
}
```

---

#### POST `/api/auth/login`
**Purpose:** Login for both admin and patient users

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Success Response (200 OK):**
```json
{
  "userId": 1,
  "username": "admin",
  "role": "ADMIN",  // or "PATIENT"
  "patientId": null,  // only populated for PATIENT role
  "message": "Login successful"
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials or inactive account
```json
{
  "error": "Invalid username or password"
}
```
```json
{
  "error": "Account is inactive"
}
```

---

#### POST `/api/auth/logout`
**Purpose:** Logout (client-side session cleanup)

**Success Response (200 OK):**
```json
{
  "message": "Logout successful"
}
```

**Note:** Logout is primarily a client-side operation. Clear stored auth information (userId, role, patientId, etc.) from local storage/session storage.

---

### 2. Admin Dashboard Endpoints

#### GET `/api/admin/dashboard`
**Purpose:** Get admin dashboard overview

**Authorization:** Admin only

**Success Response (200 OK):**
```json
{
  "totalPatients": 2,
  "totalAppointments": 5,
  "recentPatients": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "phone": "+1234567890",
      "digitalHealthCardNumber": "DHC-2024-001",
      "address": "123 Main Street, City",
      "dateOfBirth": "1985-05-15",
      "emergencyContactName": "Jane Doe",
      "emergencyContactPhone": "+1234567899",
      "medicalHistory": "Hypertension diagnosed in 2018",
      "bloodType": "O+",
      "allergies": "Penicillin"
    }
    // ... up to 5 recent patients
  ],
  "recentAppointments": [
    {
      "id": 1,
      "patientId": 1,
      "patientName": "John Doe",
      "providerId": 1,
      "providerName": "Dr. Sarah Williams",
      "specialty": "Cardiology",
      "appointmentTime": "2025-10-17T09:00:00",
      "confirmationNumber": "APT-12345678",
      "status": "CONFIRMED",
      "paymentRequired": false,
      "hospitalName": "City Government Hospital"
    }
    // ... up to 5 recent appointments
  ]
}
```

---

#### GET `/api/admin/patients`
**Purpose:** Get all registered patients

**Authorization:** Admin only

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "digitalHealthCardNumber": "DHC-2024-001",
    "address": "123 Main Street, City",
    "dateOfBirth": "1985-05-15",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+1234567899",
    "medicalHistory": "Hypertension diagnosed in 2018",
    "bloodType": "O+",
    "allergies": "Penicillin"
  }
  // ... all patients
]
```

---

#### GET `/api/admin/appointments`
**Purpose:** Get all appointments

**Authorization:** Admin only

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "John Doe",
    "providerId": 1,
    "providerName": "Dr. Sarah Williams",
    "specialty": "Cardiology",
    "appointmentTime": "2025-10-17T09:00:00",
    "confirmationNumber": "APT-12345678",
    "status": "CONFIRMED",
    "paymentRequired": false,
    "hospitalName": "City Government Hospital"
  }
  // ... all appointments
]
```

---

#### DELETE `/api/admin/patients/{patientId}`
**Purpose:** Delete a patient account

**Authorization:** Admin only

**URL Parameters:**
- `patientId`: Patient ID (number)

**Success Response (200 OK):**
```json
{
  "message": "Patient deleted successfully"
}
```

**Error Response:**
- `404 Not Found`: Patient not found

---

#### DELETE `/api/admin/appointments/{appointmentId}`
**Purpose:** Cancel an appointment

**Authorization:** Admin only

**URL Parameters:**
- `appointmentId`: Appointment ID (number)

**Success Response (200 OK):**
```json
{
  "message": "Appointment cancelled successfully"
}
```

**Error Response:**
- `404 Not Found`: Appointment not found

---

## Authentication Flow

### Registration Flow (Patient)
1. User fills out registration form
2. Frontend validates all required fields
3. POST request to `/api/auth/register`
4. On success:
   - Store `userId`, `username`, `role`, and `patientId` in session/local storage
   - Redirect to patient dashboard
5. On error:
   - Display error message (e.g., "Username already exists")

### Login Flow (Admin & Patient)
1. User enters username and password
2. POST request to `/api/auth/login`
3. On success:
   - Store `userId`, `username`, `role`, and `patientId` (if patient) in session/local storage
   - If role is "ADMIN", redirect to admin dashboard
   - If role is "PATIENT", redirect to patient dashboard
4. On error:
   - Display error message

### Logout Flow
1. User clicks logout button
2. POST request to `/api/auth/logout` (optional)
3. Clear all stored authentication data
4. Redirect to login page

---

## User Roles & Permissions

### ADMIN Role
**Default Credentials:** username: `admin`, password: `admin`

**Permissions:**
- View all patients (`/api/admin/patients`)
- View all appointments (`/api/admin/appointments`)
- View dashboard statistics (`/api/admin/dashboard`)
- Delete patient accounts (`/api/admin/patients/{patientId}`)
- Cancel appointments (`/api/admin/appointments/{appointmentId}`)
- Access all other system endpoints

### PATIENT Role
**Permissions:**
- View/update their own patient profile (`/api/patients/{patientId}`)
- Book appointments (`/api/appointments/book`)
- View their own appointments (`/api/appointments/patient/{patientId}`)
- Access medical records (`/api/medical-records/{patientId}`)
- View available providers and time slots
- Process payments for private appointments

---

## Page Requirements

### 1. Login Page
**Route:** `/login`

**Requirements:**
- Username input field
- Password input field
- Login button
- Link to registration page
- Error message display area

**Functionality:**
- Call `/api/auth/login` on form submission
- Store authentication data on success
- Route to appropriate dashboard based on role
- Display error messages

---

### 2. Registration Page (Patient)
**Route:** `/register`

**Requirements:**
- All patient information fields (see register endpoint)
- Password and confirm password fields
- Terms and conditions checkbox
- Submit button
- Link to login page

**Functionality:**
- Validate all required fields
- Password confirmation
- Call `/api/auth/register` on form submission
- Store authentication data on success
- Route to patient dashboard
- Display error messages

---

### 3. Admin Dashboard
**Route:** `/admin/dashboard`

**Authorization:** Admin only

**Requirements:**
- Display total patients count
- Display total appointments count
- List of recent patients (up to 5)
- List of recent appointments (up to 5)
- Navigation to:
  - All Patients page
  - All Appointments page
  - Logout button

**Functionality:**
- Load dashboard data from `/api/admin/dashboard`
- Redirect to login if not authenticated as admin
- Handle logout

---

### 4. Admin - All Patients Page
**Route:** `/admin/patients`

**Authorization:** Admin only

**Requirements:**
- Table displaying all patients with columns:
  - ID
  - Name
  - Email
  - Phone
  - Digital Health Card Number
  - Date of Birth
  - Actions (View, Delete)
- Search/filter functionality
- Pagination (if needed)

**Functionality:**
- Load patients from `/api/admin/patients`
- View patient details (route to patient profile page)
- Delete patient with confirmation dialog
- Handle delete via `/api/admin/patients/{patientId}`

---

### 5. Admin - All Appointments Page
**Route:** `/admin/appointments`

**Authorization:** Admin only

**Requirements:**
- Table displaying all appointments with columns:
  - ID
  - Patient Name
  - Provider Name
  - Specialty
  - Date & Time
  - Status
  - Hospital Name
  - Actions (View, Cancel)
- Filter by status, date range
- Search functionality

**Functionality:**
- Load appointments from `/api/admin/appointments`
- View appointment details
- Cancel appointment with confirmation dialog
- Handle cancel via `/api/admin/appointments/{appointmentId}`

---

### 6. Patient Dashboard
**Route:** `/patient/dashboard`

**Authorization:** Patient only

**Requirements:**
- Welcome message with patient name
- Quick actions:
  - Book Appointment
  - View My Appointments
  - View My Medical Records
  - Manage My Account
- Display upcoming appointments
- Recent medical records summary

**Functionality:**
- Load patient data using stored `patientId`
- Navigation to appointment booking, appointments list, medical records, profile pages
- Handle logout

---

### 7. Book Appointment Page (Updated)
**Route:** `/patient/book-appointment`

**Authorization:** Patient only

**Requirements:**
- Same as before (provider selection, specialty filter, date/time selection)
- Use stored `patientId` automatically

**Functionality:**
- No changes to existing functionality
- Use authenticated patient's ID for booking

---

### 8. My Appointments Page (Patient)
**Route:** `/patient/appointments`

**Authorization:** Patient only

**Requirements:**
- List of patient's appointments
- Filter by status (upcoming, past, cancelled)
- Display confirmation number, provider, date/time, status

**Functionality:**
- Load appointments from `/api/appointments/patient/{patientId}`
- Use stored `patientId`

---

### 9. My Account Page (Patient)
**Route:** `/patient/account`

**Authorization:** Patient only

**Requirements:**
- Display and edit all patient information
- Change password functionality (to be implemented if needed)
- Update button
- Display username (read-only)

**Functionality:**
- Load patient data from `/api/patients/{patientId}`
- Update via `/api/patients/{patientId}`

---

## Updated Endpoints

### Existing Endpoints - No Changes Required
The following endpoints remain unchanged and can be used as before:

- `GET /api/appointments/providers` - Get healthcare providers
- `GET /api/appointments/timeslots` - Get available time slots
- `POST /api/appointments/book` - Book appointment
- `POST /api/appointments/payment` - Process payment
- `GET /api/appointments/confirmation/{number}` - Get appointment by confirmation
- `GET /api/appointments/patient/{patientId}` - Get patient appointments
- `GET /api/patients/{id}` - Get patient profile
- `PUT /api/patients/{id}` - Update patient profile
- All medical records endpoints (`/api/medical-records/*`)
- All report endpoints (`/api/reports/*`)

**Note:** For patient-specific endpoints, use the `patientId` from the authentication response.

---

## Error Handling

### HTTP Status Codes
- `200 OK`: Successful request
- `201 Created`: Resource created successfully (registration)
- `400 Bad Request`: Invalid input or constraint violation
- `401 Unauthorized`: Invalid credentials or not authenticated
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

### Error Response Format
```json
{
  "error": "Error message description"
}
```

or

```json
{
  "timestamp": "2025-10-16T16:47:00",
  "message": "Error message description",
  "status": 404
}
```

### Client-Side Error Handling
1. **Authentication Errors (401):**
   - Clear stored authentication data
   - Redirect to login page
   - Display appropriate error message

2. **Not Found Errors (404):**
   - Display user-friendly error message
   - Offer navigation back to relevant page

3. **Validation Errors (400):**
   - Display specific field errors
   - Keep form data so user can correct

4. **Server Errors (500):**
   - Display generic error message
   - Log error details for debugging
   - Offer retry option

---

## Sample Implementation

### Storing Authentication Data (JavaScript)
```javascript
// After successful login or registration
const authData = {
  userId: response.userId,
  username: response.username,
  role: response.role,
  patientId: response.patientId
};
localStorage.setItem('auth', JSON.stringify(authData));
```

### Checking Authentication
```javascript
function isAuthenticated() {
  const auth = JSON.parse(localStorage.getItem('auth'));
  return auth && auth.userId;
}

function isAdmin() {
  const auth = JSON.parse(localStorage.getItem('auth'));
  return auth && auth.role === 'ADMIN';
}

function isPatient() {
  const auth = JSON.parse(localStorage.getItem('auth'));
  return auth && auth.role === 'PATIENT';
}

function getPatientId() {
  const auth = JSON.parse(localStorage.getItem('auth'));
  return auth ? auth.patientId : null;
}
```

### Protected Route Example (React)
```javascript
import { Navigate } from 'react-router-dom';

function AdminRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }
  if (!isAdmin()) {
    return <Navigate to="/patient/dashboard" />;
  }
  return children;
}

function PatientRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }
  if (!isPatient()) {
    return <Navigate to="/admin/dashboard" />;
  }
  return children;
}
```

### API Call Example with Error Handling
```javascript
async function login(username, password) {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Login failed');
    }

    const data = await response.json();
    localStorage.setItem('auth', JSON.stringify(data));
    
    // Redirect based on role
    if (data.role === 'ADMIN') {
      window.location.href = '/admin/dashboard';
    } else {
      window.location.href = '/patient/dashboard';
    }
  } catch (error) {
    console.error('Login error:', error);
    alert(error.message);
  }
}
```

### Logout Example
```javascript
async function logout() {
  try {
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST'
    });
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    localStorage.removeItem('auth');
    window.location.href = '/login';
  }
}
```

---

## Testing Credentials

### Admin Account
- **Username:** `admin`
- **Password:** `admin`

### Test Patient Accounts
- **Username:** `john.doe`
  - **Password:** `password123`
  - **Patient ID:** 1

- **Username:** `jane.smith`
  - **Password:** `password123`
  - **Patient ID:** 2

---

## Implementation Checklist

### Phase 1: Authentication
- [ ] Create login page
- [ ] Create registration page
- [ ] Implement authentication service/hooks
- [ ] Set up protected routes
- [ ] Test login with admin and patient accounts
- [ ] Test registration flow
- [ ] Test logout functionality

### Phase 2: Admin Dashboard
- [ ] Create admin dashboard page
- [ ] Create all patients page with delete functionality
- [ ] Create all appointments page with cancel functionality
- [ ] Implement admin navigation
- [ ] Test all admin functionalities

### Phase 3: Patient Dashboard
- [ ] Create patient dashboard page
- [ ] Update appointment booking to use authenticated patient
- [ ] Create my appointments page
- [ ] Create my account/profile page
- [ ] Test all patient functionalities

### Phase 4: Integration & Testing
- [ ] Test role-based access control
- [ ] Test error handling
- [ ] Test navigation between pages
- [ ] Test logout from different pages
- [ ] Perform end-to-end testing

---

## Summary

This implementation guide provides all the necessary information to build a complete frontend for the authentication and authorization system. The system supports:

1. **User Registration** for patients
2. **Login** for both admin and patients
3. **Admin Dashboard** with full management capabilities
4. **Patient Dashboard** with self-service features
5. **Role-Based Access Control** ensuring proper security
6. **Complete CRUD operations** for managing patients and appointments

All existing appointment booking and medical records functionality remains unchanged, just requiring the authenticated user's information (patientId) for patient-specific operations.

For any questions or clarifications, please refer to the API documentation or contact the backend development team.
