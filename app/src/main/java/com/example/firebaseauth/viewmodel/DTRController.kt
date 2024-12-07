import android.util.Log
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
import com.google.android.gms.maps.model.LatLng
import java.util.Date

class DTRController : ViewModel() {


        private val _dtrRecords = MutableStateFlow<List<DTRRecord>>(emptyList())
        val dtrRecords: StateFlow<List<DTRRecord>> = _dtrRecords

        fun fetchDTRRecords(email: String) {
            FirebaseFirestore.getInstance().collection("dtr_records")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val records = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(DTRRecord::class.java)?.takeIf { it.email == email }
                    }
                    _dtrRecords.value = records
                }
                .addOnFailureListener { exception ->
                    Log.e("DTR", "Error fetching DTR records: ${exception.message}")
                }
        }
    }
