package com.example.test_lab_week_12

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.test_lab_week_12.model.Movie
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val movieAdapter by lazy {
        MovieAdapter(object : MovieAdapter.MovieClickListener {
            override fun onMovieClick(movie: Movie) {
                openMovieDetails(movie)
            }
        })
    }

    private lateinit var movieViewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.movie_list)
        recyclerView.adapter = movieAdapter

        val movieRepository = (application as MovieApplication).movieRepository

        movieViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MovieViewModel(movieRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[MovieViewModel::class.java]

        // lifecycleScope is a lifecycle-aware coroutine scope
        lifecycleScope.launch {
            // run when Activity at least STARTED
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // collect movies
                launch {
                    movieViewModel.popularMovies.collect { movies ->
                        movieAdapter.addMovies(movies)
                    }
                }
                // collect error
                launch {
                    movieViewModel.error.collect { errorMsg ->
                        if (errorMsg.isNotEmpty()) {
                            Snackbar.make(
                                recyclerView,
                                errorMsg,              // sekarang String normal
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun openMovieDetails(movie: Movie) {
        val intent = Intent(this, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EXTRA_TITLE, movie.title)
            putExtra(DetailsActivity.EXTRA_RELEASE, movie.releaseDate)
            putExtra(DetailsActivity.EXTRA_OVERVIEW, movie.overview)
            putExtra(DetailsActivity.EXTRA_POSTER, movie.posterPath)
        }
        startActivity(intent)
    }
}
