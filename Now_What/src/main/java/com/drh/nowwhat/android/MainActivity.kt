package com.drh.nowwhat.android

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.drh.nowwhat.android.data.DBHelper
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val db = DBHelper(this, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to main
        setContentView(R.layout.activity_main)

        // configure button listeners
        val categoriesButton: Button = findViewById(R.id.categories_button)
        categoriesButton.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }
        val platformsButton: Button = findViewById(R.id.platforms_button)
        platformsButton.setOnClickListener {
            val intent = Intent(this, PlatformsActivity::class.java)
            startActivity(intent)
        }

        // configure randomizer
        val randomizerButton: Button = findViewById(R.id.randomizer_button)
        randomizerButton.setOnClickListener {
            val categoryView: TextView = findViewById(R.id.selected_category)
            val choiceView: TextView = findViewById(R.id.selected_choice)
            val platformView: TextView = findViewById(R.id.selected_platform)
            val progressBar: ProgressBar = findViewById(R.id.randomizer_progress_bar)
            val progressBarText: TextView = findViewById(R.id.randomizer_progress_text)

            val platforms = db.getPlatforms().associateBy { it.id }
            val categories = db.getEnabledCategoriesWithChoices().map { c ->
                c.copy(choices = c.choices.map { ch ->
                    ch.copy(platform = platforms[ch.platformId])
                }.filter { it.platform?.enabled == true})
            }

            if (categories.isEmpty()) {
                categoryView.text = getString(R.string.no_choices_error)
                categoryView.visibility = VISIBLE
                choiceView.visibility = INVISIBLE
                platformView.visibility = INVISIBLE
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
                            // double the chance for a favorited category
                            categories.forEach { if (it.favorite) eligibleCategories.add(it) }
                            val allOptions = eligibleCategories
                                .map { c -> c.choices.map { Pair(c, it) } }
                                .flatten()
                            val eligibleOptions = allOptions.toMutableList()
                            // double the chance for a favorited choice and platform
                            allOptions.forEach {
                                if (it.second.favorite) eligibleOptions.add(it)
                                if (it.second.platform?.favorite == true) eligibleOptions.add(it)
                            }
                            eligibleOptions.shuffle()
                            val (selectedCategory, selectedChoice) = eligibleOptions[Random.nextInt(allOptions.size)]
                            categoryView.text = selectedCategory.name
                            choiceView.text = selectedChoice.name
                            platformView.text = selectedChoice.platform?.name
                            categoryView.visibility = VISIBLE
                            choiceView.visibility = VISIBLE
                            platformView.visibility = VISIBLE
                        }
                        override fun onAnimationStart(animation: Animator?) {
                            progressBar.visibility = VISIBLE
                            progressBarText.visibility = VISIBLE
                            categoryView.visibility = INVISIBLE
                            choiceView.visibility = INVISIBLE
                            platformView.visibility = INVISIBLE
                        }
                        override fun onAnimationCancel(animation: Animator?) {
                            progressBar.visibility = INVISIBLE
                            progressBarText.visibility = INVISIBLE
                            categoryView.visibility = INVISIBLE
                            choiceView.visibility = INVISIBLE
                            platformView.visibility = INVISIBLE
                        }
                        override fun onAnimationRepeat(animation: Animator?) {}
                    }
                )
                animator.start()
            }
        }
    }

    override fun onResume() {
        val categories = db.getCategories()
        val enabledCategoryCount = categories.count { it.enabled }
        val platforms = db.getPlatforms()
        val enabledPlatformCount = platforms.count { it.enabled }

        val categoriesButton: Button = findViewById(R.id.categories_button)
        val platformsButton: Button = findViewById(R.id.platforms_button)

        categoriesButton.text = getString(R.string.categories, enabledCategoryCount, categories.size)
        platformsButton.text = getString(R.string.platforms, enabledPlatformCount, platforms.size)
        super.onResume()
    }
}

