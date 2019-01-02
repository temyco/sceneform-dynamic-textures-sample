package co.temy.sceneform

import com.google.gson.annotations.SerializedName

data class Mesh(
        @SerializedName("index")
        val meshIndex: Int,
        @SerializedName("name")
        val materialName: String
)