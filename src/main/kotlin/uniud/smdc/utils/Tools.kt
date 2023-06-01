package uniud.smdc.utils

import krangl.DataCol

import org.apache.commons.math3.distribution.FDistribution
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext

import kotlin.math.pow
import kotlin.math.sqrt

object Tools {

    fun updateLogger(logger: Logger, level: Level): Logger {
        val currentContext = (LogManager.getContext(false) as LoggerContext)
        val currentConfiguration = currentContext.configuration
        val loggerConfig = currentConfiguration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        loggerConfig.level = level
        currentContext.updateLoggers()
        return logger
    }

    fun ANOVA2(alpha: Double = 0.05, beta: Double = 0.2, minD: Double = 0.05, sigma2: Double = 0.0, m: Double = 2.0, maxIter: Int = 1000): Pair<Int, Int> {

        val lambdaA = 4.86 + 3.584 * sqrt(m - 1)
        val minDelta = (minD * minD) / (2 * sigma2)
        val nApprox = (lambdaA / minDelta).toInt()

        var largeEnough = true
        var i = 0
        var n = nApprox + 100

        while (largeEnough && i < maxIter) {
            i += 1
            val phiE = m * (n - 1)
            var phiA = m - 1
            val w = FDistribution(phiA, phiE).inverseCumulativeProbability(1.0 - alpha)
            val cA = (phiA + 2 * n * minDelta) / (phiA + n * minDelta)
            val phiAStar = ((phiA + n * minDelta).pow(2.0) / (phiA + 2 * n * minDelta))
            val uLessThen = (sqrt(w / phiE) * sqrt(2 * phiE - 1) - sqrt(cA / phiA) * sqrt(2 * phiAStar - 1)) / sqrt(cA / phiA - w / phiE)
            val oneMinusBetaApprox = 1 - NormalDistribution(0.0, 1.0).cumulativeProbability(uLessThen)
            largeEnough = (1 - beta) < oneMinusBetaApprox
            n -= 1
        }

        var nRecommend = n + 2
        if (i == maxIter)
            nRecommend = -1

        return Pair(nApprox, nRecommend)

    }

    fun computeResidual(columns: MutableList<DataCol>): Double {

        var rows = mutableListOf<MutableList<Double>>()
        var rowsFiltered = mutableListOf<MutableList<Double>>()
        var means = emptyList<Double>()

        var rowsNumber = 0
        if (columns.size>0) {
            rowsNumber = columns[0].values().size
        }

        for (rowIndex in 0 until rowsNumber) {
            val row = mutableListOf<Double>()
            val rowFiltered = mutableListOf<Double>()
            for (colIndex in 0 until columns.size) {
                val valueCurrent = (columns[colIndex][rowIndex] as String).toDouble()
                if (valueCurrent != -1.0)
                    rowFiltered.plusAssign(valueCurrent)
                row.plusAssign(valueCurrent)
            }
            var rowValid = false
            for (value in row) {
                if (value != -1.0) rowValid = true
            }
            if (rowValid) {
                rows.plusAssign(row)
                rowsFiltered.plusAssign(rowFiltered)
                val meanCurrent = rowFiltered.average()
                means = means.plus(meanCurrent)
            }
        }

        var sE1 = 0.0
        for (rowIndex in 0 until rows.size) {
            for (colIndex in 0 until rows[rowIndex].size) {
                val valueCurrent = rows[rowIndex][colIndex]
                if (valueCurrent != -1.0) {
                    sE1 += (valueCurrent - means[rowIndex]).pow(2.0)
                }
            }
        }

        var phiE1 = 0.0
        for (rowFiltered in rowsFiltered)
            phiE1 += rowFiltered.size
        phiE1 -= rows.size

        var residual = 0.0

        if (phiE1 != 0.0 && sE1 != 0.0) {
          residual = sE1 / phiE1
        }

        return residual

    }


}