# MediBackend - Healthcare Management System

## Overview
This is a comprehensive Spring Boot application implementing multiple healthcare management use cases. The system includes:
- Appointment booking with healthcare providers (free for government hospitals, paid for private)
- Patient account management with full CRUD operations
- Statistical reporting and analytics with PDF/CSV export
- Medical records access through digital health card scanning

The application provides a complete REST API for healthcare operations including booking management, patient profiles, analytics reports, and comprehensive medical records access.

## Features Implemented

### UC-01: Appointment Booking
- Browse healthcare providers by specialty
- View real-time availability of time slots
- Book appointments (free for government hospitals, paid for private hospitals)
- Process payments for private hospital appointments
- View appointment confirmation and details
- Concurrent booking protection (prevents double-booking of time slots)

### UC-02: Patient Account Management
- Create, read, update, and delete patient accounts
- Store comprehensive patient demographics
- Audit logging for all patient account changes
- Unique digital health card number tracking

### UC-03: Statistical Reporting
- Generate statistical reports with KPIs and analytics
- Export reports as PDF or CSV
- Filter reports by hospital, department, and date range
- Daily visits and department breakdowns

### UC-04: Medical Records Access (NEW!)
- Access comprehensive medical records by scanning digital health card
- View patient demographics, medical history, and allergies
- Track current medications and prescriptions
- Access test results and vaccination records
- View previous visit history
- Add new prescriptions and treatment notes
- Download medical records as PDF
- Complete audit trail of all medical record access

### Technical Features
- RESTful API with Spring Boot
- H2 in-memory database
- JPA/Hibernate for data persistence
- Lombok for reducing boilerplate code
- CORS enabled for all origins
- No authentication required (as per requirements)
- Comprehensive unit and integration tests (94 tests passing!)

## Tech Stack
- Java 17
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security (configured for public access)
- H2 Database
- Lombok
- JUnit 5 & Mockito for testing

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use included mvnw wrapper)

### Running the Application

```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using installed Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AppointmentServiceTest
```

## API Endpoints

### 1. Get Healthcare Providers

**GET** `/api/appointments/providers`

Get all healthcare providers or filter by specialty.

**Query Parameters:**
- `specialty` (optional): Filter providers by specialty

**Example:**
```bash
# Get all providers
curl http://localhost:8080/api/appointments/providers

# Get providers by specialty
curl "http://localhost:8080/api/appointments/providers?specialty=Cardiology"
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Dr. Sarah Williams",
    "specialty": "Cardiology",
    "hospitalName": "City Government Hospital",
    "hospitalType": "GOVERNMENT"
  }
]
```

### 2. Get Available Time Slots

**GET** `/api/appointments/timeslots`

Get available time slots for a specific provider on a given date.

**Query Parameters:**
- `providerId` (required): ID of the healthcare provider
- `date` (required): Date in ISO 8601 format (e.g., 2025-10-13T00:00:00)

**Example:**
```bash
curl "http://localhost:8080/api/appointments/timeslots?providerId=1&date=2025-10-13T00:00:00"
```

**Response:**
```json
[
  {
    "id": 1,
    "providerId": 1,
    "providerName": "Dr. Sarah Williams",
    "startTime": "2025-10-13T14:00:00",
    "endTime": "2025-10-13T15:00:00",
    "available": true
  }
]
```

### 3. Book Appointment

**POST** `/api/appointments/book`

Book an appointment with a healthcare provider.

**Request Body:**
```json
{
  "patientId": 1,
  "providerId": 1,
  "timeSlotId": 1
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/appointments/book \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"providerId":1,"timeSlotId":1}'
```

