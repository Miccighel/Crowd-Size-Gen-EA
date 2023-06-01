import org.apache.commons.cli.*

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

import Tools.updateLogger
import Tools.ANOVA2

object Program {

    @JvmStatic
    fun main(arguments: Array<String>) {

        val commandLine: CommandLine
        val parser: CommandLineParser
        val options = loadCommandLineOptions()
        val logger = updateLogger(LogManager.getLogger(this::class.simpleName), Level.INFO)

        val iterationsNumber: Int
        val populationSize: Int
        val crossoverProbability: Double
        val mutationProbability: Double

        logger.info("Crowd-Size-Gen execution started.")

        try {

            parser = DefaultParser()
            commandLine = parser.parse(options, arguments)

            iterationsNumber = commandLine.getOptionValue("it").toInt()
            populationSize = commandLine.getOptionValue("po").toInt()
            crossoverProbability = commandLine.getOptionValue("cp").toDouble()
            mutationProbability = commandLine.getOptionValue("mp").toDouble()

            logger.info("Printing parsed options:")
            logger.info("--------------------")
            logger.info("iterationsNumber: $iterationsNumber")
            logger.info("populationSize: $populationSize")
            logger.info("crossoverProbability: $crossoverProbability")
            logger.info("mutationProbability: $mutationProbability")
            logger.info("--------------------")

            logger.info("ANOVA2 Sample: ${ANOVA2()}")

            logger.info("Crowd-Size-Gen execution terminated.")

        } catch (exception: ParseException) {

            val formatter = HelpFormatter()
            logger.error(exception.message)
            formatter.printHelp("Crowd-Size-Gen", options)
            logger.error("End of the usage section.")
            logger.info("Crowd-Size-Gen execution terminated.")
        }

    }

    private fun loadCommandLineOptions(): Options {

        val options = Options()
        options.addOption(Option.builder("it").longOpt("iterationsNumber").desc("Number of iterations to perform [REQUIRED]").hasArg().argName("Iterations Number").required().build())
        options.addOption(Option.builder("po").longOpt("populationSize").desc("Size of the initial population to generate [REQUIRED]").hasArg().argName("Population Size").required().build())
        options.addOption(Option.builder("cp").longOpt("crossoverProbability").desc("Probability of triggering a crossover operation.  [REQUIRED]").hasArg().argName("Crossover Probability").required().build())
        options.addOption(Option.builder("mp").longOpt("mutationProbability").desc("Probability of triggering a mutation operation [REQUIRED]").hasArg().argName("Mutation Probability").required().build())
        options.addOption(Option.builder("rp").longOpt("resultPath").desc("Relative path to the directory in which to store the results [REQUIRED]").hasArg().argName("Result Path").required().build())
        options.addOption(Option.builder("fn").longOpt("filename").desc("Name of the file in which to store the results [REQUIRED]").hasArg().argName("File Name").required().build())

        return options

    }

}
