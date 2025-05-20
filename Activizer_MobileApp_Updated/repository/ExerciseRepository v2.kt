class ExerciseRepository(private val api: ApiService) {
    suspend fun getAllRemoteResults(): List<ExerciseResult> {
        val now = LocalDateTime.now()
        val from = now.minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val to = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val response = api.getAllStats(from, to)
        if (response.isSuccessful) {
            return response.body()?.message?.map { list ->
                ExerciseResult(
                    username = "ozan",
                    score = (list[0] as Double).toFloat(),
                    steps = 10,
                    durationInSeconds = 30,
                    exerciseDate = list[1] as String
                )
            } ?: emptyList()
        }
        return emptyList()
    }
}
