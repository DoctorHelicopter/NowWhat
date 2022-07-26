package com.drh.nowwhat.android

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.drh.nowwhat.android.data.DBHelper
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DBHelper(this, null)

        // set view to main
        setContentView(R.layout.activity_main)

        // configure button listener
        val categoriesButton: Button = findViewById(R.id.categories_button)
        categoriesButton.setOnClickListener {
            val intent = Intent(this, CategoriesListActivity::class.java)
            startActivity(intent)
        }

        // configure randomizer
        val randomizerButton: Button = findViewById(R.id.randomizer_button)
        randomizerButton.setOnClickListener {
            val categoryView: TextView = findViewById(R.id.selected_category)
            val choiceView: TextView = findViewById(R.id.selected_choice)
            val progressBar: ProgressBar = findViewById(R.id.randomizer_progress_bar)
            val progressBarText: TextView = findViewById(R.id.randomizer_progress_text)

            val categories = db.getEnabledCategoriesWithChoices()

            if (categories.isEmpty()) {
                categoryView.text = getString(R.string.no_choices_error)
                categoryView.visibility = VISIBLE
                choiceView.visibility = INVISIBLE
            } else {
                // animate progress bar
                val progressTextOptions = this.resources.getStringArray(R.array.progress_text)
                progressBarText.text = progressTextOptions[Random.nextInt(progressTextOptions.size)]
                val width: Int = progressBar.width * 1000
                progressBar.max = width
                val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, width)
                animator.interpolator = DecelerateInterpolator()
                animator.startDelay = 0
                animator.duration = this.resources.getInteger(R.integer.randomizer_duration).toLong()
                animator.setAutoCancel(true)
                animator.addListener(
                    object : Animator.AnimatorListener {
                        override fun onAnimationEnd(animation: Animator?) {
                            // hide progress bar
                            progressBar.visibility = INVISIBLE
                            progressBarText.visibility = INVISIBLE
                            // display choice
                            val eligibleCategories = categories.toMutableList()
                            // add favorited categories twice
                            categories.forEach { if (it.favorite) eligibleCategories.add(it) }
                            val allOptions = eligibleCategories
                                .map { c -> c.choices.map { Pair(c, it) } }
                                .flatten()
                            val eligibleOptions = allOptions.toMutableList()
                            // add favorited choices twice (four times if the category is also favorited)
                            allOptions.forEach { if (it.second.favorite) eligibleOptions.add(it) }
                            val (selectedCategory, selectedChoice) = eligibleOptions[Random.nextInt(allOptions.size)]
                            categoryView.text = selectedCategory.name
                            choiceView.text = selectedChoice.name
                            categoryView.visibility = VISIBLE
                            choiceView.visibility = VISIBLE
                        }
                        override fun onAnimationStart(animation: Animator?) {
                            progressBar.visibility = VISIBLE
                            progressBarText.visibility = VISIBLE
                            categoryView.visibility = INVISIBLE
                            choiceView.visibility = INVISIBLE
                        }
                        override fun onAnimationCancel(animation: Animator?) {
                            progressBar.visibility = INVISIBLE
                            progressBarText.visibility = INVISIBLE
                            categoryView.visibility = INVISIBLE
                            choiceView.visibility = INVISIBLE
                        }
                        override fun onAnimationRepeat(animation: Animator?) {}
                    }
                )
                animator.start()
            }
        }
    }


}

