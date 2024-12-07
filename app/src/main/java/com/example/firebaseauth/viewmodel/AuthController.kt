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
import androidx.compose.runtime.State
import androidx.navigation.NavController
import com.example.firebaseauth.Screen



class AuthController(application: Application) : AndroidViewModel(application) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>(AuthState.Unauthenticated)
    val authState: LiveData<AuthState> get() = _authState

    private val _userDetails = MutableLiveData<UserModel>(UserModel())
    val userDetails: LiveData<UserModel> get() = _userDetails

    private val _userModel = mutableStateOf(UserModel())
    val userModel: State<UserModel> = _userModel

    val user = FirebaseAuth.getInstance().currentUser
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> get() = _userRole


    val authStatus: MutableLiveData<AuthState.AuthResult> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    val isAuthenticated: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    init {
        checkAuthState()
    }

    var userState = mutableStateOf(UserModel())

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

                    _authState.value = AuthState.Authenticated(auth.currentUser!!)

                    when (role) {
                        "admin" -> {
                            navController.navigate(Screen.AdminHomePage.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "employee" -> {
                            navController.navigate(Screen.HomePage.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }

                        }

                }

            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Error during login: ${exception.message}")
            }
    }


    fun assignRoleBasedOnEmail(email: String?): String {
        return if (email == "admin10@example.com") {
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
                    onSignUpFailure(task.exception?.message ?: "Unknown error occurred during sign-up.")
                }
            }
            .addOnFailureListener { e ->
                onSignUpFailure("Sign-up failed: ${e.message}")
            }
    }

        fun signOut() {
            try {
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
                authStatus.value = AuthState.AuthResult.LoggedOut
            } catch (e: Exception) {
                errorMessage.value = "Error logging out: ${e.message}"
            }
        }
}


