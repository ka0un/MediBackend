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
- ✅ Full CORS support (no authentication required)
- ✅ 72 comprehensive tests (100% passing)

## Example Usage

```bash
# Get all providers
curl http://localhost:8080/api/appointments/providers

# Book an appointment
curl -X POST http://localhost:8080/api/appointments/book \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"providerId":1,"timeSlotId":1}'
```

## Documentation

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference and detailed usage examples.

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- Spring Data JPA
- H2 Database
- Lombok
- iText 7 (PDF generation)
- JUnit 5 & Mockito

## Testing

All 72 tests passing:
- 35 unit tests (service layer - AppointmentService: 11, PatientService: 11, ReportService: 13)
- 36 integration tests (API layer - AppointmentController: 9, PatientController: 11, ReportController: 16)
- Manual testing verified

## Sample Data

The application initializes with:
- 2 Patients
- 4 Healthcare Providers (2 Government, 2 Private)
- 20 Available Time Slots

## API Endpoints

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

## H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:medidb`
- Username: `sa`
- Password: (empty)

## License

Part of the MediBackend healthcare system.
