package uniud.smdc.genetic

import uniud.smdc.utils.Constants
import uniud.smdc.dataset.Parameters
import krangl.DataCol

import org.apache.commons.lang3.tuple.Pair
import org.apache.logging.log4j.LogManager

import org.uma.jmetal.problem.integerproblem.IntegerProblem
import org.uma.jmetal.solution.integersolution.IntegerSolution

import krangl.DataFrame
import krangl.StringCol
import krangl.dataFrameOf
import org.apache.commons.text.similarity.HammingDistance
import uniud.smdc.utils.CriterionException

import uniud.smdc.utils.Tools
import kotlin.math.abs

class Problem(

    private var parameters: Parameters,

    ) : IntegerProblem {

    var dataframe = dataFrameOf(StringCol("row_id", emptyArray()))
    var rowIdentifiers = mutableListOf<String>()
    var cardinality = 1

    var solutions = linkedMapOf<Double, MutableList<Solution>>()
    var means = linkedMapOf<Double, Double>()

    private var thresholdCompliance = sortedMapOf<Double, Boolean>()
    var thresholdMap = sortedMapOf<Double, Double>()
    private val improvementThreshold: Double = 100.0
    private var hammingCompliance = sortedMapOf<Double, Boolean>()
    var hammingMap = sortedMapOf<Double, Int>()
    private val hammingThreshold: Int = 10

    private var iterationCounter = 0
    private var progressCounter = 0

    private var logger = LogManager.getLogger(this::class.simpleName)

    fun loadDataframe(dataframe: DataFrame, rowIdentifiers: MutableList<String>) {
        this.dataframe = dataframe
        this.rowIdentifiers = rowIdentifiers
    }

    override fun getName(): String {
        return "Crowd-Size-Gen"
    }

    override fun getNumberOfVariables(): Int {
        return solutions[0.0]!!.random().variables.size
    }

    override fun getNumberOfObjectives(): Int {
        return 1
    }

    override fun getNumberOfConstraints(): Int {
        return solutions[0.0]!!.random().numberOfConstraints
    }

    override fun getLowerBound(index: Int): Int {
        return solutions[0.0]!!.random().getLowerBound(0)
    }

    override fun getUpperBound(index: Int): Int {
        return solutions[0.0]!!.random().getUpperBound(0)
    }

    override fun getBounds(): MutableList<Pair<Int, Int>> {
        val bounds = emptyList<Pair<Int, Int>>()
        bounds.plusElement(Pair.of(solutions[0.0]!!.random().getLowerBound(0), solutions[0.0]!!.random().getUpperBound(0)))
        return bounds.toMutableList()
    }

    override fun createSolution(): IntegerSolution {

        if (cardinality == dataframe.ncol) {
            cardinality = 1
        }

        val data = dataframe.cols.asSequence().shuffled().take(cardinality).toList()
        val configuration = mutableListOf<Int>()
        dataframe.cols.forEachIndexed { index, columnCurrent ->
            configuration.plusAssign(0)
            for (columnSelected in data) {
                if (columnCurrent.name == columnSelected.name) {
                    configuration[index] = 1
                }
            }
        }

        val solution = Solution(configuration)

        solution.updateData(dataframe.cols.toList(), dataframe.ncol, dataframe.nrow, rowIdentifiers)

        cardinality += 1

        return solution

    }

    override fun evaluate(solution: IntegerSolution?) {

        val loggingFactor = (parameters.numberOfIterations * Constants.LOGGING_FACTOR) / 100
        if ((iterationCounter % loggingFactor) == 0 && parameters.numberOfIterations > loggingFactor && iterationCounter <= parameters.numberOfIterations) {
            logger.info("Completed iterations: $iterationCounter/${parameters.numberOfIterations} ($progressCounter%) for evaluations being computed on \"${Thread.currentThread().name}\" with target \"${parameters.targetToAchieve}\".")
            progressCounter += Constants.LOGGING_FACTOR
        }

        solution as Solution

        solution.updateData(dataframe.cols.toList(), dataframe.ncol, dataframe.nrow, rowIdentifiers)

        val cardinality = solution.getCardinality()

        @Suppress("UNCHECKED_CAST")
        val columns = solution.getAttribute("columns") as MutableList<DataCol>

        /* PART 1: Metrics and objectives computation */

        when (parameters.targetToAchieve) {
            Constants.TARGET_BEST -> {
                val (_, nRecommended) = Tools.ANOVA2(sigma2 = Tools.computeResidual(columns))
                solution.setObjective(0, nRecommended.toDouble())
            }
            Constants.TARGET_WORST -> {
                val (_, nRecommended) = Tools.ANOVA2(sigma2 = Tools.computeResidual(columns))
                solution.setObjective(0, nRecommended.toDouble())
            }
        }

        /* PART 2: Constraints evaluation */


        /* PART 3: Solution sorting & storage */

        val solutionsToMoveToAnotherList = mutableListOf<Solution>()
        solutions.forEach { (cardinality, solutionsList) ->
            val solutionToDeleteFromCurrentList = mutableListOf<Solution>()
            solutionsList.forEachIndexed { _, solutionCurrent ->
                if (solutionCurrent.getCardinality() != cardinality) {
                    solutionsToMoveToAnotherList.plusAssign(solutionCurrent)
                    solutionToDeleteFromCurrentList.plusAssign(solutionCurrent)
                }
            }
            solutionsList.removeAll(solutionToDeleteFromCurrentList)
        }

        solutionsToMoveToAnotherList.forEachIndexed { _, solutionToMove ->
            var solutionList = solutions[solutionToMove.getCardinality()]
            if (solutionList != null) {
                solutionList.plusAssign(solutionToMove)
            } else {
                solutionList = mutableListOf()
                solutionList.plusAssign(solutionToMove)
            }
        }

        var solutionsList = solutions[cardinality]
        if (solutionsList != null) {
            solutionsList.plusAssign(solution)
        } else {
            solutionsList = mutableListOf()
            solutionsList.plusAssign(solution)
        }

        solutionsList.sortWith { solutionFirst: Solution, solutionSecond: Solution ->
            if (solutionFirst.getObjective(0) < solutionSecond.getObjective(0)) 1 else if (solutionFirst.getObjective(0) == solutionSecond.getObjective(0)) 0 else -1
        }

        when (parameters.targetToAchieve) {
            Constants.TARGET_BEST -> solutionsList = solutionsList.distinct().reversed().toMutableList()
            Constants.TARGET_WORST -> solutionsList = solutionsList.distinct().toMutableList()
        }

        var objectiveValue = 0.0
        for (solutionCurrent in solutionsList) {
            objectiveValue += solutionCurrent.getObjective(0)
        }
        val mean = objectiveValue / solutionsList.size.toDouble()
        means[cardinality] = mean

        solutions[cardinality] = solutionsList

        /* PART 3: Stopping criterion evaluation */

        if (thresholdCompliance[cardinality] == null) {
            thresholdCompliance[cardinality] = true
        }
        if (hammingCompliance[cardinality] == null) {
            hammingCompliance[cardinality] = true
        }

        if (solutions[cardinality] != null) {
            val solutionList = solutions[cardinality]
            if (solutionList != null) {
                val topSolution = solutionList.first()
                val percentage = ((solution.getObjective(0) - topSolution.getObjective(0)) / topSolution.getObjective(0)) * 100.0
                thresholdMap[cardinality] = percentage
                if (parameters.targetToAchieve == Constants.TARGET_BEST) {
                    if (percentage < improvementThreshold && percentage > 0) {
                        thresholdCompliance[cardinality] = false
                    }
                } else {
                    if (abs(percentage) < improvementThreshold && percentage < 0) {
                        thresholdCompliance[cardinality] = false
                    }
                }
                val distance = HammingDistance()
                val result = distance.apply(topSolution.getRowConfiguration(), solution.getRowConfiguration())
                hammingMap[cardinality] = result
                if (result < hammingThreshold) {
                    hammingCompliance[cardinality] = false
                }
            }
        }

        if (thresholdCompliance.values.toBooleanArray().all { !it }) {
            throw CriterionException("Stopping criterion triggered.")
        }

        iterationCounter++

    }

}

