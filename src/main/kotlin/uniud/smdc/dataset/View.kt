package uniud.smdc.dataset

import com.google.gson.Gson
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.logging.log4j.LogManager
import uniud.smdc.utils.Constants
import java.io.FileWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class View {


    private var logger = LogManager.getLogger(this::class.simpleName)

    fun print(model: Model) {

        val path = model.getSolutionsPath()
        val solutionsToSerialize = mutableListOf<SolutionSerialized>()

        val writer: Writer = Files.newBufferedWriter(Paths.get(path))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

        logger.info("Starting to print results at path: ")
        logger.info(path)

        val data = mutableListOf<Array<String>>()

        val header = mutableListOf<String>()
        header.add("parameter_topic")
        header.add("parameter_iterations_number")
        header.add("parameter_population_size")
        header.add("parameter_crossover_probability")
        header.add("parameter_mutation_probability")
        header.add("parameter_repetitions_number")
        header.add("parameter_matrix_kind")
        header.add("solution_cardinality")
        header.add("solution_objective")
        header.add("algorithm_time_elapsed")

        data.add(header.toTypedArray())

        csvPrinter.printRecord(header)

        for ((cardinality, solutionList) in model.problem.solutions) {
            if (solutionList.size > 0) {
                val solution = solutionList[0]
                val row = mutableListOf<String>()
                row.plusAssign(model.parameters.datasetName)
                row.plusAssign(model.parameters.numberOfIterations.toString())
                row.plusAssign(model.parameters.populationSize.toString())
                row.plusAssign(model.parameters.crossoverProbability.toString())
                row.plusAssign(model.parameters.mutationProbability.toString())
                row.plusAssign(model.parameters.numberOfRepetitions.toString())
                row.plusAssign(model.parameters.matrixKind)
                row.plusAssign(solution.getCardinality().toString())
                row.plusAssign(solution.getObjective(0).toString())
                row.plusAssign(model.timeElapsed.toString())
                csvPrinter.printRecord(row)
                val solutionSerialized = SolutionSerialized(
                    solution.getObjective(0).toString(),
                    solution.getAttribute("columnsActual"),
                    solution.getAttribute("columnsTotal"),
                    solution.getAttribute("columnsNames"),
                    solution.getAttribute("rowsTotal"),
                    solution.getAttribute("rowsActual"),
                    solution.getAttribute("rowsNames"),
                    solution.getAttribute("configuration"),
                    solution.getAttribute("rowsConfiguration"),
                )
                solutionsToSerialize.plusAssign(solutionSerialized)
            }
        }

        csvPrinter.flush()

        val gson = Gson()
        gson.toJson(solutionsToSerialize, FileWriter(model.getSolutionsSerializedPath()));

        logger.info("Results printing completed.")

    }

    fun print(model: Model, values: Map<Double, Double>) {

        val path = model.getSolutionsPath()

        val writer: Writer = Files.newBufferedWriter(Paths.get(path))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)

        logger.info("Starting to print results at path: ")
        logger.info(path)

        val data = mutableListOf<Array<String>>()

        val header = mutableListOf<String>()
        header.add("parameter_topic")
        header.add("parameter_iterations_number")
        header.add("parameter_population_size")
        header.add("parameter_crossover_probability")
        header.add("parameter_mutation_probability")
        header.add("parameter_repetitions_number")
        header.add("parameter_matrix_kind")
        header.add("solution_cardinality")
        header.add("solution_objective")
        header.add("algorithm_time_elapsed")

        data.add(header.toTypedArray())

        csvPrinter.printRecord(header)

        for ((cardinality, mean) in values) {
            val row = mutableListOf<String>()
            row.plusAssign(model.parameters.datasetName)
            row.plusAssign(model.parameters.numberOfIterations.toString())
            row.plusAssign(model.parameters.populationSize.toString())
            row.plusAssign(model.parameters.crossoverProbability.toString())
            row.plusAssign(model.parameters.mutationProbability.toString())
            row.plusAssign(model.parameters.numberOfRepetitions.toString())
            row.plusAssign(model.parameters.matrixKind)
            row.plusAssign(cardinality.toString())
            row.plusAssign(mean.toString())
            row.plusAssign(model.timeElapsed.toString())
            csvPrinter.printRecord(row)
        }

        csvPrinter.flush()

        val solutionsToSerialize = mutableListOf<SolutionSerialized>()
        for ((cardinality, solutionList) in model.problem.solutions) {
            if (solutionList.size > 0) {
                solutionList.forEach { solution ->
                    val solutionSerialized = SolutionSerialized(
                        solution.getObjective(0).toString(),
                        solution.getAttribute("columnsActual"),
                        solution.getAttribute("columnsTotal"),
                        solution.getAttribute("columnsNames"),
                        solution.getAttribute("rowsTotal"),
                        solution.getAttribute("rowsActual"),
                        solution.getAttribute("rowsNames"),
                        solution.getAttribute("configuration"),
                        solution.getAttribute("rowsConfiguration"),
                    )
                    solutionsToSerialize.plusAssign(solutionSerialized)
                }
            }
        }
        val gson = Gson()
        gson.toJson(solutionsToSerialize, FileWriter(model.getSolutionsSerializedPath()));

        logger.info("Results printing completed.")

    }

}