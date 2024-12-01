import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.model.DTRRecord
import com.example.firebaseauth.model.GeofenceData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

import com.google.android.gms.maps.model.LatLng

class DTRViewModel : ViewModel() {

    private val _geofenceData = MutableLiveData(GeofenceData(LatLng(0.0, 0.0), 0.0))
    val geofenceData: LiveData<GeofenceData> = _geofenceData
    private val db = FirebaseFirestore.getInstance()
    private val _dtrRecords = MutableStateFlow<List<DTRRecord>>(emptyList())
    val dtrRecords: StateFlow<List<DTRRecord>> get() = _dtrRecords

    // Fetch DTR records using the logged-in user's email
    fun fetchDTRRecords(email: String) {
        // Fetch DTR records from Firestore for the logged-in employee using email
        viewModelScope.launch {
            try {
                val snapshot = db.collection("dtr_records")
                    .whereEqualTo("email", email)  // Use email instead of employeeId
                    .get()
                    .await()

                val records = snapshot.documents.map { doc ->
                    val date = doc.getDate("date") ?: Date()
                    val morningArrival = doc.getDate("morningArrival")
                    val morningDeparture = doc.getDate("morningDeparture")
                    val afternoonArrival = doc.getDate("afternoonArrival")
                    val afternoonDeparture = doc.getDate("afternoonDeparture")

                    DTRRecord(
                        email = email,
                        date = date,
                        morningArrival = morningArrival,
                        morningDeparture = morningDeparture,
                        afternoonArrival = afternoonArrival,
                        afternoonDeparture = afternoonDeparture
                    )
                }
                _dtrRecords.value = records
            } catch (e: Exception) {
            }
        }
    }
}
