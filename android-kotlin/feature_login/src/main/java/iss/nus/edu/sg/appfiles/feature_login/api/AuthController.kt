package iss.nus.edu.sg.appfiles.feature_login.api

import iss.nus.edu.sg.appfiles.feature_login.data.AuthRequest
import iss.nus.edu.sg.appfiles.feature_login.data.AuthResponse
import iss.nus.edu.sg.appfiles.feature_login.data.MessageResponse
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException
import org.json.JSONObject

@Singleton
class AuthController @Inject constructor(
    private val authApi: AuthApi
) {
    private val usernameSafeRegex = Regex("^[A-Za-z0-9._-]+$")

    // Local validation that mirrors backend login rules
    private fun validateLoginLocally(req: AuthRequest): String? {
        val u = req.username
        val p = req.password
        // (1) Not blank
        if (u.isBlank() || p.isBlank()) return "Parameters cannot be empty"
        // (3) Username length >= 4
        if (u.length < 4) return "Username is too short"
        // (2) Only safe characters
        if (!usernameSafeRegex.matches(u)) return "Username contains illegal characters"
        // (4) Password length >= 8
        if (p.length < 8) return "Password is too short"
        return null // valid
    }

    suspend fun login(request: AuthRequest): Result<AuthResponse> {
        // Local validation first (gives immediate user-friendly message)
        validateLoginLocally(request)?.let { msg ->
            return Result.failure(IllegalStateException(msg))
        }

        return try {
            val resp = authApi.login(request)
            Result.success(resp)

        } catch (e: HttpException) {
            // Try to parse {"message":"..."} from error body (if backend provides it)
            val parsed = try {
                val raw = e.response()?.errorBody()?.string()
                if (!raw.isNullOrBlank())
                    JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
                else null
            } catch (_: Exception) { null }

            // If backend doesn't return a message, map by status code
            val fallback = when (e.code()) {
                401 -> "Login failed: username and password do not match"
                409 -> "Invalid parameters" // generic for 409 when message missing
                else -> "Login failed (${e.code()})"
            }

            Result.failure(IllegalStateException(parsed ?: fallback))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Register unchanged, already working with message body
    suspend fun register(request: AuthRequest): Result<MessageResponse> {
        return try {
            val response = authApi.register(request) // 2xx case
            Result.success(response)
        } catch (e: HttpException) {
            val msg = try {
                val raw = e.response()?.errorBody()?.string()
                if (!raw.isNullOrBlank())
                    JSONObject(raw).optString("message", "Registration failed")
                else
                    "Registration failed"
            } catch (_: Exception) {
                "Registration failed"
            }
            Result.failure(IllegalStateException(msg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}