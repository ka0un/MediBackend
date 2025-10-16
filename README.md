# MediBackend - Healthcare Appointment Booking System

A Spring Boot REST API for booking healthcare appointments, supporting both government (free) and private (paid) healthcare providers.

## Quick Start

```bash
# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The API will be available at `http://localhost:8080`

## Key Features

- ✅ Browse healthcare providers by specialty
- ✅ View real-time available time slots
- ✅ Book appointments (free for government, paid for private hospitals)
- ✅ Process payments for private appointments
- ✅ Manage patient accounts (create, read, update, delete)
- ✅ Audit logging for all patient account changes
- ✅ Concurrent booking protection (no double-booking)
- ✅ Generate statistical reports with KPIs and analytics
- ✅ Export reports as PDF or CSV
- ✅ Filter reports by hospital, department, and date range
- ✅ **NEW: Access comprehensive medical records by digital health card**
- ✅ **NEW: View medications, prescriptions, test results, and vaccinations**
- ✅ **NEW: Add prescriptions and treatment notes**
- ✅ **NEW: Download medical records as PDF**
- ✅ **NEW: Complete audit trail of medical record access**
- ✅ **NEW: User authentication with admin and patient roles**
- ✅ **NEW: Patient registration system**
- ✅ **NEW: Admin dashboard with full management capabilities**
- ✅ **NEW: Healthcare provider management (add, edit, remove providers)**
- ✅ **NEW: Time slot management for healthcare providers**
- ✅ Full CORS support (no authentication required)
- ✅ 139 comprehensive tests (100% passing)

## Example Usage

```bash
# Register a new patient
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"patient1","password":"password123","name":"John Doe","email":"john@test.com","phone":"1234567890","digitalHealthCardNumber":"DHC-001"}'

# Login as admin (⚠️ CHANGE DEFAULT PASSWORD IN PRODUCTION!)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Login as patient (⚠️ These are test credentials for development only)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"password123"}'

# Get admin dashboard
curl http://localhost:8080/api/admin/dashboard

# Get all providers
curl http://localhost:8080/api/appointments/providers

# Book an appointment
curl -X POST http://localhost:8080/api/appointments/book \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"providerId":1,"timeSlotId":1}'
```

## Documentation

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference and detailed usage examples.

See [FRONTEND_CHANGES.md](FRONTEND_CHANGES.md) for comprehensive frontend implementation guide including all new authentication endpoints and UI requirements.

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- Spring Data JPA
- H2 Database
- Lombok
- iText 7 (PDF generation)
- JUnit 5 & Mockito

## Testing

All 139 tests passing:
- 57 unit tests (service layer)
  - AppointmentService: 11 tests
  - PatientService: 11 tests
  - ReportService: 12 tests
  - MedicalRecordService: 10 tests
  - AuthService: 13 tests
- 81 integration tests (API layer)
  - AppointmentController: 9 tests
  - PatientController: 11 tests
  - ReportController: 15 tests
  - MedicalRecordController: 14 tests
  - AuthController: 8 tests
  - AdminController: 24 tests (14 new provider/timeslot management tests)
- 1 application context test
- Manual testing verified

## Sample Data

The application initializes with:
- 1 Admin User (username: admin, password: admin)
- 2 Patient Users (john.doe/password123, jane.smith/password123)
- 2 Patients with complete medical records
- 4 Healthcare Providers (2 Government, 2 Private)
- 20 Available Time Slots

## API Endpoints

### Authentication (NEW)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new patient account |
| POST | `/api/auth/login` | Login (admin or patient) |
| POST | `/api/auth/logout` | Logout |

### Admin Dashboard (NEW)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | Get admin dashboard with stats and recent data |
| GET | `/api/admin/patients` | Get all registered patients |
| GET | `/api/admin/appointments` | Get all appointments |
| DELETE | `/api/admin/patients/{id}` | Delete a patient account |
| DELETE | `/api/admin/appointments/{id}` | Cancel an appointment |
| POST | `/api/admin/providers` | Create a new healthcare provider |
| GET | `/api/admin/providers/{id}` | Get provider details |
| PUT | `/api/admin/providers/{id}` | Update provider information |
| DELETE | `/api/admin/providers/{id}` | Delete a provider |
| POST | `/api/admin/providers/{id}/timeslots` | Create time slot for provider |
| GET | `/api/admin/providers/{id}/timeslots` | Get all time slots for provider |
| PUT | `/api/admin/timeslots/{id}` | Update a time slot |
| DELETE | `/api/admin/timeslots/{id}` | Delete a time slot |

### Appointment Management (UC-01)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/appointments/providers` | List all providers (optional: filter by specialty) |
| GET | `/api/appointments/timeslots` | Get available time slots for a provider |
| POST | `/api/appointments/book` | Book an appointment |
| POST | `/api/appointments/payment` | Process payment for private appointment |
| GET | `/api/appointments/confirmation/{number}` | Get appointment by confirmation number |
| GET | `/api/appointments/patient/{id}` | Get all appointments for a patient |

### Patient Account Management (UC-02)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/patients` | Create a new patient account |
| GET | `/api/patients/{id}` | Get patient profile details |
| GET | `/api/patients` | Get all patient profiles |
| PUT | `/api/patients/{id}` | Update patient profile |
| DELETE | `/api/patients/{id}` | Delete patient account |

### Statistical Reports (UC-03)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports` | Generate statistical report with filters (hospital, department, date range) |
| POST | `/api/reports/export` | Export report as PDF or CSV |

### Medical Records Access (UC-04)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/medical-records/scan-card` | Access medical records by scanning digital health card |
| GET | `/api/medical-records/{id}` | Get comprehensive medical records by patient ID |
| POST | `/api/medical-records/prescriptions` | Add new prescription/treatment notes |
| GET | `/api/medical-records/{id}/download` | Download medical records as PDF |
| GET | `/api/medical-records/{id}/access-logs` | Get audit trail of medical record access |

## H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:medidb`
- Username: `sa`
- Password: (empty)

## License

Part of the MediBackend healthcare system.
