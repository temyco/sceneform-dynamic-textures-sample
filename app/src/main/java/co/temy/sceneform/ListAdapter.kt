package co.temy.sceneform

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class ListAdapter internal constructor(context: Context,
                                       private val itemSelectedListener: (modelPath: String, type: Int, textureFolder: String) -> Unit
): RecyclerView.Adapter<ListAdapter.ItemViewHolder>(){

    private val modelList: MutableList<ModelData>
    private val inflater: LayoutInflater
    private val assetManager: AssetManager

    init {
        modelList = ArrayList()
        inflater = LayoutInflater.from(context)
        assetManager = context.assets
    }

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(inflater.inflate(R.layout.model_item, container, false))
    }

    override fun getItemCount(): Int = modelList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(modelList[position], assetManager, itemSelectedListener)
    }

    fun addAll(modelItemCollection: Collection<ModelData>) {
        modelList.addAll(modelItemCollection)
        notifyDataSetChanged()
    }

    fun clear() = modelList.clear()

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val previewImageView: ImageView = itemView.findViewById(R.id.preview_image_view)
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.subtitle_text_view)

        fun bind(modelData: ModelData, assetManager: AssetManager, itemSelectedListener: (modelPath: String, type: Int, textureFolder: String) -> Unit){
            previewImageView.setImageBitmap(BitmapFactory.decodeStream(assetManager.open(modelData.previewImage)))
            titleTextView.text = modelData.title
            subtitleTextView.text = modelData.subtitle
            itemView.setOnClickListener{ itemSelectedListener.invoke(modelData.modelPath, modelData.type, modelData.textureFolder) }
        }
    }
}