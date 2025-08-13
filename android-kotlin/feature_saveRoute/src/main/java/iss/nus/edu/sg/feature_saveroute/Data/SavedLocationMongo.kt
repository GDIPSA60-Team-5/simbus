package iss.nus.edu.sg.feature_saveroute.Data

data class SavedLocationMongo(
    val id: String? = null,
    val userId: String,
    val name: String,
    val postalCode: String,
)