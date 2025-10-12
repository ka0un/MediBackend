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
- ✅ Concurrent booking protection (no double-booking)
- ✅ Full CORS support (no authentication required)
- ✅ 21 comprehensive tests (100% passing)

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
- JUnit 5 & Mockito

## Testing

All 21 tests passing:
- 11 unit tests (service layer)
- 9 integration tests (API layer)
- Manual testing verified

## Sample Data

The application initializes with:
- 2 Patients
- 4 Healthcare Providers (2 Government, 2 Private)
- 20 Available Time Slots

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/appointments/providers` | List all providers (optional: filter by specialty) |
| GET | `/api/appointments/timeslots` | Get available time slots for a provider |
| POST | `/api/appointments/book` | Book an appointment |
| POST | `/api/appointments/payment` | Process payment for private appointment |
| GET | `/api/appointments/confirmation/{number}` | Get appointment by confirmation number |
| GET | `/api/appointments/patient/{id}` | Get all appointments for a patient |

## H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:medidb`
- Username: `sa`
- Password: (empty)

## License

Part of the MediBackend healthcare system.
