package co.sodalabs.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_demo.rvDemoList
import kotlinx.android.synthetic.main.demo_recycler_view_item.view.tvDescription
import kotlinx.android.synthetic.main.demo_recycler_view_item.view.tvLabel

data class DemoViewModel(val name: String, val description: String, val intent: Intent)

class DemoActivity : AppCompatActivity() {

    private val mDisposablesOnCreate = CompositeDisposable()

    private val demoList by lazy {
        listOf(
            DemoViewModel(getString(R.string.demo_label_recyclerview), getString(R.string.demo_desc_recyclerview), Intent(this, StyledRecyclerViewDemo::class.java)),
            DemoViewModel(getString(R.string.demo_label_switch), getString(R.string.demo_desc_switch), Intent(this, StyledSwitchViewDemo::class.java))
        )
    }

    private val demoAdapter by lazy { DemoAdapter(demoList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        rvDemoList.apply {
            adapter = demoAdapter
            layoutManager = LinearLayoutManager(context)
        }

        demoAdapter.clickListener = { _, item ->
            startActivity(item.intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unbind view.
        mDisposablesOnCreate.clear()
    }
}

class DemoAdapter(private val items: List<DemoViewModel>) : RecyclerView.Adapter<DemoAdapter.ViewHolder>() {

    var clickListener: ((view: View, item: DemoViewModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.demo_recycler_view_item, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.setOnClickListener(clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var clickListener: ((view: View, item: DemoViewModel) -> Unit)? = null

        fun bind(viewModel: DemoViewModel) = itemView.run {
            tvLabel.text = viewModel.name
            tvDescription.text = viewModel.description
            setOnClickListener { clickListener?.invoke(itemView, viewModel) }
        }

        fun setOnClickListener(listener: ((view: View, item: DemoViewModel) -> Unit)?) {
            this.clickListener = listener
        }
    }
}
