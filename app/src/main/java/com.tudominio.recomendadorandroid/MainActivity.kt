package com.tudominio.recomendadorandroid // Asegúrate de que el nombre de tu paquete sea correcto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- 1. Modelo de Datos (data class) ---
data class Movie(val id: Int, val title: String)
data class UserRatings(val userId: Int, val ratings: Map<Int, Int>)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Usa el tema de la app
            MaterialTheme {
                RecommenderScreen()
            }
        }
    }
}

// --- 2. Interfaz de Usuario (Jetpack Compose) y Lógica ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommenderScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var resultsText by remember { mutableStateOf("Valora algunas películas para obtener recomendaciones.") }
    var userMovieRatings by remember { mutableStateOf<Map<Int, Int>>(mapOf()) }

    // Simulación de la base de datos
    val allMovies = remember { generateMovies() }
    val moviesToRate = remember { allMovies.shuffled().take(10) }
    val simulatedUserData = remember { generateSimulatedData(50, allMovies) }
    
    // Lista de recomendaciones
    var recommendations by remember { mutableStateOf<List<Movie>>(emptyList()) }
    
    // State para las valoraciones del usuario
    val userRatingsState = remember { mutableStateMapOf<Int, Int>() }

    // --- Lógica del Algoritmo de Recomendación ---
    suspend fun runRecommendationAlgorithm() {
        isLoading = true
        resultsText = "Analizando tus gustos..."
        recommendations = emptyList() // Limpia recomendaciones previas
        
        delay(1000) // Simula tiempo de procesamiento

        // 1. Encontrar usuarios similares
        val similarUsers = findSimilarUsers(userRatingsState.toMap(), simulatedUserData)

        // 2. Generar recomendaciones
        val generatedRecommendations = getRecommendations(userRatingsState.toMap(), similarUsers, simulatedUserData, allMovies)

        // 3. Mostrar los resultados
        resultsText = "Basándonos en tus valoraciones, te recomendamos estas películas:"
        recommendations = generatedRecommendations
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎬 Recomendador de Películas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sección de valoración de películas
                Text("1. Valora estas películas", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                MovieRatingSection(moviesToRate, userRatingsState)
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (userRatingsState.isEmpty()) {
                            // Usar Toast en Android para mostrar mensajes
                            return@Button
                        }
                        runRecommendationAlgorithm()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Analizando..." else "Obtener Recomendaciones")
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Sección de resultados
                Text("2. Tus Recomendaciones", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    RecommendationResultSection(resultsText, recommendations)
                }
            }
        }
    )
}

@Composable
fun MovieRatingSection(movies: List<Movie>, userRatingsState: MutableMap<Int, Int>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(movies) { movie ->
            MovieCard(movie, onRatingChange = { rating ->
                userRatingsState[movie.id] = rating
            })
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onRatingChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(movie.title, fontWeight = FontWeight.SemiBold)
            RatingDropdown(onRatingChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingDropdown(onRatingChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(0) }
    val ratings = listOf(0, 5, 4, 3, 2, 1) // 0 para "---"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = if (selectedRating == 0) "---" else "$selectedRating ⭐",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().width(80.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ratings.forEach { rating ->
                DropdownMenuItem(
                    text = { Text(if (rating == 0) "---" else "$rating ⭐") },
                    onClick = {
                        selectedRating = rating
                        onRatingChange(rating)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RecommendationResultSection(introText: String, recommendations: List<Movie>) {
    Column(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
    ) {
        Text(introText)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
            ) {
                if (recommendations.isEmpty()) {
                    item {
                        Text("No se encontraron recomendaciones. Valora más películas.", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(recommendations) { movie ->
                        Text("• ${movie.title}", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

// --- Funciones del Algoritmo de Recomendación ---

private fun generateMovies(): List<Movie> {
    val titles = listOf(
        "Matrix", "El Señor de los Anillos", "Pulp Fiction", "Interestelar", "Titanic",
        "El Padrino", "Cadena Perpetua", "Origen", "Forrest Gump", "Parásitos",
        "Star Wars", "V de Vendetta", "La La Land", "Joker", "El Club de la Pelea",
        "Gladiator", "Up", "Toy Story", "Spider-Man", "Blade Runner"
    )
    return titles.mapIndexed { index, title -> Movie(index + 1, title) }
}

private fun generateSimulatedData(numUsers: Int, movies: List<Movie>): List<UserRatings> {
    val random = Random.Default
    val simulatedData = mutableListOf<UserRatings>()
    
    for (i in 1..numUsers) {
        val numRatings = random.nextInt(5, movies.size / 2)
        val ratedMovies = movies.shuffled().take(numRatings)
        val ratings = ratedMovies.associate { it.id to random.nextInt(1, 6) }
        simulatedData.add(UserRatings(i, ratings))
    }
    return simulatedData
}

private fun findSimilarUsers(userRatings: Map<Int, Int>, simulatedData: List<UserRatings>): List<UserRatings> {
    val userSimilarities = simulatedData.mapNotNull { simulatedUser ->
        val commonMovies = userRatings.keys.intersect(simulatedUser.ratings.keys)
        if (commonMovies.isEmpty()) return@mapNotNull null

        val totalDifference = commonMovies.sumOf { movieId ->
            val userRating = userRatings[movieId] ?: 0
            val simulatedRating = simulatedUser.ratings[movieId] ?: 0
            (userRating - simulatedRating).toDouble().pow(2)
        }

        // La similaridad es inversamente proporcional a la diferencia
        val similarityScore = 1.0 / (1.0 + totalDifference)
        simulatedUser to similarityScore
    }

    return userSimilarities.sortedByDescending { it.second }.take(3).map { it.first }
}

private fun getRecommendations(
    userRatings: Map<Int, Int>,
    similarUsers: List<UserRatings>,
    simulatedData: List<UserRatings>,
    allMovies: List<Movie>
): List<Movie> {
    val movieScores = mutableMapOf<Int, Double>()
    
    similarUsers.forEach { similarUser ->
        similarUser.ratings.forEach { (movieId, rating) ->
            if (userRatings[movieId] == null && rating >= 4) {
                movieScores.compute(movieId) { _, score ->
                    (score ?: 0.0) + rating
                }
            }
        }
    }
    
    val recommendedMovieIds = movieScores.entries
        .sortedByDescending { it.value }
        .take(5)
        .map { it.key }
        
    return allMovies.filter { it.id in recommendedMovieIds }
}
