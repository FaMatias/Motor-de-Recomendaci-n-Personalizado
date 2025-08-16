document.addEventListener('DOMContentLoaded', () => {
    const moviesToRate = [
        "Matrix", "El Señor de los Anillos", "Pulp Fiction", "Interestelar", "Titanic",
        "El Padrino", "Cadena Perpetua", "Origen", "Forrest Gump", "Parásitos"
    ];
    
    // --- 1. Simulación de "Big Data" de valoraciones ---
    // Esta es nuestra "base de datos" simulada con 50 usuarios y sus valoraciones aleatorias.
    const allMovies = [...moviesToRate, "Star Wars", "V de Vendetta", "La La Land", "Joker", "El Club de la Pelea", "Gladiator", "Up", "Toy Story", "Spider-Man", "Blade Runner"];
    const simulatedUserData = generateSimulatedData(50, allMovies);

    const movieListDiv = document.getElementById('movie-list');
    const recommendBtn = document.getElementById('recommend-btn');
    const loadingSpinner = document.getElementById('loading-spinner');
    const recommendationsList = document.getElementById('recommendations-list');
    const resultsIntro = document.getElementById('results-intro');

    // Carga inicial: muestra las películas para valorar
    displayMoviesToRate();

    recommendBtn.addEventListener('click', () => {
        runRecommendationAlgorithm();
    });

    // --- Funciones del Algoritmo de Recomendación ---

    function runRecommendationAlgorithm() {
        const userRatings = getUserRatings();
        if (Object.keys(userRatings).length === 0) {
            alert("Por favor, valora al menos una película.");
            return;
        }

        recommendBtn.disabled = true;
        loadingSpinner.style.display = 'block';
        recommendationsList.innerHTML = '';
        resultsIntro.textContent = 'Analizando tus gustos...';

        setTimeout(() => {
            // Paso 1: Encontrar usuarios similares
            const similarUsers = findSimilarUsers(userRatings);
            
            // Paso 2: Generar recomendaciones
            const recommendations = getRecommendations(userRatings, similarUsers);
            
            // Paso 3: Mostrar los resultados
            displayRecommendations(recommendations);
            
            recommendBtn.disabled = false;
            loadingSpinner.style.display = 'none';
        }, 1000); // Simula un tiempo de procesamiento
    }

    // Calcula la "similaridad" entre el usuario y los usuarios simulados.
    // Usamos una simple métrica de distancia de calificaciones.
    function findSimilarUsers(userRatings) {
        const userSimilarities = [];
        
        for (const userId in simulatedUserData) {
            const simulatedUserRatings = simulatedUserData[userId];
            let totalDifference = 0;
            let commonMovies = 0;

            for (const movie in userRatings) {
                if (simulatedUserRatings[movie] !== undefined) {
                    totalDifference += Math.pow(userRatings[movie] - simulatedUserRatings[movie], 2);
                    commonMovies++;
                }
            }

            // Si hay películas en común, calcula la similaridad.
            if (commonMovies > 0) {
                // La similaridad es inversamente proporcional a la diferencia total
                const similarityScore = 1 / (1 + totalDifference);
                userSimilarities.push({ userId, score: similarityScore });
            }
        }
        
        // Ordena por la puntuación de similaridad de mayor a menor
        userSimilarities.sort((a, b) => b.score - a.score);
        
        // Devuelve los 3 usuarios más similares
        return userSimilarities.slice(0, 3);
    }
    
    // Genera recomendaciones basadas en los usuarios similares
    function getRecommendations(userRatings, similarUsers) {
        const recommendedMovies = {};

        similarUsers.forEach(similarUser => {
            const ratings = simulatedUserData[similarUser.userId];
            for (const movie in ratings) {
                // Si la película no ha sido valorada por el usuario actual y tiene una buena valoración por el usuario similar
                if (userRatings[movie] === undefined && ratings[movie] >= 4) {
                    if (recommendedMovies[movie] === undefined) {
                        recommendedMovies[movie] = { score: 0, count: 0 };
                    }
                    recommendedMovies[movie].score += ratings[movie] * similarUser.score;
                    recommendedMovies[movie].count += similarUser.score;
                }
            }
        });
        
        const finalRecommendations = [];
        for (const movie in recommendedMovies) {
            finalRecommendations.push({
                title: movie,
                predictedScore: recommendedMovies[movie].score / recommendedMovies[movie].count
            });
        }
        
        // Ordena las recomendaciones por la puntuación de predicción
        finalRecommendations.sort((a, b) => b.predictedScore - a.predictedScore);
        
        return finalRecommendations.slice(0, 5); // Devuelve las 5 mejores recomendaciones
    }

    // --- Funciones de Interfaz (UI) ---

    function displayMoviesToRate() {
        moviesToRate.forEach(movie => {
            const card = document.createElement('div');
            card.className = 'movie-card';
            card.innerHTML = `
                <h4>${movie}</h4>
                <div class="rating-options">
                    <select data-movie="${movie}">
                        <option value="0">---</option>
                        <option value="5">⭐⭐⭐⭐⭐</option>
                        <option value="4">⭐⭐⭐⭐</option>
                        <option value="3">⭐⭐⭐</option>
                        <option value="2">⭐⭐</option>
                        <option value="1">⭐</option>
                    </select>
                </div>
            `;
            movieListDiv.appendChild(card);
        });
    }

    function getUserRatings() {
        const userRatings = {};
        const selects = movieListDiv.querySelectorAll('select');
        selects.forEach(select => {
            const rating = parseInt(select.value, 10);
            if (rating > 0) {
                const movie = select.dataset.movie;
                userRatings[movie] = rating;
            }
        });
        return userRatings;
    }

    function displayRecommendations(recommendations) {
        recommendationsList.innerHTML = '';
        resultsIntro.textContent = 'Basándonos en tus valoraciones, te recomendamos estas películas:';
        if (recommendations.length === 0) {
            recommendationsList.innerHTML = '<li>No pudimos encontrar recomendaciones. Valora más películas para obtener mejores resultados.</li>';
        } else {
            recommendations.forEach(rec => {
                const li = document.createElement('li');
                li.textContent = `${rec.title} (Puntuación esperada: ${rec.predictedScore.toFixed(1)})`;
                recommendationsList.appendChild(li);
            });
        }
    }

    // --- Función para generar datos simulados ---
    function generateSimulatedData(numUsers, movies) {
        const data = {};
        const random = () => Math.floor(Math.random() * 5) + 1; // 1-5 rating
        
        for (let i = 1; i <= numUsers; i++) {
            const user = `user_${i}`;
            const numRatings = Math.floor(Math.random() * (movies.length / 2)) + 5;
            const userRatings = {};
            const ratedMovies = new Set();
            
            while (ratedMovies.size < numRatings) {
                const movie = movies[Math.floor(Math.random() * movies.length)];
                if (!ratedMovies.has(movie)) {
                    userRatings[movie] = random();
                    ratedMovies.add(movie);
                }
            }
            data[user] = userRatings;
        }
        return data;
    }
});
