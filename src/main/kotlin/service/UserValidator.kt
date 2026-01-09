package service

import java.time.LocalDate
import java.time.Period

/**
 * Validates user creation requests.
 *
 * Rules: firstname, surname, email must be non-blank;
 * user must meet minimum age requirement if dateOfBirth provided.
 *
 * @param minimumAge Required age threshold (default: 21)
 */
class UserValidator(
    private val minimumAge: Int = 21
) {
    /** @return ValidationError if invalid, null if valid */
    fun validate(request: AddUserRequest): AddUserResult.ValidationError? {
        if (request.firstname.isBlank()) {
            return AddUserResult.ValidationError("Firstname is required")
        }

        if (request.surname.isBlank()) {
            return AddUserResult.ValidationError("Surname is required")
        }

        if (request.email.isBlank()) {
            return AddUserResult.ValidationError("Email is required")
        }

        if (request.dateOfBirth != null) {
            val age = Period.between(request.dateOfBirth, LocalDate.now()).years
            if (age < minimumAge) {
                return AddUserResult.ValidationError("User must be at least $minimumAge years old")
            }
        }

        return null // Valid
    }
}
