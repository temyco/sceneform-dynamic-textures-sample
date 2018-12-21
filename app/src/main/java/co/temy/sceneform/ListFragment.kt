package co.temy.sceneform

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class ListFragment: Fragment() {
    lateinit var recyclerView: RecyclerView

    private val adapter: ListAdapter by lazy {
        ListAdapter(requireContext()) { modelPath, type, textureFolder-> setFragment(modelPath, type, textureFolder)}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_fragment, container, false)
        recyclerView = view.findViewById(R.id.list_recycler_view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        recyclerView.apply {
            this@ListFragment.adapter.clear()
            this@ListFragment.adapter.addAll(listOf(
                ModelData("cube.sfb", DYNAMIC_TEXTURE_TYPE, "textures", "cube_image.jpg", "Cube", "Model with dynamic textures"),
                ModelData("converse_obj.sfb", SUBMESHES_TYPE, "textures","converse_image.jpg", "Converse", "Model with submeshes")
        ))
            adapter = this@ListFragment.adapter
        }
    }

    private fun setFragment(sourceString: String, modelType: Int, textureAssetFolder: String){
        val fragment = ArExperienceFragment.newInstance(sourceString, modelType, textureAssetFolder)
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.root, fragment)
                ?.addToBackStack("arExp")
                ?.commit()
    }

    companion object {
        fun newInstance(): ListFragment{
            return ListFragment()
        }
    }
}