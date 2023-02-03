package com.example.biophonie.ui.activities

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.biophonie.BiophonieApplication
import com.example.biophonie.R
import com.example.biophonie.databinding.ActivityTutorialBinding
import com.example.biophonie.ui.fragments.NameFragment
import com.example.biophonie.ui.fragments.TutorialFragment
import com.example.biophonie.viewmodels.TutorialViewModel
import com.google.android.material.tabs.TabLayoutMediator


private const val NUM_PAGES = 3

class TutorialActivity : FragmentActivity(), ViewTreeObserver.OnGlobalLayoutListener {

    private val viewModel: TutorialViewModel by lazy {
        ViewModelProvider(this, TutorialViewModel.ViewModelFactory((application as BiophonieApplication).tutorialRepository)).get(TutorialViewModel::class.java)
    }
    private var keyboardShown = false
    private lateinit var binding: ActivityTutorialBinding
    private val adapter = TutorialPagerAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial)

        setUpViewPager()
        setUpListeners()
        setUpDataObservers()
    }

    private fun setUpListeners() {
        binding.apply {
            skip.setOnClickListener { pager.currentItem = NUM_PAGES -1 }
            pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == NUM_PAGES - 1){
                        next.setOnClickListener { viewModel.onClickEnter(adapter.nameFragment.name.text.toString()) }
                        next.text = getString(R.string.done)
                        next.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.button_font_size))
                        skip.visibility = View.INVISIBLE
                    } else {
                        next.setOnClickListener { pager.currentItem++ }
                        next.text = getString(R.string.next)
                        next.textSize = 30F
                        skip.visibility = View.VISIBLE
                    }
                }

            })
        }
    }

    private fun setUpDataObservers() {
        viewModel.warning.observe(this) {
            adapter.nameFragment.textInput.error = it
        }
        viewModel.shouldStartActivity.observe(this) {
            if (it) {
                startActivity(
                    Intent(this@TutorialActivity, MapActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) })
            }
        }
    }

    private fun setUpViewPager() {
        binding.pager.apply {
            adapter = this@TutorialActivity.adapter
            //Get rid of overscrolling effect
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            //tab.view.isClickable = false
        }.attach()
    }

    override fun onBackPressed() {
        if (binding.pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.pager.currentItem--
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onPause() {
        super.onPause()
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    private inner class TutorialPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa){
        val nameFragment = NameFragment()
        override fun getItemCount(): Int =
            NUM_PAGES

        override fun createFragment(position: Int): Fragment{
            return when(position){
                NUM_PAGES -1 -> nameFragment
                else -> TutorialFragment()
            }
        }
    }

    // Used only to check if keyboard has opened
    override fun onGlobalLayout() {
        val r = Rect()
        binding.root.getWindowVisibleDisplayFrame(r)
        val screenHeight = binding.root.rootView.height
        val keypadHeight = screenHeight - r.bottom
        val isKeyboardShowing = keypadHeight > screenHeight * 0.15
        if (!keyboardShown && isKeyboardShowing){
            shrinkWithSoftKeyboard()
        } else if (keyboardShown && !isKeyboardShowing) {
            deployWithSoftKeyboard()
        }
        keyboardShown = isKeyboardShowing
    }

    private fun deployWithSoftKeyboard() {
        binding.apply {
            decoration.visibility = View.VISIBLE
            root.setBackgroundColor(
                ContextCompat.getColor(
                    this@TutorialActivity,
                    R.color.colorAccent
                )
            )
            ConstraintSet().apply {
                clone(binding.root as ConstraintLayout)
                clear(R.id.pager, ConstraintSet.VERTICAL)
                connect(R.id.pager, ConstraintSet.BOTTOM, R.id.skip, ConstraintSet.TOP)
                connect(R.id.pager, ConstraintSet.TOP, R.id.subtitle, ConstraintSet.BOTTOM)
                applyTo(binding.root as ConstraintLayout)
            }
        }
    }

    private fun shrinkWithSoftKeyboard() {
        binding.apply {
            decoration.visibility = View.GONE
            root.setBackgroundColor(
                ContextCompat.getColor(
                    this@TutorialActivity,
                    R.color.design_default_color_background
                )
            )
            ConstraintSet().apply {
                clone(root as ConstraintLayout)
                clear(R.id.pager, ConstraintSet.VERTICAL)
                connect(R.id.pager, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM)
                connect(R.id.pager, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
                applyTo(root as ConstraintLayout)
            }
        }
    }
}
