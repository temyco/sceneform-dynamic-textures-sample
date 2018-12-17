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
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ar_experience_fragment.*
import java.util.concurrent.CompletableFuture


private const val MIN_OPENGL_VERSION = 3.0
private const val TAG = "ArExperienceFragment"

class ArExperienceFragment: Fragment(){
    private lateinit var arFragment: ArFragment
    private lateinit var colorButton: Button
    private lateinit var metallicButton: Button
    private lateinit var roughnessButton: Button
    private lateinit var normalButton: Button
    private lateinit var resetMaterialButton: Button

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
        val sourcePath: String?
        if (arguments != null){
            sourcePath = arguments?.getString("modelPath")
            if (sourcePath == null){
                Toast.makeText(requireContext(), "model path is null", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressed()
            } else {
                if (sourcePath.contains("converse_obj")){
                    view?.findViewById<LinearLayout>(R.id.buttons_layout)?.visibility = View.GONE
                } else if (sourcePath.contains("cube")){
                    view?.findViewById<LinearLayout>(R.id.buttons_layout)?.visibility = View.VISIBLE
                }
                createRenderable(sourcePath)
            }
        } else {
            Toast.makeText(requireContext(), "You should pass parameters", Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        }

    }

    private fun createRenderable(modelPath: String){
        renderableFuture = ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse(modelPath))
                .build()

        if (modelPath.contains("cube.sfb")){
            materialFuture = CustomMaterial.build(requireContext()) {
                baseColorSource = Uri.parse("textures/cube_diffuse.jpg")
                metallicSource = Uri.parse("textures/cube_metallic.jpg")
                roughnessSource = Uri.parse("textures/cube_roughness.jpg")
                normalSource = Uri.parse("textures/cube_normal.jpg")
            }

            renderableFuture.thenAcceptBoth(materialFuture) { renderableResult, materialResult ->
                customMaterial = materialResult
                renderableModel = renderableResult
                renderableModel.material = customMaterial.value
            }
        }else if (modelPath.contains("converse_obj.sfb")){
            renderableFuture.thenAccept { renderable ->
                renderableModel = renderable

                val meshes = Gson().fromJson(requireContext().assets.open("converse_obj_meshes.json")
                        .bufferedReader(),
                        MeshList::class.java)

                for (i in 0 until renderableModel.submeshCount){
                    val materialName = meshes.list.find { it.meshIndex == i }?.materialName

                    val textureFilename = context?.assets?.list("textures")?.find { it.contains("${materialName}_diffuse") }
                    if (textureFilename != null){
                        CustomMaterial.build(requireContext()) {
                            baseColorSource = Uri.parse("textures/$textureFilename")
                            metallicSource = Uri.parse("")
                            roughnessSource = Uri.parse("")
                            normalSource = Uri.parse("")
                        }.thenAccept { meshMaterial  ->
                            meshMaterial.switchBaseColor()
                            renderableModel.setMaterial(i, meshMaterial.value)
                        }
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
        fun newInstance(modelPath: String): ArExperienceFragment{
            val bundle = Bundle()
            bundle.putString("modelPath", modelPath)

            val fragment = ArExperienceFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}