**Response (Government Hospital - No Payment Required):**
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "John Doe",
  "providerId": 1,
  "providerName": "Dr. Sarah Williams",
  "specialty": "Cardiology",
  "appointmentTime": "2025-10-13T14:00:00",
  "confirmationNumber": "APT-49C2A07E",
  "status": "CONFIRMED",
  "paymentRequired": false,
  "hospitalName": "City Government Hospital"
}
```

**Response (Private Hospital - Payment Required):**
```json
{
  "id": 2,
  "patientId": 2,
  "patientName": "Jane Smith",
  "providerId": 3,
  "providerName": "Dr. Emily Davis",
  "specialty": "Dermatology",
  "appointmentTime": "2025-10-13T14:00:00",
  "confirmationNumber": "APT-BC96D524",
  "status": "PENDING_PAYMENT",
  "paymentRequired": true,
  "hospitalName": "Advanced Private Clinic"
}
```

### 4. Process Payment

**POST** `/api/appointments/payment`

Process payment for a private hospital appointment.

**Request Body:**
```json
{
  "appointmentId": 2,
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "1234567890123456",
  "cvv": "123"
}
```

**Payment Methods:**
- `CREDIT_CARD`
- `DEBIT_CARD`
- `ONLINE_BANKING`
- `WALLET`

**Example:**
```bash
curl -X POST http://localhost:8080/api/appointments/payment \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 2,
    "amount": 100.00,
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "1234567890123456",
    "cvv": "123"
  }'
```

**Response:**
```json
{
  "id": 2,
  "patientId": 2,
  "patientName": "Jane Smith",
  "providerId": 3,
  "providerName": "Dr. Emily Davis",
  "specialty": "Dermatology",
  "appointmentTime": "2025-10-13T14:00:00",
  "confirmationNumber": "APT-BC96D524",
  "status": "CONFIRMED",
  "paymentRequired": true,
  "hospitalName": "Advanced Private Clinic"
}
```

### 5. Get Appointment by Confirmation Number

**GET** `/api/appointments/confirmation/{confirmationNumber}`

Retrieve appointment details by confirmation number.

**Example:**
```bash
curl http://localhost:8080/api/appointments/confirmation/APT-49C2A07E
```

### 6. Get Patient Appointments

**GET** `/api/appointments/patient/{patientId}`

Get all appointments for a specific patient.

**Example:**
```bash
curl http://localhost:8080/api/appointments/patient/1
```

**Response:**
```json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "John Doe",
    "providerId": 1,
    "providerName": "Dr. Sarah Williams",
    "specialty": "Cardiology",
    "appointmentTime": "2025-10-13T14:00:00",
    "confirmationNumber": "APT-49C2A07E",
    "status": "CONFIRMED",
    "paymentRequired": false,
    "hospitalName": "City Government Hospital"
  }
]
```

### 7. Create Patient Account (UC-02)

**POST** `/api/patients`

Create a new patient account in the system.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "digitalHealthCardNumber": "DHC12345",
  "address": "123 Main St, City",
  "dateOfBirth": "1990-01-01",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "0987654321",
  "medicalHistory": "No major health issues",
  "bloodType": "O+",
  "allergies": "None"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "digitalHealthCardNumber": "DHC12345",
    "address": "123 Main St",
    "dateOfBirth": "1990-01-01"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "digitalHealthCardNumber": "DHC12345",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-01",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "0987654321",
  "medicalHistory": "No major health issues",
  "bloodType": "O+",
  "allergies": "None"
}
```

### 8. Get Patient Profile (UC-02)

**GET** `/api/patients/{patientId}`

Retrieve complete patient profile information.

**Example:**
```bash
curl http://localhost:8080/api/patients/1
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "digitalHealthCardNumber": "DHC12345",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-01",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "0987654321",
  "medicalHistory": "No major health issues",
  "bloodType": "O+",
  "allergies": "None"
}
```

### 9. Get All Patients (UC-02)

**GET** `/api/patients`

Get all patient profiles in the system.

**Example:**
```bash
curl http://localhost:8080/api/patients
```

### 10. Update Patient Profile (UC-02)

**PUT** `/api/patients/{patientId}`

Update patient account information. Only provided fields will be updated.

**Request Body:**
```json
{
  "name": "John Updated",
  "phone": "9999999999",
  "address": "456 New St",
  "emergencyContactName": "Jane Updated",
  "emergencyContactPhone": "1111111111",
  "medicalHistory": "Updated medical history",
  "bloodType": "O+",
  "allergies": "Peanuts"
}
```

