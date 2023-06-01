import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext

import org.apache.commons.math3.distribution.FDistribution
import org.apache.commons.math3.distribution.NormalDistribution
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

    fun stringComparison(firstString: String, secondString: String): Int {
        var distance = 0
        var i = 0
        while (i < firstString.length) {
            if (secondString[i] != firstString[i]) distance++; i++
        }
        return distance
    }

    fun ANOVA2(alpha: Double = 0.01, beta: Double = 0.05, minD: Double = 0.1, sigma2: Double = 0.26, m: Double = 3.0, maxIter: Int = 1000) : Pair<Int, Int> {

        val lambdaA = 4.86 + 3.584 * sqrt(m-1)
        val minDelta = (minD*minD)/(2*sigma2)
        val nApprox = (lambdaA / minDelta).toInt()

        var largeEnough = true
        var i = 0
        var n = nApprox + 100

        while (largeEnough && i<maxIter) {
            i+=1
            val phiE = m*(n-1)
            var phiA = m-1
            val w = FDistribution(phiA, phiE).inverseCumulativeProbability(1.0 - alpha)
            val cA = (phiA + 2 * n * minDelta)/(phiA + n * minDelta)
            val phiAStar = ((phiA + n * minDelta).pow(2.0) /(phiA+2*n*minDelta))
            val uLessThen = (sqrt(w/phiE)*sqrt(2*phiE-1)-sqrt(cA/phiA)*sqrt(2*phiAStar-1))/sqrt(cA/phiA - w/phiE)
            val oneMinusBetaApprox  = 1 - NormalDistribution(0.0, 1.0).cumulativeProbability(uLessThen)
            largeEnough = (1-beta) < oneMinusBetaApprox
            n -= 1
        }

       var nRecommend = n+2
        if (i == maxIter)
            nRecommend = -1

        return Pair(nApprox, nRecommend)

    }

}