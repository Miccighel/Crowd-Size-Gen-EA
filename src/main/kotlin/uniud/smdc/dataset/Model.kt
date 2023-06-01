package uniud.smdc.dataset

import uniud.smdc.utils.Constants
import uniud.smdc.genetic.Crossover
import uniud.smdc.genetic.Mutation
import uniud.smdc.genetic.Problem
import krangl.*
import org.apache.logging.log4j.LogManager
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import uniud.smdc.genetic.Solution
import uniud.smdc.utils.Tools
import java.io.FileReader
import java.util.*
import kotlin.system.measureTimeMillis

class Model() {

    lateinit var parameters: Parameters
    lateinit var problem: Problem

    var timeElapsed: Long = 0

    private var logger = LogManager.getLogger(this::class.simpleName)

    fun updateParameters(parameters: Parameters) {
        this.parameters = parameters
        this.problem = Problem(parameters)
    }

    fun loadData() {

        val recordsParsed = mutableListOf<CSVRecord>()
        val columnIdentifiers = mutableListOf<String>()
        val rowIdentifiers = mutableListOf<String>()

        var dataframe: DataFrame = dataFrameOf(
            StringCol("row_id", emptyArray()),
        )

        val fileReader = FileReader("${Constants.PROJECT_INPUT_PATH}${parameters.datasetName}${Constants.CSV_FILE_EXTENSION}")
        val records: Iterable<CSVRecord> = CSVFormat.Builder.create(CSVFormat.RFC4180).setHeader("row_id", "col_id", "rel").build().parse(fileReader)
        for (record in records) {
            recordsParsed.add(record)
            if(parameters.matrixKind=="WxD") {
                val rowIdentifier = record.get("row_id")
                val columnIdentifier = record.get("col_id")
                if (!rowIdentifiers.contains(rowIdentifier) && rowIdentifier != "row_id") rowIdentifiers.plusAssign(rowIdentifier)
                if (!columnIdentifiers.contains(columnIdentifier) && columnIdentifier != "col_id") columnIdentifiers.plusAssign(columnIdentifier)
            } else {
                val rowIdentifier = record.get("col_id")
                val columnIdentifier = record.get("row_id")
                if (!rowIdentifiers.contains(rowIdentifier) && rowIdentifier != "col_id") rowIdentifiers.plusAssign(rowIdentifier)
                if (!columnIdentifiers.contains(columnIdentifier) && columnIdentifier != "row_id") columnIdentifiers.plusAssign(columnIdentifier)
            }

        }

        columnIdentifiers.forEach { documentIdentifier ->
            dataframe = dataframe.addColumn(documentIdentifier) { -1 }
        }

        for (rowIdentifier in rowIdentifiers) {
            val map = mutableMapOf<String, String>()
            columnIdentifiers.forEach { currentDocumentIdentifier ->
                map[currentDocumentIdentifier] = "-1"
            }
            for (record in recordsParsed) {
                var currentRowIdentifier = ""
                var currentColumnIdentifier = ""
                if(parameters.matrixKind=="WxD") {
                    currentRowIdentifier = record.get("row_id")
                    currentColumnIdentifier = record.get("col_id")
                } else{
                    currentRowIdentifier = record.get("col_id")
                    currentColumnIdentifier = record.get("row_id")
                }
                val currentRelValue = record.get("rel")
                if (rowIdentifier == currentRowIdentifier) {
                    map[currentColumnIdentifier] = currentRelValue
                }
            }
            val row = listOf(rowIdentifier) + map.values.toMutableList()
            dataframe = dataframe.addRow(row)
        }

        dataframe = dataframe.remove("row_id")

        problem.loadDataframe(dataframe, rowIdentifiers)

    }