**Example:**
```bash
curl -X PUT http://localhost:8080/api/patients/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "phone": "9999999999",
    "address": "456 New St"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "John Updated",
  "email": "john@example.com",
  "phone": "9999999999",
  "digitalHealthCardNumber": "DHC12345",
  "address": "456 New St",
  "dateOfBirth": "1990-01-01",
  "emergencyContactName": "Jane Updated",
  "emergencyContactPhone": "1111111111",
  "medicalHistory": "Updated medical history",
  "bloodType": "O+",
  "allergies": "Peanuts"
}
```

### 11. Delete Patient Account (UC-02)

**DELETE** `/api/patients/{patientId}`

Delete a patient account from the system.

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/patients/1
```

**Response:** 204 No Content

## Data Models

### Patient
- `id`: Unique identifier
- `name`: Patient's full name
- `email`: Email address (unique)
- `phone`: Phone number
- `digitalHealthCardNumber`: Unique health card number
- `address`: Patient's address (optional)
- `dateOfBirth`: Patient's date of birth (optional)
- `emergencyContactName`: Emergency contact name (optional)
- `emergencyContactPhone`: Emergency contact phone (optional)
- `medicalHistory`: Patient's medical history (optional)
- `bloodType`: Patient's blood type (optional)
- `allergies`: Patient's allergies (optional)

### Healthcare Provider
- `id`: Unique identifier
- `name`: Provider's name
- `specialty`: Medical specialty
- `hospitalName`: Hospital/clinic name
- `hospitalType`: GOVERNMENT or PRIVATE

### Appointment
- `id`: Unique identifier
- `patient`: Patient reference
- `provider`: Provider reference
- `timeSlot`: Time slot reference
- `status`: PENDING_PAYMENT, CONFIRMED, CANCELLED, or COMPLETED
- `bookingDateTime`: When the appointment was booked
- `confirmationNumber`: Unique confirmation number
- `paymentRequired`: Whether payment is required

### Payment
- `id`: Unique identifier
- `appointment`: Appointment reference
- `amount`: Payment amount
- `status`: PENDING, COMPLETED, FAILED, or REFUNDED
- `transactionId`: Unique transaction identifier
- `paymentMethod`: Payment method used
- `paymentDateTime`: When payment was processed

### AuditLog
- `id`: Unique identifier
- `patientId`: Patient reference
- `action`: Action performed (CREATE_ACCOUNT, UPDATE_PROFILE, DELETE_ACCOUNT)
- `details`: Detailed description of changes
- `timestamp`: When the action was performed
- `ipAddress`: IP address from which the action was performed (optional)

## Sample Data

The application initializes with sample data:
- 2 Patients
- 4 Healthcare Providers (2 Government, 2 Private)
- 20 Time Slots (5 per provider)

## Error Handling

The API returns appropriate HTTP status codes and error messages:

```json
{
  "timestamp": "2025-10-12T14:17:44.294584441",
  "message": "Time slot is no longer available",
  "status": 500
}
```

## CORS Configuration

CORS is enabled for all origins with the following settings:
- **Allowed Origins:** `*`
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers:** `*`

## Security

Security is configured to allow public access to all endpoints (as per requirements):
- No authentication required
- CSRF protection disabled
- All endpoints are publicly accessible

## Testing

The project includes comprehensive tests:

### Unit Tests (11 tests)
- Service layer tests with Mockito
- Tests for booking appointments (government and private)
- Payment processing tests
- Error scenario tests

### Integration Tests (9 tests)
- Full API endpoint tests
- Database integration tests
- End-to-end booking flow tests

All 21 tests pass successfully.

## Database

H2 in-memory database is used with the following configuration:
- **URL:** `jdbc:h2:mem:medidb`
- **Console:** Enabled at `/h2-console`
- **Schema:** Auto-created on startup
- **Data:** Sample data initialized on startup

To access H2 console:
1. Navigate to `http://localhost:8080/h2-console`
2. Use JDBC URL: `jdbc:h2:mem:medidb`
3. Username: `sa`
4. Password: (leave empty)

## Design Principles

The implementation follows SOLID principles:
- **Single Responsibility:** Each class has one responsibility
- **Open/Closed:** Extensible without modification
- **Liskov Substitution:** Proper inheritance hierarchy
- **Interface Segregation:** Focused repository interfaces
- **Dependency Inversion:** Dependencies injected through constructors

