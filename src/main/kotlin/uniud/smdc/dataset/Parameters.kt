package uniud.smdc.dataset

data class Parameters(
    val datasetName: String,
    val targetToAchieve: String,
    val numberOfIterations: Int,
    val numberOfRepetitions: Int,
    val populationSize: Int,
    val crossoverProbability: Double,
    val mutationProbability: Double,
    val matrixKind: String
)
