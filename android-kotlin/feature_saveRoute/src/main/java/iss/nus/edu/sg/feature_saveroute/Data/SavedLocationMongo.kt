package iss.nus.edu.sg.feature_saveroute.Data

data class SavedLocationMongo(
    val id: String? = null,
    val deviceId: String,
    val name: String,
    val postalCode: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)