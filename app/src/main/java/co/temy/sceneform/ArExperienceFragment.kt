package co.temy.sceneform

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.Gson
import java.util.concurrent.CompletableFuture


private const val MIN_OPENGL_VERSION = 3.0
private const val TAG = "ArExperienceFragment"

const val DYNAMIC_TEXTURE_TYPE = 1
const val SUBMESHES_TYPE = 2

class ArExperienceFragment: Fragment(){
    private lateinit var arFragment: ArFragment
    private lateinit var colorButton: Button
    private lateinit var metallicButton: Button
    private lateinit var roughnessButton: Button
    private lateinit var normalButton: Button
    private lateinit var resetMaterialButton: Button

    private var sourcePath: String? = null
    private var modelType: Int = 0
    private var textureFolder: String? = null

    private lateinit var renderableModel: ModelRenderable

    private lateinit var renderableFuture: CompletableFuture<ModelRenderable>
    private lateinit var materialFuture: CompletableFuture<CustomMaterial>

    private lateinit var customMaterial: CustomMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(requireActivity()))
            return
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.ar_experience_fragment, container, false)

        arFragment = childFragmentManager.findFragmentById(R.id.arFragmentView) as ArFragment
        colorButton = view.findViewById(R.id.colorButton)
        metallicButton = view.findViewById(R.id.metallicButton)
        roughnessButton = view.findViewById(R.id.roughnessButton)
        normalButton = view.findViewById(R.id.normalButton)
        resetMaterialButton = view.findViewById(R.id.resetMaterialButton)

        colorButton.setText(R.string.set_color)
        metallicButton.setText(R.string.set_metallic)
        roughnessButton.setText(R.string.set_roughness)
        normalButton.setText(R.string.set_normal)

        colorButton.setOnClickListener {
            customMaterial.switchBaseColor()
            if (customMaterial.isDefaultBaseColorMap) {
                colorButton.setText(R.string.set_color)
            } else
                colorButton.setText(R.string.reset_color)
        }

        metallicButton.setOnClickListener {
            customMaterial.switchMetallic()
            if (customMaterial.isDefaultMetallicMap)
                metallicButton.setText(R.string.set_metallic)
            else
                metallicButton.setText(R.string.reset_metallic)
        }

        roughnessButton.setOnClickListener {
            customMaterial.switchRoughness()
            if (customMaterial.isDefaultRoughnessMap)
                roughnessButton.setText(R.string.set_roughness)
            else
                roughnessButton.setText(R.string.reset_roughness)
        }

        normalButton.setOnClickListener {
            customMaterial.switchNormal()
            if (customMaterial.isDefaultNormalMap)
                normalButton.setText(R.string.set_normal)
            else
                normalButton.setText(R.string.reset_normal)
        }

        resetMaterialButton.setOnClickListener {
            customMaterial.reset()
            colorButton.setText(R.string.set_color)
            metallicButton.setText(R.string.set_metallic)
            roughnessButton.setText(R.string.set_roughness)
            normalButton.setText(R.string.set_normal)
        }

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _: Plane, _: MotionEvent ->
            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val modelNode = TransformableNode(arFragment.transformationSystem)
            modelNode.setParent(anchorNode)
            modelNode.renderable = renderableModel
            modelNode.select()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (arguments != null){
            getArguments(arguments!!)
            when {
                sourcePath == null -> {
                    Toast.makeText(requireContext(), "model path is unspecified", Toast.LENGTH_LONG).show()
                }
                textureFolder == null -> {
                    Toast.makeText(requireContext(), "textures folder is unspecified", Toast.LENGTH_LONG).show()
                }
                modelType != DYNAMIC_TEXTURE_TYPE && modelType != SUBMESHES_TYPE ->{
                    Toast.makeText(requireContext(), "model type is inappropriate", Toast.LENGTH_LONG).show()
                }
                else -> {
                    if (modelType == SUBMESHES_TYPE)
                        view?.findViewById<LinearLayout>(R.id.buttons_layout)?.visibility = View.GONE
                    else {
                        view?.findViewById<LinearLayout>(R.id.buttons_layout)?.visibility = View.VISIBLE
                    }
                    createRenderable(sourcePath!!, modelType, textureFolder!!)
                }
            }
        } else {
            Toast.makeText(requireContext(), "You should pass parameters", Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        }

    }

    private fun createRenderable(modelPath: String, modelType: Int, textureFolder: String){
        val modelName = modelPath.substringAfterLast("/").substringBeforeLast(".")

        renderableFuture = ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse(modelPath))
                .build()

        when (modelType){
            DYNAMIC_TEXTURE_TYPE -> {
                createDynamicTextureMaterial(modelName, textureFolder)
            }
            SUBMESHES_TYPE -> {
                createSubmeshMaterials(modelName, textureFolder)
            }
            else -> {
                Toast.makeText(requireContext(), "pass correct modelType value", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun getArguments(bundle: Bundle){
        sourcePath = bundle.getString("modelPath")
        modelType = bundle.getInt("modelType")
        textureFolder = bundle.getString("textureAssetFolder")
    }

    private fun createDynamicTextureMaterial(modelName: String, textureFolder: String){
        val textureList = requireContext().assets.list(textureFolder)?. filter { it.contains(modelName) }
        if (textureList != null){
            materialFuture = CustomMaterial.build(requireContext()) {
                baseColorSource = createUri(textureList, textureFolder, "_diffuse")
                metallicSource = createUri(textureList, textureFolder, "_metallic")
                roughnessSource = createUri(textureList, textureFolder, "_roughness")
                normalSource = createUri(textureList, textureFolder, "_normal")
            }
        }
        renderableFuture.thenAcceptBoth(materialFuture) { renderableResult, materialResult ->
            customMaterial = materialResult
            renderableModel = renderableResult
            renderableModel.material = customMaterial.value
        }
    }

    private fun createSubmeshMaterials(modelName: String, textureFolder: String){
        renderableFuture.thenAccept { renderable ->
            renderableModel = renderable

            val meshes = Gson().fromJson(requireContext().assets.open("${modelName}_meshes.json")
                    .bufferedReader(),
                    MeshList::class.java)

            for (i in 0 until renderableModel.submeshCount){
                val materialName = meshes.list.find { it.meshIndex == i }?.materialName

                val textureList: List<String>? = if (!materialName.isNullOrBlank()) {
                    requireContext().assets.list(textureFolder)?.filter { it.contains(materialName) }
                } else {
                    null
                }

                if (!textureList.isNullOrEmpty()){
                    CustomMaterial.build(requireContext()) {
                        baseColorSource = createUri(textureList, textureFolder, "_diffuse")
                        metallicSource = createUri(textureList, textureFolder, "_metallic")
                        roughnessSource = createUri(textureList, textureFolder, "_roughness")
                        normalSource = createUri(textureList, textureFolder, "_normal")
                    }.thenAccept { meshMaterial  ->
                        meshMaterial.switchBaseColor()
                        renderableModel.setMaterial(i, meshMaterial.value)
                    }
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(requireContext(), "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        val openGlVersionString: String = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        return true
    }

    companion object {
        fun newInstance(modelPath: String, modelType: Int, textureAssetFolder: String): ArExperienceFragment{
            val bundle = Bundle()
            bundle.putString("modelPath", modelPath)
            bundle.putInt("modelType", modelType)
            bundle.putString("textureAssetFolder", textureAssetFolder)
            val fragment = ArExperienceFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}