package com.example.biophonie.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.biophonie.R
import com.example.biophonie.databinding.ActivityTutorialBinding
import com.google.android.material.tabs.TabLayoutMediator

private const val NUM_PAGES = 3
private const val TAG = "TutorialActivity"

//TODO maybe get permissions inside tutorial ?
class TutorialActivity : FragmentActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private val adapter = TutorialPagerAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial)

        setUpViewPager()
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.apply {
            skip.setOnClickListener { pager.currentItem = NUM_PAGES -1 }
            pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == NUM_PAGES - 1){
                        next.setOnClickListener { startMapActivity() }
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

    private fun startMapActivity() {
        if(adapter.nameFragment.name.text.toString().isNotEmpty()){
            storeName(adapter.nameFragment.name.text.toString())
            startActivity(
                Intent(this, MapActivity::class.java)
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) })
        }
        else
            Toast.makeText(this, "Veuillez donner un nom", Toast.LENGTH_SHORT).show()
    }

    private fun storeName(name: String){
        val prefs = getSharedPreferences(null, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("name", name)
        editor.apply()
    }

    private fun setUpViewPager() {
        binding.pager.adapter = adapter

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


    private inner class TutorialPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        val nameFragment = NameFragment()
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment{
            return when(position){
                NUM_PAGES-1 -> nameFragment
                else -> TutorialFragment()
            }
        }
    }

}