## Project Structure

```
src/
├── main/
│   ├── java/com/hapangama/medibackend/
│   │   ├── config/           # Security, CORS, Data initialization
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Exception handling
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Spring Data repositories
│   │   └── service/          # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/hapangama/medibackend/
        ├── controller/       # Integration tests
        └── service/          # Unit tests
```

### 12. Generate Statistical Report (UC-03)

**GET** `/api/reports`

Generate a comprehensive statistical report with KPIs, daily visits breakdown, and department statistics.

**Query Parameters:**
- `hospital` (optional): Filter by hospital name
- `department` (optional): Filter by department/specialty
- `startDate` (optional): Start date in ISO format (YYYY-MM-DD)
- `endDate` (optional): End date in ISO format (YYYY-MM-DD)
- `reportType` (optional): Type of report
- `granularity` (optional): Data granularity (DAILY, WEEKLY, MONTHLY)

**Example:**
```bash
# Get report without filters (last 30 days)
curl http://localhost:8080/api/reports

# Get report for specific hospital
curl "http://localhost:8080/api/reports?hospital=City%20Hospital"

# Get report for specific department
curl "http://localhost:8080/api/reports?department=Cardiology"

# Get report with date range
curl "http://localhost:8080/api/reports?startDate=2025-10-01&endDate=2025-10-16"

# Get report with multiple filters
curl "http://localhost:8080/api/reports?hospital=City%20Hospital&department=Cardiology&startDate=2025-10-01&endDate=2025-10-16"
```

**Response:**
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
  "generatedAt": "2025-10-16T14:42:00"
}
```

**Response (No Data):**
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
    "endDate": "2025-11-30"
  },
  "generatedAt": "2025-10-16T14:42:00",
  "message": "No data available for the selected filters and date range"
}
```

### 13. Export Report (UC-03)

**POST** `/api/reports/export`

Export a report in PDF or CSV format with the specified filters.

**Request Body:**
```json
{
  "format": "PDF",
  "filters": {
    "hospital": "City Hospital",
    "department": "Cardiology",
    "startDate": "2025-10-01",
    "endDate": "2025-10-16"
  }
}
```

**Supported Formats:**
- `PDF` (default): Generates a formatted PDF report
- `CSV`: Generates a CSV file with report data

**Example:**
```bash
# Export as PDF
curl -X POST http://localhost:8080/api/reports/export \
  -H "Content-Type: application/json" \
  -d '{
    "format": "PDF",
    "filters": {
      "startDate": "2025-10-01",
      "endDate": "2025-10-16"
    }
  }' \
  --output report.pdf

# Export as CSV
curl -X POST http://localhost:8080/api/reports/export \
  -H "Content-Type: application/json" \
  -d '{
    "format": "CSV",
    "filters": {
      "hospital": "City Hospital",
      "startDate": "2025-10-01",
      "endDate": "2025-10-16"
    }
  }' \
  --output report.csv
```

**Response:**
- **Content-Type:** `application/pdf` or `text/csv`
- **Content-Disposition:** `attachment; filename="report_<timestamp>.pdf"`
- **Body:** Binary PDF data or CSV text

