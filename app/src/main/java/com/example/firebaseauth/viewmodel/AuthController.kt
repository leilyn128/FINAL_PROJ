import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauth.model.UserModel
import com.example.firebaseauth.viewmodel.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController
import com.example.firebaseauth.Screen




class AuthController(application: Application) : AndroidViewModel(application) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>(AuthState.Unauthenticated)
    val authState: LiveData<AuthState> get() = _authState
    private val _userDetails = MutableLiveData<UserModel>(UserModel())
    val userDetails: LiveData<UserModel> get() = _userDetails
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> get() = _userRole
    var userState = mutableStateOf(UserModel())

    init {
        checkAuthState()
    }



    private fun checkAuthState() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }


    fun updateUser(field: String, value: String) {
        userState.value = when (field) {
            "email" -> userState.value.copy(email = value)
            "username" -> userState.value.copy(username = value)
            "employeeId" -> userState.value.copy(employeeID = value)
            else -> {
                userState.value
            }
        }
    }

    fun login(email: String?, password: String?, navController: NavController) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            Log.e("AuthViewModel", "Email or password is blank.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userEmail = auth.currentUser?.email ?: ""
                    val role = assignRoleBasedOnEmail(userEmail)

                    // Update authState to reflect authenticated state
                    _authState.value = AuthState.Authenticated(auth.currentUser!!)

                    // Log the successful login and the assigned role
                    Log.d("AuthViewModel", "Login successful for: $userEmail with role: $role")

                    // Immediately navigate based on the role
                    when (role) {
                        "admin" -> {
                            Log.d("AuthViewModel", "Navigating to adminHomePage")
                            navController.navigate(Screen.AdminHomePage.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }

                        "employee" -> {
                            Log.d("AuthViewModel", "Navigating to homePage")
                            navController.navigate(Screen.HomePage.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }

                        else -> {
                            Log.e("AuthViewModel", "Unknown role: $role")
                        }
                    }
                } else {
                    Log.e("AuthViewModel", "Login failed: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Error during login: ${exception.message}")
            }
    }


    fun assignRoleBasedOnEmail(email: String?): String {
        Log.d("AuthController", "Assigning role for email: $email")

        return if (email == "admin10@example.com") { // Replace with the actual admin email
            "admin"
        } else {
            "employee"
        }


    }

    fun signup(
        email: String,
        password: String,
        employeeID: String,
        username: String,
        onSignUpSuccess: () -> Unit,
        onSignUpFailure: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    val user = FirebaseAuth.getInstance().currentUser
                    val userData = mapOf(
                        "employeeID" to employeeID,
                        "username" to username,
                        "email" to email
                    )

                    if (userId != null) {
                        db.collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated(auth.currentUser!!)
                                onSignUpSuccess()
                            }
                            .addOnFailureListener { e ->
                                onSignUpFailure("Failed to save user data: ${e.message}")
                            }
                    }
                } else {
                    onSignUpFailure(
                        task.exception?.message ?: "Unknown error occurred during sign-up."
                    )
                }
            }
            .addOnFailureListener { e ->
                onSignUpFailure("Sign-up failed: ${e.message}")
            }
    }

    fun signOut(navController: NavController) {
        try {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            Log.d("AuthController", "Auth state after logout: ${_authState.value}")
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        } catch (e: Exception) {
            Log.e("AuthController", "Error during logout: ${e.message}")
        }
    }
}