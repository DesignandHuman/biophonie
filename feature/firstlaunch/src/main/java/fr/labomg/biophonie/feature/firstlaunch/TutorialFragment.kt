package fr.labomg.biophonie.feature.firstlaunch

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.work.ClearCacheWorker
import fr.labomg.biophonie.feature.firstlaunch.databinding.FragmentTutorialBinding
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

private const val ANIMATION_DELAY = 100L

@AndroidEntryPoint
class TutorialFragment : Fragment(), ViewTreeObserver.OnGlobalLayoutListener {

    private val viewModel: TutorialViewModel by activityViewModels()
    private var keyboardShown = false
    private lateinit var binding: FragmentTutorialBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial, container, false)
        setUpViewPager()
        setUpListeners()
        setUpDataObservers()
        setUpClearCacheWorker()
        return binding.root
    }

    private fun setUpListeners() {
        binding.apply {
            skip.setOnClickListener { pager.setCurrentItem(NUM_PAGES - 1, true) }
            next.setOnClickListener {
                if (pager.currentItem == NUM_PAGES - 1) {
                    viewModel.onClickEnter()
                } else {
                    pager.setCurrentItem(pager.currentItem + 1, true)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.pager.currentItem == 0) {
                requireActivity().finish()
            } else {
                binding.pager.setCurrentItem(binding.pager.currentItem - 1, true)
            }
        }
    }

    private fun setUpDataObservers() {
        viewModel.shouldStartExploring.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack(R.id.firstlaunch_navigation, true)
            }
        }
    }

    private fun setUpViewPager() {
        binding.pager.apply {
            adapter = TutorialPagerAdapter(this@TutorialFragment)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == NUM_PAGES - 1) {
                            displayLastPage()
                        } else {
                            displayPage()
                        }
                        postDelayed(ANIMATION_DELAY) {
                            (childFragmentManager.findFragmentByTag("f$position")
                                    as? FirstLaunchFragments)
                                ?.animate()
                        }
                    }
                }
            )
        }
        TabLayoutMediator(binding.tabLayout, binding.pager) { _, _ ->
                // tab.view.isClickable = false
            }
            .attach()
    }

    private fun setUpClearCacheWorker() {
        val requestBuilder =
            PeriodicWorkRequestBuilder<ClearCacheWorker>(
                DAYS_BETWEEN_WORK_REQUEST,
                TimeUnit.DAYS,
                1,
                TimeUnit.DAYS
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = LocalDateTime.now()
            val tonight = LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
            requestBuilder.setInitialDelay(
                Duration.between(now, tonight.plusDays(DAYS_BETWEEN_WORK_REQUEST))
            )
        }
        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                ClearCacheWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                requestBuilder.build()
            )
    }

    override fun onResume() {
        super.onResume()
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onPause() {
        super.onPause()
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    private class TutorialPagerAdapter(fa: TutorialFragment) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                FIRST_PAGE -> TutoMapFragment()
                SECOND_PAGE -> TutoDetailsFragment()
                THIRD_PAGE -> TutoLocationFragment()
                FOURTH_PAGE -> TutoRecordFragment()
                else -> TutoNameFragment()
            }
        }

        companion object {
            private const val FIRST_PAGE = 0
            private const val SECOND_PAGE = FIRST_PAGE + 1
            private const val THIRD_PAGE = SECOND_PAGE + 1
            private const val FOURTH_PAGE = THIRD_PAGE + 1
        }
    }

    // Used only to check if keyboard was opened
    override fun onGlobalLayout() {
        val r = Rect()
        binding.root.getWindowVisibleDisplayFrame(r)
        val screenHeight = binding.root.rootView.height
        val keypadHeight = screenHeight - r.bottom
        val isKeyboardShowing = keypadHeight > screenHeight * MIN_SCREEN_KEYBOARD_RATIO
        if (!keyboardShown && isKeyboardShowing) {
            shrinkWithSoftKeyboard()
        } else if (keyboardShown && !isKeyboardShowing) {
            deployWithSoftKeyboard()
        }
        keyboardShown = isKeyboardShowing
    }

    private fun deployWithSoftKeyboard() {
        binding.apply {
            decoration.visibility = View.VISIBLE
            root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            circle.background =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.background_circle,
                    requireContext().theme
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
                ContextCompat.getColor(requireContext(), R.color.design_default_color_background)
            )
            circle.background = null
            ConstraintSet().apply {
                clone(root as ConstraintLayout)
                clear(R.id.pager, ConstraintSet.VERTICAL)
                connect(R.id.pager, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM)
                connect(R.id.pager, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
                applyTo(root as ConstraintLayout)
            }
        }
    }

    private fun displayPage() {
        with(binding) {
            next.text = getString(R.string.next)
            next.textSize = TEXT_SIZE
            // setting visibility does not work :(
            skip.setTextColor(requireContext().getColor(R.color.colorPrimary))
        }
    }

    private fun displayLastPage() {
        with(binding) {
            next.text = getString(R.string.done)
            next.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.button_font_size)
            )
            // setting visibility does not work :(
            skip.setTextColor(requireContext().getColor(R.color.colorAccent))
        }
    }

    companion object {
        private const val NUM_PAGES = 5
        private const val TEXT_SIZE = 30F
        private const val DAYS_BETWEEN_WORK_REQUEST = 14L
        private const val MIN_SCREEN_KEYBOARD_RATIO = 0.15
    }
}
