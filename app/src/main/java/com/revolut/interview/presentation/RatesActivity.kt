package com.revolut.interview.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revolut.interview.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Task 1 and Task 4 screen implementation.
 *
 * Displays USD rates in a RecyclerView, observes ViewModel state, and starts polling
 * only while the Activity is STARTED so periodic requests stop in the background.
 */
class RatesActivity : AppCompatActivity() {

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    private val adapter = RatesAdapter()
    private val viewModel: RatesViewModel by viewModels { ViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        applySystemBarInsets()
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Task 4: dividers are drawn by RecyclerView decoration instead of item views.
        recyclerView.addItemDecoration(
            RatesDividerItemDecoration(
                dividerColor = ContextCompat.getColor(this, R.color.grey),
                dividerHeight = resources.getDimensionPixelSize(R.dimen.rate_divider_height)
            )
        )
        recyclerView.adapter = adapter

        observeRates()
    }

    private fun observeRates() {
        lifecycleScope.launch {
            // Task 1: repeatOnLifecycle cancels polling when the screen is not visible.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.rateFlow.collect { rates ->
                        adapter.submitList(rates)
                    }
                }

                launch {
                    while (true) {
                        viewModel.getRates()
                        delay(1_000.milliseconds)
                    }
                }
            }
        }
    }

    private fun applySystemBarInsets() {
        val appBar = findViewById<View>(R.id.appBar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            appBar.updatePadding(top = statusBars.top)
            recyclerView.updatePadding(bottom = navigationBars.bottom)
            insets
        }
    }
}
