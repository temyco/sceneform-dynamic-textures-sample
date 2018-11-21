package co.temy.sceneform

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.utilities.AndroidPreconditions
import java.util.concurrent.CompletableFuture

class CustomMaterial private constructor(
    val value: Material,
    private val baseColorMap: Texture?,
    private val metallicMap: Texture?,
    private val roughnessMap: Texture?,
    private val normalMap: Texture?,
    private val defaultBaseColorMap: Texture?,
    private val defaultMetallicMap: Texture?,
    private val defaultRoughnessMap: Texture?,
    private val defaultNormalMap: Texture?
) {
    companion object {
        inline fun build(context: Context, block: Builder.() -> Unit) = Builder(
            context
        ).apply(block).build()
    }

    var isDefaultBaseColorMap: Boolean
        private set
    var isDefaultMetallicMap: Boolean
        private set
    var isDefaultRoughnessMap: Boolean
        private set
    var isDefaultNormalMap: Boolean
        private set

    init {
        isDefaultBaseColorMap = true
        isDefaultMetallicMap = true
        isDefaultRoughnessMap = true
        isDefaultNormalMap = true
    }

    private fun setBaseColorMap(texture: Texture?) {
        if (texture != null) {
            value.setTexture("baseColorMap", texture)
        }
    }

    private fun setMetallicMap(texture: Texture?) {
        if (texture != null) {
            value.setTexture("metallicMap", texture)
        }
    }

    private fun setRoughnessMap(texture: Texture?) {
        if (texture != null) {
            value.setTexture("roughnessMap", texture)
        }
    }

    private fun setNormalMap(texture: Texture?) {
        if (texture != null) {
            value.setTexture("normalMap", texture)
        }
    }

    fun switchBaseColor() {
        isDefaultBaseColorMap = if (isDefaultBaseColorMap) {
            setBaseColorMap(baseColorMap)
            false
        } else {
            setBaseColorMap(defaultBaseColorMap)
            true
        }
    }

    fun switchMetallic() {
        isDefaultMetallicMap = if (isDefaultMetallicMap) {
            setMetallicMap(metallicMap)
            false
        } else {
            setMetallicMap(defaultMetallicMap)
            true
        }
    }

    fun switchRoughness() {
        isDefaultRoughnessMap = if (isDefaultRoughnessMap) {
            setRoughnessMap(roughnessMap)
            false
        } else {
            setRoughnessMap(defaultRoughnessMap)
            true
        }
    }

    fun switchNormal() {
        isDefaultNormalMap = if (isDefaultNormalMap) {
            setNormalMap(normalMap)
            false
        } else {
            setNormalMap(defaultNormalMap)
            true
        }
    }

    fun reset() {
        isDefaultBaseColorMap = true
        isDefaultMetallicMap = true
        isDefaultRoughnessMap = true
        isDefaultNormalMap = true
        setBaseColorMap(defaultBaseColorMap)
        setMetallicMap(defaultMetallicMap)
        setRoughnessMap(defaultRoughnessMap)
        setNormalMap(defaultNormalMap)
    }

    class Builder(val context: Context) {
        var baseColorSource: Uri? = null
        var metallicSource: Uri? = null
        var roughnessSource: Uri? = null
        var normalSource: Uri? = null

        fun build(): CompletableFuture<CustomMaterial> {
            AndroidPreconditions.checkUiThread()

            val renderableFuture = createMaterialRenderable()
            val defaultColorFuture = createTexture(R.raw.custom_material_default_diffuse, Texture.Usage.COLOR)
            val defaultMetallicFuture = createTexture(R.raw.custom_material_default_metallic, Texture.Usage.DATA)
            val defaultRoughnessFuture = createTexture(R.raw.custom_material_default_roughness, Texture.Usage.DATA)
            val defaultNormalFuture = createTexture(R.raw.custom_material_default_normal, Texture.Usage.NORMAL)
            val baseColorFuture = createTexture(baseColorSource!!, Texture.Usage.COLOR)
            val metallicFuture = createTexture(metallicSource!!, Texture.Usage.DATA)
            val roughnessFuture = createTexture(roughnessSource!!, Texture.Usage.DATA)
            val normalFuture = createTexture(normalSource!!, Texture.Usage.NORMAL)

            return CompletableFuture.allOf(
                renderableFuture,
                baseColorFuture,
                metallicFuture,
                roughnessFuture,
                normalFuture,
                defaultColorFuture,
                defaultMetallicFuture,
                defaultRoughnessFuture,
                defaultNormalFuture
            ).thenApplyAsync {
                CustomMaterial(
                    renderableFuture.get().material,
                    baseColorFuture.get(),
                    metallicFuture.get(),
                    roughnessFuture.get(),
                    normalFuture.get(),
                    defaultColorFuture.get(),
                    defaultMetallicFuture.get(),
                    defaultRoughnessFuture.get(),
                    defaultNormalFuture.get()
                )
            }.exceptionally { ex ->
                Log.e(TAG, "Unable to create CustomMaterial", ex)
                null
            }
        }

        private fun createMaterialRenderable(): CompletableFuture<ModelRenderable> = ModelRenderable.builder()
            .setSource(context, R.raw.custom_material)
            .build()
            .exceptionally { ex ->
                Log.e(TAG, "unable to load custom_material renderable", ex)
                null
            }

        private fun createTexture(sourceUri: Uri, usage: Texture.Usage): CompletableFuture<Texture> =
            Texture.builder()
                .setSource(context, sourceUri)
                .setUsage(usage)
                .setSampler(
                    Texture.Sampler.builder()
                        .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                        .setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                        .build()
                ).build()
                .exceptionally { ex ->
                    Log.e(TAG, "Unable to load texture from $sourceUri", ex)
                    null
                }

        private fun createTexture(id: Int, usage: Texture.Usage): CompletableFuture<Texture> = Texture.builder()
            .setSource(context, id)
            .setUsage(usage)
            .setSampler(
                Texture.Sampler.builder()
                    .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                    .setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                    .build()
            ).build()
    }
}