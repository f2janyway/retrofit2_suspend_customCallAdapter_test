package com.box.retrofit_custom

import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.box.retrofit_custom.Service.Companion.service
import com.box.retrofit_custom.databinding.ActivityMainBinding
import com.box.retrofit_custom.databinding.GitItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val mAdpater by lazy { SuspendAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.suspendRecyclerview.adapter = mAdpater

        binding.searchEdit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.searchButton.setOnClickListener {
            if (binding.searchEdit.text.isNotBlank()) {
                search(binding.searchEdit.text.toString())
            }
        }
        GlobalScope.launch(Dispatchers.Main) {
            binding.apply {
                delay(500)
                searchEdit.text = SpannableStringBuilder("f2janyway")
                searchButton.performClick()
            }
        }
    }

    private fun search(q: String) {
        lifecycleScope.launch {
            when (val rs = service.create(Service::class.java).getUser(q)) {
                is NetworkResponse.Success -> {
                    val list = rs.body
                    mAdpater.setList(list)
                }
                is NetworkResponse.ApiError -> Log.e("Main", "search: api error")
                is NetworkResponse.NetworkError -> Log.e("Main", "search: network error")
                is NetworkResponse.UnknownError -> Log.e("Main", "search: UnknownError")
            }
        }

    }

    class SuspendAdapter : RecyclerView.Adapter<SuspendAdapter.VH>() {
        private val mlist = ArrayList<Git>()

        class VH(val binding: GitItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(git: Git, position: Int) {
                binding.apply {
                    gitRepoName.text = git.name
                    gitUrl.text = git.full_name
                }
            }
            private fun animRipple(view: ConstraintLayout) {
                val anim = AnimationUtils.loadAnimation(view.context,R.anim.anim_ripple_test)
                view.startAnimation(anim)
            }
        }


        fun setList(list: List<Git>) {
            mlist.clear()
            mlist.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuspendAdapter.VH {
            val binding = GitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: SuspendAdapter.VH, position: Int) {
            holder.bind(mlist[position], position)
        }

        override fun getItemCount(): Int {
            return mlist.count()
        }
    }
}