**CSV Format Example:**
```csv
Healthcare Statistical Report
Generated: 2025-10-16T14:42:00

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

**PDF Format:**
The PDF export includes:
- Report title and generation timestamp
- Applied filters section
- Key Performance Indicators in readable format
- Department Breakdown table with all metrics
- Professional formatting and layout

## Report Features (UC-03)

### Key Performance Indicators (KPIs)
- **Total Visits:** Total number of appointments in the date range
- **Confirmed Appointments:** Number of successfully confirmed appointments
- **Pending Payments:** Number of appointments awaiting payment
- **Cancelled Appointments:** Number of cancelled appointments
- **Total Revenue:** Sum of all completed payments
- **Average Wait Time:** Average time (in hours) between booking and appointment
- **Completion Rate:** Percentage of confirmed appointments out of total visits

### Daily Visits Breakdown
Shows day-by-day statistics including:
- Visit count per day
- Number of confirmed appointments per day
- Number of cancelled appointments per day

### Department Breakdown
Shows statistics grouped by medical specialty/department:
- Total appointments per department
- Confirmed appointments per department
- Revenue generated per department
- Completion rate per department

### Filtering Options
Reports can be filtered by:
- **Hospital:** Filter by specific hospital name
- **Department:** Filter by medical specialty (e.g., Cardiology, Dermatology)
- **Date Range:** Filter by start and end dates
- **Multiple Filters:** Combine hospital, department, and date range filters

### Edge Cases Handled
- **No Data Available:** Returns appropriate message when no data matches filters
- **Large Result Sets:** Efficiently handles large datasets with pagination in mind
- **Export Failures:** Tracks failed exports and provides error messages
- **Invalid Filters:** Gracefully handles invalid or missing filter parameters

### Audit Trail
All report exports are logged in the system with:
- Export format (PDF/CSV)
- Export timestamp
- Filter parameters used
- Export status (COMPLETED/FAILED)

## Medical Records Access (UC-04)

### Overview
The Medical Records Access API allows hospital staff to access comprehensive patient medical records by scanning their digital health card or directly by patient ID. The system provides complete medical history including demographics, medications, prescriptions, test results, vaccinations, and previous visit records.

### API Endpoints

#### 1. Scan Digital Health Card

**POST** `/api/medical-records/scan-card`

Access patient medical records by scanning their digital health card barcode/QR code.

**Request Body:**
```json
{
  "cardNumber": "DHC-2024-001",
  "staffId": "DR-SMITH-001",
  "purpose": "General consultation"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/medical-records/scan-card \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "DHC-2024-001",
    "staffId": "DR-SMITH-001",
    "purpose": "General consultation"
  }'
```

**Response:**
```json
{
  "patientId": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "digitalHealthCardNumber": "DHC-2024-001",
  "address": "123 Main Street, City",
  "dateOfBirth": "1985-05-15",
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
      "prescribedBy": "Dr. Sarah Williams",
      "active": true
    }
  ],
  "previousVisits": [
    {
      "appointmentId": 1,
      "visitDate": "2024-10-11T14:00:00",
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
      "prescriptionDate": "2024-10-06T10:00:00",
      "diagnosis": "Hypertension management",
      "treatment": "Continue current medication, monitor blood pressure",
      "medications": "Lisinopril 10mg once daily",
      "notes": "Patient responding well to treatment",
      "followUpDate": "2025-01-06T10:00:00"
    }
  ],
  "testResults": [
    {
      "id": 1,
      "testName": "Blood Pressure",
      "testDate": "2024-10-09",
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
      "vaccinationDate": "2024-04-16",
      "batchNumber": "FLU2024-001",
      "manufacturer": "Pfizer",
      "administeredBy": "Nurse Williams",
      "nextDoseDate": "2025-04-16",
      "notes": "Annual flu vaccine"
    }
  ],
  "accessedAt": "2025-10-16T15:30:00",
  "accessedBy": "DR-SMITH-001"
}
```

#### 2. Get Medical Records by Patient ID

**GET** `/api/medical-records/{patientId}`

Access patient medical records directly by patient ID.

**Query Parameters:**
- `staffId` (optional, default: "STAFF_DEFAULT"): ID of staff accessing the records
- `purpose` (optional, default: "General consultation"): Purpose of accessing records

**Example:**
```bash
curl "http://localhost:8080/api/medical-records/1?staffId=DR-JONES-002&purpose=Follow-up%20visit"
```

**Response:** Same as Scan Card endpoint

#### 3. Add Prescription/Treatment Notes

**POST** `/api/medical-records/prescriptions`

Add new prescription or treatment notes to a patient's medical record.

**Request Body:**
```json
{
  "patientId": 1,
  "staffId": "Dr. Johnson",
  "diagnosis": "Seasonal Allergies",
  "treatment": "Antihistamines and rest",
  "medications": "Cetirizine 10mg once daily",
  "notes": "Review in 2 weeks",
  "followUpDate": "2024-10-30T14:00:00"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/medical-records/prescriptions \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "staffId": "Dr. Johnson",
    "diagnosis": "Seasonal Allergies",
    "treatment": "Antihistamines and rest",
    "medications": "Cetirizine 10mg once daily",
    "notes": "Review in 2 weeks",
    "followUpDate": "2024-10-30T14:00:00"
  }'