    fun solve() {

        logger.info("Model solving started")

        if (parameters.targetToAchieve == Constants.TARGET_AVERAGE) {

            val loggingFactor = (problem.dataframe.ncol * Constants.LOGGING_FACTOR) / 100
            var progressCounter = 0

            // This branch gets executed when an "Average" experiment needs to be computed

            val startTime = System.nanoTime()

            val numberOfDocuments = problem.dataframe.ncol

            (0 until numberOfDocuments).withIndex().forEach { (iterationCounter, currentCardinality) ->

                if ((iterationCounter % loggingFactor) == 0 && numberOfDocuments - 1 > loggingFactor) {
                    logger.info("Completed iterations: $currentCardinality/${numberOfDocuments} ($progressCounter%) for evaluations being computed on \"${Thread.currentThread().name}\" with target ${parameters.targetToAchieve}.")
                    progressCounter += Constants.LOGGING_FACTOR
                }

                val generator = Random()
                var configuration: IntArray

                for (repetition in 0 until parameters.numberOfRepetitions) {
                    val documentsToChoose = HashSet<Int>()
                    while (documentsToChoose.size < currentCardinality + 1) documentsToChoose.add(generator.nextInt(numberOfDocuments) + 1)
                    configuration = IntArray(numberOfDocuments)
                    documentsToChoose.forEach { chosenDocument -> configuration[chosenDocument - 1] = 1 }
                    val solution = Solution(configuration.toMutableList())
                    solution.updateData(problem.dataframe.cols, problem.dataframe.ncol, problem.dataframe.nrow, problem.rowIdentifiers)
                    @Suppress("UNCHECKED_CAST")
                    val columns = solution.getAttribute("columns") as MutableList<DataCol>
                    val (_, nRecommended) = Tools.ANOVA2(sigma2 = Tools.computeResidual(columns))
                    solution.setObjective(0, nRecommended.toDouble())
                    var solutionList = problem.solutions[solution.getCardinality()]
                    if (solutionList != null) {
                        solutionList.plusAssign(solution)
                    } else {
                        solutionList = mutableListOf()
                        solutionList.plusAssign(solution)
                    }
                    var objectiveValue = 0.0
                    for (solutionCurrent in solutionList) {
                        objectiveValue += solutionCurrent.getObjective(0)
                    }
                    val mean = objectiveValue / solutionList.size.toDouble()
                    problem.means[solution.getCardinality()] = mean
                    problem.solutions[solution.getCardinality()] = solutionList
                }

            }

            this.timeElapsed = (System.nanoTime() - startTime) / 1000000

        } else {

            val crossover = Crossover(parameters.crossoverProbability)
            val mutation = Mutation(parameters.mutationProbability)
            val builder = NSGAIIBuilder(problem, crossover, mutation, parameters.populationSize)
            builder.setMaxEvaluations(parameters.numberOfIterations)

            val algorithm = builder.build()

            val timeElapsed = measureTimeMillis {

                try {
                    algorithm.run()
                } catch (exception: Exception) {
                    logger.warn(exception.message)
                    logger.warn(exception.stackTraceToString())
                } finally {
                    logger.info("Model solving ended")
                }

            }

            this.timeElapsed = timeElapsed

        }

    }

    fun getSolutionsPath(): String {
        return "${Constants.PROJECT_OUTPUT_PATH}${parameters.datasetName}${Constants.FILE_NAME_SEPARATOR}${parameters.targetToAchieve.lowercase(Locale.getDefault())}${Constants.FILE_NAME_SEPARATOR}${parameters.matrixKind.lowercase(Locale.getDefault())}${Constants.SOLUTIONS_FILE_SUFFIX}${Constants.CSV_FILE_EXTENSION}"
    }

    fun getSolutionsSerializedPath(): String {
        return "${Constants.PROJECT_OUTPUT_PATH}${parameters.datasetName}${Constants.FILE_NAME_SEPARATOR}${parameters.targetToAchieve.lowercase(Locale.getDefault())}${Constants.FILE_NAME_SEPARATOR}${parameters.matrixKind.lowercase(Locale.getDefault())}${Constants.SOLUTIONS_FILE_SUFFIX}${Constants.JSON_FILE_EXTENSION}"
    }


}