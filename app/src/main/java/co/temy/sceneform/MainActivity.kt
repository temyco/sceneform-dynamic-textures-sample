package co.temy.sceneform

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = ListFragment.newInstance()

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.root, fragment)
                .addToBackStack("list")
                .commit()
    }
}
