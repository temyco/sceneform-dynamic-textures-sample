package co.temy.sceneform

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ListFragment: Fragment() {
    private lateinit var cubeCardView: CardView
    private lateinit var houseCardView: CardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_fragment, container, false)
        cubeCardView = view.findViewById(R.id.cube_model_view)
        houseCardView = view.findViewById(R.id.converse_model_view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cubeCardView.setOnClickListener {
           setFragment("cube.sfb", DYNAMIC_TEXTURE_TYPE, "textures")
        }

        houseCardView.setOnClickListener {
            setFragment("converse_obj.sfb", SUBMESHES_TYPE, "textures")
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