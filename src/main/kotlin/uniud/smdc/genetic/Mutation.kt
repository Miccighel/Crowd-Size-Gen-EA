package uniud.smdc.genetic

import org.apache.logging.log4j.LogManager
import org.uma.jmetal.operator.mutation.MutationOperator
import org.uma.jmetal.solution.integersolution.IntegerSolution
import org.uma.jmetal.util.pseudorandom.JMetalRandom

import kotlin.math.floor

class Mutation(private var probability: Double) : MutationOperator<IntegerSolution> {

    private var logger = LogManager.getLogger(this::class.simpleName)

    override fun execute(solution: IntegerSolution?): IntegerSolution {

        solution as IntegerSolution

        val oldCandidate = solution.variables.toString()

        logger.debug("Num Rel. Doc.: ${solution.variables.sum()}, Pre: $oldCandidate>")

        if (JMetalRandom.getInstance().nextDouble() < probability) {

            var flipIndex = floor(JMetalRandom.getInstance().nextDouble() * solution.numberOfVariables).toInt()
            if (flipIndex == solution.numberOfVariables) flipIndex -= 1
            val oldValue = solution.getVariable(flipIndex)
            val newValue = if (oldValue == 0) 1 else 0
            solution.setVariable(flipIndex, newValue)

        }

        val newCandidate = solution.variables.toString()

        logger.debug("Num Rel. Doc.: ${solution.variables.sum()}, Post: $newCandidate>")

        return solution

    }

    override fun getMutationProbability(): Double {
        return probability
    }

}