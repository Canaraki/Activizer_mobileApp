class ExerciseRepository(private val api: ApiService) {
    suspend fun getLastRemoteResult(): ExerciseResult? {
        val now = LocalDateTime.now()
        val from = now.minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val to = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val response = api.getLatestStats(from, to)
        if (response.isSuccessful) {
            val list = response.body()?.message?.firstOrNull()
            if (list != null) {
                val score = (list[0] as Double).toFloat()
                val date = list[1] as String
                return ExerciseResult("ozan", score, steps = 10, durationInSeconds = 33, exerciseDate = date)
            }
        }
        return null
    }
}
