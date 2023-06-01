package uniud.smdc.genetic

import org.apache.logging.log4j.LogManager

import org.uma.jmetal.operator.crossover.CrossoverOperator
import org.uma.jmetal.solution.integersolution.IntegerSolution
import org.uma.jmetal.util.pseudorandom.JMetalRandom

import kotlin.math.abs
import kotlin.math.pow

class Crossover(

    private var probability: Double

) : CrossoverOperator<IntegerSolution> {

    private var logger = LogManager.getLogger(this::class.simpleName)

    override fun execute(solutionList: List<IntegerSolution>): List<IntegerSolution> {

        /* Paper describing the algorithm:
         * Title: An Efficient Constraint Handling Method for Genetic Algorithms
         * Author: Kalyanmoy Deb
         * More info: Appendix A. Page 30.
         * Implementation reference: https://gist.github.com/Tiagoperes/1779d5f1c89bae0cfdb87b1960bba36d
         */

        val generator = JMetalRandom.getInstance()

        val firstParent = solutionList[0] as Solution
        val secondParent = solutionList[1] as Solution

        var firstChild = firstParent.copy() as Solution
        var secondChild = secondParent.copy() as Solution

        val eps = 1.0e-14  // precision error tolerance, its value is 1.0e-14;
        val etaC = 10 // represents "nc" in the paper, ie. the distribution index for the crossover. In the original NSGA-II implementation, eta_c is given by the user. 10 or 5 is used as default value.

        var alpha : Double
        var beta : Double
        var betaQ : Double // betaQ, in the original paper, is the symbol beta with a line above

        var targetFirst: Int // targetFirst stores a gene's value for the 1st child
        var targetSecond: Int // targetSecond stores a gene's value for the 2nd child
        var targetLowerBound : Int // targetLowerBound holds the lower limit for the variable
        var targetUpperBound : Int // targetUpperBound holds the upper limit for the variable

        if(generator.nextDouble() <= probability) { // Roll the dices. Should we apply a crossover?

            for (i in 0 until firstParent.numberOfVariables) { // For each variable of a solution (individual)

                if(generator.nextDouble() <= 0.5) { // According to the paper, each variable in a solution has a 50% chance of changing its value. This should be removed when dealing with one-dimensional solutions.

                    // The following if/else block puts the lowest value between firstParent and secondParent in targetFirst and the other in targetSecond

                    if(firstParent.getVariable(i) < secondParent.getVariable(i)) {
                        targetFirst = firstParent.getVariable(i)
                        targetSecond = secondParent.getVariable(i)
                    } else {
                        targetSecond = firstParent.getVariable(i)
                        targetFirst = secondParent.getVariable(i)
                    }

                    if(abs(firstParent.getVariable(i)-secondParent.getVariable(i)) > eps) {  // If the value in firstParent is not the same of secondParent

                        targetLowerBound = firstParent.getLowerBound(i)
                        targetUpperBound = firstParent.getUpperBound(i)

                        // FIRST CHILD COMPUTATION

                        val random = generator.nextDouble()

                        beta = 1.0 + (2.0*(targetFirst-targetLowerBound)/(targetSecond-targetLowerBound)) // It differs from the paper here. The paper uses one value of beta for calculating both children. Here, we use one beta for each child.
                        alpha = 2.0 - beta.pow(-(etaC + 1.0)) // Calculation of alpha as described in the paper

                        betaQ = (if( random <= (1.0/alpha)) (random*alpha).pow(1.0/(etaC+1.0)) else (1.0/(2.0-random*alpha)).pow(1.0/(etaC+1.0))) // Calculation of betaQ as described in the paper

                        firstChild.setVariable(i, (0.5 * ((targetFirst + targetSecond) - betaQ*(targetSecond-targetFirst))).toInt()) // Calculation of the first child as described in the paper

                        // SECOND CHILD COMPUTATION

                        beta = 1.0 + (2.0*((targetUpperBound-targetSecond)/(targetSecond-targetFirst))) // Differs from the paper. The second value of beta uses the upper limit (targetUpperBound) and the maximum between firstParent and secondParent (targetSecond)
                        alpha = 2.0 - beta.pow(-(etaC + 1.0)) // Calculation of alpha as described in the paper

                        betaQ = if( random <= (1.0/alpha)) (random*alpha).pow(1.0/(etaC+1.0)) else (1.0/(2.0-random*alpha)).pow(1.0/(etaC+1.0)) // Calculation of betaQ as described in the paper

                        secondChild.setVariable(i, (0.5*((targetFirst+targetSecond) + betaQ * (targetSecond-targetFirst))).toInt()) // Calculation of the second child as described in the paper

                        /* The paper is not very clear about this, but I assume, in the equation of beta (not betaQ), targetSecond and targetFirst, since they could not have been calculated yet, refer to the parents.
                         * So, if both parents are equal at the specified variable, the divisor would be zero. In this case, the children should have the same value as the parents. */

                    } else { // If the i-th variable has the same value in both parents

                        firstChild.setVariable(i, firstParent.getVariable(i))
                        secondChild.setVariable(i, secondParent.getVariable(i))

                    }

                } else { // 50% chance of changing values. In the case random > 0.5, the children should have the same value as the parents

                    firstChild.setVariable(i, firstParent.getVariable(i))
                    secondChild.setVariable(i, secondParent.getVariable(i))

                }

            }

        } else { // If the random number generated is greater than the crossover rate, return the children as exact clones of the parents

            firstChild = firstParent.copy() as Solution
            secondChild = secondParent.copy() as Solution

        }

        logger.debug("Num. Rel. Doc: ${firstParent.variables.sum()}, Parent 1: ${firstParent.variables}>")
        logger.debug("Num. Rel. Doc: ${secondParent.variables.sum()}, Parent 2: ${secondParent.variables}>")
        logger.debug("Num. Rel. Doc: ${firstChild.variables.sum()}, Children 1: ${firstChild.variables}>")
        logger.debug("Num. Rel. Doc: ${secondChild.variables.sum()}, Children 2: ${secondChild.variables}>")

        return listOf(firstChild, secondChild)
    }

    override fun getCrossoverProbability(): Double {
        return probability
    }

    override fun getNumberOfRequiredParents(): Int {
        return 2
    }

    override fun getNumberOfGeneratedChildren(): Int {
        return 2
    }


}
