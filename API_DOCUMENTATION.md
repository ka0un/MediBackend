# MediBackend - Appointment Booking System

## Overview
This is a Spring Boot application implementing UC-01: Book Appointment use case for a healthcare appointment booking system. The system supports booking appointments with healthcare providers at both government hospitals (free) and private hospitals (requiring payment).

## Features Implemented

### Core Functionality
- Browse healthcare providers by specialty
- View real-time availability of time slots
- Book appointments (free for government hospitals, paid for private hospitals)
- Process payments for private hospital appointments
- View appointment confirmation and details
- Concurrent booking protection (prevents double-booking of time slots)

### Technical Features
- RESTful API with Spring Boot
- H2 in-memory database
- JPA/Hibernate for data persistence
- Lombok for reducing boilerplate code
- CORS enabled for all origins
- No authentication required (as per requirements)
- Comprehensive unit and integration tests

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

## Future Enhancements

Potential improvements for production:
- Add authentication and authorization
- Implement notification system (email/SMS)
- Add appointment cancellation/rescheduling
- Integrate with real payment gateway
- Add appointment reminders
- Implement waiting list for fully booked slots
- Add audit logging
- Add rate limiting
- Implement caching for frequently accessed data

## License

This project is part of the MediBackend healthcare system.