```

**Response:** Complete medical record response including the newly added prescription

#### 4. Download Medical Records as PDF

**GET** `/api/medical-records/{patientId}/download`

Download complete medical records as a PDF document.

**Query Parameters:**
- `staffId` (optional, default: "STAFF_DEFAULT"): ID of staff downloading the records
- `purpose` (optional, default: "Patient copy"): Purpose of download

**Example:**
```bash
curl "http://localhost:8080/api/medical-records/1/download?staffId=DR-SMITH-001&purpose=Referral" \
  --output medical_record_patient_1.pdf
```

**Response:**
- **Content-Type:** `application/pdf`
- **Content-Disposition:** `attachment; filename="medical_record_1_20251016_153000.pdf"`
- **Body:** PDF file containing complete medical records

#### 5. Get Access Logs

**GET** `/api/medical-records/{patientId}/access-logs`

Retrieve audit trail of all access attempts for a patient's medical records.

**Example:**
```bash
curl http://localhost:8080/api/medical-records/1/access-logs
```

**Response:**
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

### Medical Records Features

#### Comprehensive Patient Information
The medical records include:
- **Patient Demographics:** Name, contact info, DOB, blood type, address
- **Emergency Contacts:** Name and phone number
- **Medical History:** Chronic conditions and past medical events
- **Allergies:** Known allergies and sensitivities
- **Current Medications:** Active medications with dosage and frequency
- **Prescriptions:** Historical prescriptions with diagnosis and treatment plans
- **Test Results:** Laboratory and diagnostic test results with reference ranges
- **Vaccinations:** Immunization history with batch numbers and next dose dates
- **Previous Visits:** Appointment history with providers and specialties

#### Access Logging
All medical record access is logged with:
- **Access Type:** VIEW, UPDATE, or DOWNLOAD
- **Staff ID:** Identifier of the staff member accessing records
- **Timestamp:** When the access occurred
- **Purpose:** Reason for accessing the records
- **Access Status:** Whether access was granted
- **Denial Reason:** Explanation if access was denied

#### Security and Compliance
- Unique digital health card number for patient identification
- Complete audit trail for regulatory compliance
- All access attempts are logged for security review
- Support for multiple access types (view, update, download)

### Use Cases

#### Scenario 1: Emergency Room Visit
1. Patient arrives at emergency room
2. Staff scans patient's digital health card using `/api/medical-records/scan-card`
3. System displays complete medical history including allergies (critical for emergency care)
4. Doctor reviews current medications to avoid drug interactions
5. All access is logged for audit purposes

#### Scenario 2: Regular Checkup
1. Staff retrieves medical records using `/api/medical-records/{patientId}`
2. Doctor reviews previous visit notes and test results
3. Doctor adds new prescription using `/api/medical-records/prescriptions`
4. Patient requests copy of records for personal use
5. Staff downloads PDF using `/api/medical-records/{patientId}/download`

#### Scenario 3: Audit and Compliance
1. Administrator reviews access logs using `/api/medical-records/{patientId}/access-logs`
2. System shows all staff who accessed the patient's records
3. Each access includes timestamp, staff ID, and purpose
4. Helps ensure compliance with privacy regulations

### Error Scenarios

#### Patient Not Found
```json
{
  "timestamp": "2025-10-16T15:30:00",
  "message": "Patient records not found for card number: INVALID-CARD",
  "status": 500
}
```

#### Invalid Patient ID
```json
{
  "timestamp": "2025-10-16T15:30:00",
  "message": "Patient not found",
  "status": 500
}
```

## Future Enhancements

Potential improvements for production:
- Add authentication and authorization
- Implement notification system (email/SMS)
- Add appointment cancellation/rescheduling
- Integrate with real payment gateway
- Add appointment reminders
- Implement waiting list for fully booked slots
- Add rate limiting
- Implement caching for frequently accessed data
- Add scheduled report generation
- Add report sharing functionality via email
- Implement chart visualizations in reports
- Add comparative analytics (month-over-month, year-over-year)

## License

This project is part of the MediBackend healthcare system.
