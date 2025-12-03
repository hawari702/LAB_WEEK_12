package com.example.test_lab_week_12

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_lab_week_12.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MovieViewModel(
    private val movieRepository: MovieRepository
) : ViewModel() {

    // ===== StateFlow untuk data movie =====
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies

    // ===== StateFlow untuk error (TIDAK nullable) =====
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    init {
        fetchPopularMovies()
    }

    // fetch movies from the API
    private fun fetchPopularMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            movieRepository
                .fetchMovies()               // Flow<List<Movie>>
                .catch { e ->
                    _error.value = "An exception occurred: ${e.message}"
                }
                .collect { movies ->
                    _popularMovies.value = movies
                }
        }
    }
}
