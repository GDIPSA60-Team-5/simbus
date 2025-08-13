package iss.nus.edu.sg.feature_saveroute

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlan
import iss.nus.edu.sg.feature_saveroute.Data.CommutePlanRequest
import iss.nus.edu.sg.feature_saveroute.Data.toRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommutePlanStorage @Inject constructor(
    private val commutePlanController: CommutePlanController,
    @ApplicationContext private val context: Context
) {

    fun syncCommutePlanToMongoDB(commutePlan: CommutePlan, callback: ((String?) -> Unit)? = null) {
        val commutePlanRequest = commutePlan.toRequest()

        CoroutineScope(Dispatchers.IO).launch {
            commutePlanController.createCommutePlan(commutePlanRequest).fold(
                onSuccess = { serverCommutePlan ->
                    Log.d("MongoDB", "CommutePlan synced with id: ${serverCommutePlan.id}")
                    callback?.invoke(serverCommutePlan.id)
                },
                onFailure = { error ->
                    Log.e("MongoDB", "CommutePlan sync failed: ${error.message}")
                    callback?.invoke(null)
                }
            )
        }
    }

    fun updateCommutePlanOnServer(
        commutePlanId: String,
        commutePlan: CommutePlan,
        callback: ((Boolean) -> Unit)? = null
    ) {
        val commutePlanRequest = commutePlan.toRequest()

        CoroutineScope(Dispatchers.IO).launch {
            commutePlanController.updateCommutePlan(commutePlanId, commutePlanRequest).fold(
                onSuccess = { serverCommutePlan ->
                    Log.d("MongoDB", "CommutePlan updated with id: ${serverCommutePlan.id}")
                    callback?.invoke(true)
                },
                onFailure = { error ->
                    Log.e("MongoDB", "CommutePlan update failed: ${error.message}")
                    callback?.invoke(false)
                }
            )
        }
    }

    fun deleteCommutePlanFromServer(
        commutePlanId: String,
        callback: ((Boolean) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            commutePlanController.deleteCommutePlan(commutePlanId).fold(
                onSuccess = {
                    Log.d("MongoDB", "CommutePlan deleted with id: $commutePlanId")
                    callback?.invoke(true)
                },
                onFailure = { error ->
                    Log.e("MongoDB", "CommutePlan deletion failed: ${error.message}")
                    callback?.invoke(false)
                }
            )
        }
    }
}