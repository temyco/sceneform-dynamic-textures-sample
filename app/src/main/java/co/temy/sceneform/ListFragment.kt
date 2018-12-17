package co.temy.sceneform

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ListFragment: Fragment() {
    private val cubeCardView: CardView? by lazy { view?.findViewById<CardView>(R.id.cube_model_view) }
    private val houseCardView: CardView? by lazy { view?.findViewById<CardView>(R.id.converse_model_view) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cubeCardView?.setOnClickListener {
           setFragment("cube.sfb")
        }

        houseCardView?.setOnClickListener {
            setFragment("converse_obj.sfb")
        }
    }

    private fun setFragment(sourceString: String){
        val fragment = ArExperienceFragment.newInstance(sourceString)
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