# JWT Role-Based Authentication with Google reCaptcha v2

This document describes the implementation of JWT role-based authentication with Google reCaptcha v2 integration in the Anti-Money Laundering (AML) application.

## Features Implemented

### 1. JWT Authentication System
- **JWT Token Generation**: Secure token generation with configurable expiration
- **Role-Based Access Control**: Admin and Customer roles with different permissions
- **Password Encryption**: BCrypt password hashing for secure storage
- **Token Validation**: Automatic token validation on protected endpoints

### 2. Google reCaptcha v2 Integration
- **reCaptcha Verification**: Server-side verification of reCaptcha responses
- **Registration Protection**: reCaptcha required during user registration
- **Configurable**: Easy configuration through application properties

### 3. Role-Based Security
- **Admin Role**: Full access to all endpoints including admin dashboard
- **Customer Role**: Limited access to customer-specific endpoints
- **Method-Level Security**: `@PreAuthorize` annotations for fine-grained control

### 4. Document-Based Identity Verification
- **Aadhaar and PAN**: These will be extracted from uploaded documents
- **No Manual Entry**: Users don't need to manually enter Aadhaar/PAN numbers
- **Document Processing**: Identity documents will be processed to extract required information

### 5. Integrated OTP and reCaptcha Flow
- **Two-Step Login**: Login requires both OTP and reCaptcha verification
- **Three-Step Registration**: Registration includes OTP verification and reCaptcha
- **Enhanced Security**: Multiple layers of verification for both login and registration
- **User-Friendly**: Step-by-step process with clear indicators

## API Endpoints

### Authentication Endpoints
- `POST /api/auth/login/init` - Initialize login (credentials + reCaptcha)
- `POST /api/auth/login/verify` - Complete login (OTP + reCaptcha)
- `POST /api/auth/login` - Legacy login endpoint
- `POST /api/register` - User registration with reCaptcha
- `POST /api/register/send-otp` - Send OTP for email verification
- `POST /api/register/verify-otp` - Verify OTP

### Admin Endpoints (Requires ADMIN role)
- `GET /api/admin/customers` - Get all customers
- `GET /api/admin/customers/{id}` - Get customer by ID
- `PUT /api/admin/customers/{id}/kyc-status` - Update KYC status
- `GET /api/admin/dashboard` - Admin dashboard with statistics

### Customer Endpoints (Requires CUSTOMER or ADMIN role)
- `GET /api/customer/profile` - Get user profile
- `GET /api/customer/kyc-status` - Get KYC status

## Configuration

### Application Properties
```properties
# JWT Configuration
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000

# Google reCaptcha Configuration
recaptcha.secret=YOUR_RECAPTCHA_SECRET_KEY
recaptcha.url=https://www.google.com/recaptcha/api/siteverify

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/aml_database?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

### Google reCaptcha Setup
1. Go to [Google reCaptcha Admin Console](https://www.google.com/recaptcha/admin)
2. Create a new site with reCaptcha v2
3. Get your Site Key and Secret Key
4. Update `recaptcha.secret` in application.properties
5. Update the Site Key in the HTML files

## Usage Examples

### Registration Request
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "role": "CUSTOMER",
  "street": "123 Main St",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "postalCode": "400001",
  "recaptchaToken": "reCaptcha_response_token"
}
```

### Login Init Request
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "recaptchaToken": "reCaptcha_response_token"
}
```

### Login Verify Request
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "otp": "123456",
  "recaptchaToken": "reCaptcha_response_token"
}
```

### Authentication Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john.doe@example.com",
  "role": "CUSTOMER",
  "userId": 1
}
```

## Security Features

### JWT Security
- **Secret Key**: Configurable secret key for token signing
- **Expiration**: Configurable token expiration time
- **Role Claims**: User roles embedded in JWT tokens
- **Token Validation**: Automatic validation on each request

### Password Security
- **BCrypt Hashing**: Industry-standard password hashing
- **Salt Generation**: Automatic salt generation for each password
- **Minimum Length**: 8-character minimum password requirement

### reCaptcha Security
- **Server-Side Verification**: All reCaptcha responses verified server-side
- **Registration Protection**: reCaptcha required for all registrations
- **Bot Protection**: Prevents automated registration attempts

## Frontend Integration

### HTML Pages
- `login.html` - User login page
- `register.html` - User registration page with reCaptcha

### JavaScript Integration
```javascript
// Login example
const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
});

// API call with JWT token
const response = await fetch('/api/customer/profile', {
    headers: { 'Authorization': 'Bearer ' + token }
});
```

## Database Schema Updates

### Customer Entity Updates
- Added `password` field with BCrypt hashing
- Added `role` field with enum values (ADMIN, CUSTOMER)
- Maintained existing KYC and document management features

### Repository Updates
- Added `countByKycStatus()` method for admin dashboard
- Maintained existing customer lookup methods

## Testing

### Manual Testing
1. Start the application
2. Navigate to `http://localhost:8080/register.html`
3. Complete registration with reCaptcha
4. Login using `http://localhost:8080/login.html`
5. Test protected endpoints with JWT token

### API Testing
Use tools like Postman or curl to test the API endpoints:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Access protected endpoint
curl -X GET http://localhost:8080/api/customer/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Security Considerations

1. **JWT Secret**: Use a strong, randomly generated secret key in production
2. **HTTPS**: Always use HTTPS in production environments
3. **reCaptcha Keys**: Keep reCaptcha secret keys secure
4. **Password Policy**: Implement strong password policies
5. **Token Expiration**: Set appropriate token expiration times
6. **Rate Limiting**: Consider implementing rate limiting for authentication endpoints

## Dependencies Added

```xml
<!-- JWT Dependencies (already present) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- WebFlux for reCaptcha HTTP calls -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

This implementation provides a complete JWT-based authentication system with role-based access control and Google reCaptcha v2 integration for secure user registration.
