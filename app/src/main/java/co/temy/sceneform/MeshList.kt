package co.temy.sceneform

import com.google.gson.annotations.SerializedName

data class MeshList(
        @SerializedName("submeshes")
        val list: MutableList<Mesh> = mutableListOf()
)