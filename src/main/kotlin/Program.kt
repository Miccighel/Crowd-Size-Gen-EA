import org.apache.commons.cli.*

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

import uniud.smdc.utils.Tools.updateLogger
import uniud.smdc.dataset.Parameters
import uniud.smdc.utils.Constants
import uniud.smdc.dataset.Controller

import org.apache.logging.log4j.Logger

import org.uma.jmetal.util.JMetalException

import java.io.File
import java.io.FileNotFoundException

object Program {

    // java -jar Crowd-Size-Gen-1.0-all.jar -fi restry -t Average -i 100000 -p 10000 -c 0.8 -m 0.3 -l Limited -r 5 -XX:-UseGCOverheadLimit.
    // java -jar Crowd-Size-Gen-1.0-all.jar -fi restry -t Best -i 100000 -p 10000 -c 0.8 -m 0.3 -l Limited -r 5 -XX:-UseGCOverheadLimit.
    // java -jar Crowd-Size-Gen-1.0-all.jar -fi restry -t Worst -i 100000 -p 10000 -c 0.8 -m 0.3 -l Limited -r 5 -XX:-UseGCOverheadLimit.

    @JvmStatic

    fun main(arguments: Array<String>) {

        val commandLine: CommandLine
        val parser: CommandLineParser
        val options = loadCommandLineOptions()
        val datasetController: Controller
        val datasetPath: String
        val datasetName: String

        val targetToAchieve: String
        var numberOfIterations: Int
        var numberOfRepetitions: Int
        var populationSize: Int
        var mutationProbability: Double
        var crossoverProbability: Double
        var matrixKind: String
        val loggingLevel: Level
        var logger: Logger

        System.setProperty("baseLogFileName", "${Constants.LOG_PATH}${Constants.LOG_FILE_NAME}${Constants.LOG_FILE_SUFFIX}")
        logger = updateLogger(LogManager.getLogger(this::class.simpleName), Level.INFO)

        try {

            parser = DefaultParser()
            commandLine = parser.parse(options, arguments)
            datasetName = commandLine.getOptionValue("fi")
            datasetPath = "${Constants.PROJECT_INPUT_PATH}$datasetName.csv"

            if (!File(datasetPath).exists()) throw FileNotFoundException("Dataset file does not exists Path: \"$datasetPath\"") else {

                if (commandLine.getOptionValue("l") == "Verbose" || commandLine.getOptionValue("l") == "Limited" || commandLine.getOptionValue("l") == "Off") {
                    when (commandLine.getOptionValue("l")) {
                        "Verbose" -> loggingLevel = Level.DEBUG
                        "Limited" -> loggingLevel = Level.INFO
                        "Off" -> loggingLevel = Level.OFF
                        else -> loggingLevel = Level.INFO
                    }
                } else throw ParseException("Value for the option <<l>> or <<log>> is wrong. Check the usage section below.")

                if (commandLine.getOptionValue("t") == Constants.TARGET_BEST || commandLine.getOptionValue("t") == Constants.TARGET_WORST || commandLine.getOptionValue("t") == Constants.TARGET_AVERAGE || commandLine.getOptionValue("t") == Constants.TARGET_ALL) {

                    targetToAchieve = commandLine.getOptionValue("t")
                    numberOfRepetitions = 0

                    if (!commandLine.hasOption("i")) throw ParseException("Value for the option <<i>> or <<iter>> is missing. Check the usage section below.")
                    try {
                        numberOfIterations = Integer.parseInt(commandLine.getOptionValue("i"))
                        if (numberOfIterations <= 0) throw ParseException("Value for the option <<i>> or <<iter>> must be a positive value. Check the usage section below")
                    } catch (exception: NumberFormatException) {
                        throw ParseException("Value for the option <<i>> or <<iter>> is not an integer. Check the usage section below")
                    }

                    if (!commandLine.hasOption("p")) throw ParseException("Value for the option <<p>> or <<pop>> is missing. Check the usage section below.")
                    try {
                        populationSize = Integer.parseInt(commandLine.getOptionValue("p"))
                        if (populationSize <= 0) throw ParseException("Value for the option <<p>> or <<pop>> must be a positive value. Check the usage section below")
                    } catch (exception: NumberFormatException) {
                        throw ParseException("Value for the option <<po>> or <<pop>> is not an integer. Check the usage section below")
                    }


                    if (!commandLine.hasOption("c")) throw ParseException("Value for the option <<c>> or <<crossoverProb>> is missing. Check the usage section below.")
                    try {
                        crossoverProbability = commandLine.getOptionValue("c").toDouble()
                        if (crossoverProbability < 0 || crossoverProbability > 1) throw ParseException("Value for the option <<c>> or <<crossoverProb>> must be a decimal value between 0 and 1. Check the usage section below")
                    } catch (exception: NumberFormatException) {
                        throw ParseException("Value for the option <<c>> or <<crossoverProb>> is not a decimal. Check the usage section below")
                    }

                    if (!commandLine.hasOption("m")) throw ParseException("Value for the option <<m>> or <<mutationProb>> is missing. Check the usage section below.")
                    try {
                        mutationProbability = commandLine.getOptionValue("m").toDouble()
                        if (mutationProbability < 0 || mutationProbability > 1) throw ParseException("Value for the option <<m>> or <<mutationProb>> must be a decimal value between 0 and 1. Check the usage section below")
                    } catch (exception: NumberFormatException) {
                        throw ParseException("Value for the option <<m>> or <<mutationProb>> is not a decimal. Check the usage section below")
                    }

                    if (targetToAchieve == Constants.TARGET_ALL || targetToAchieve == Constants.TARGET_AVERAGE) {

                        if (!commandLine.hasOption("r")) throw ParseException("Value for the option <<r>> or <<rep>> is missing. Check the usage section below.")
                        try {
                            numberOfRepetitions = Integer.parseInt(commandLine.getOptionValue("r"))
                            if (numberOfRepetitions <= 0) throw ParseException("Value for the option <<r>> or <<rep>> must be a positive value. Check the usage section below")
                        } catch (exception: NumberFormatException) {
                            throw ParseException("Value for the option <<r>> or <<rep>> is not an integer. Check the usage section below")
                        }

                    }

                    matrixKind = commandLine.getOptionValue("k")

                    logger = updateLogger(LogManager.getLogger(this::class.simpleName), loggingLevel)

                    logger.info("${Constants.PROJECT_NAME} execution started.")
                    logger.info("--------------------")
                    logger.info("Base path:")
                    logger.info("\"${Constants.BASE_PATH}\"")
                    logger.info("${Constants.PROJECT_NAME} path:")
                    logger.info("\"${Constants.PROJECT_PATH}\"")
                    logger.info("${Constants.PROJECT_NAME} input path:")
                    logger.info("\"${Constants.PROJECT_INPUT_PATH}\"")
                    logger.info("${Constants.PROJECT_NAME} output path:")
                    logger.info("\"${Constants.PROJECT_OUTPUT_PATH}\"")
                    logger.info("${Constants.PROJECT_NAME} log path:")
                    logger.info("\"${Constants.LOG_PATH}\"")
                    logger.info("--------------------")
                    logger.info("uniud.smdc.dataset.Parameters:")
                    logger.info("Dataset Name: $datasetName")
                    logger.info("Target: $targetToAchieve")
                    logger.info("Iterations Number: $numberOfIterations")
                    logger.info("Repetitions Number: $numberOfRepetitions")
                    logger.info("Population Size: $populationSize")
                    logger.info("Crossover Probability: $crossoverProbability")
                    logger.info("Mutation Probability: $mutationProbability")
                    logger.info("Matrix Kind: $matrixKind")
                    logger.info("--------------------")

                    datasetController = Controller(targetToAchieve)
                    datasetController.solve(Parameters(datasetName, targetToAchieve, numberOfIterations, numberOfRepetitions, populationSize, crossoverProbability, mutationProbability, matrixKind))

                    logger.info("--------------------")
                    logger.info("${Constants.PROJECT_NAME} execution terminated.")

                } else throw ParseException("Value for the option <<t>> or <<target>> is wrong. Check the usage section below.")
            }

        } catch (exception: ParseException) {

            logger.error(exception.message)
            val formatter = HelpFormatter()
            formatter.printHelp(Constants.PROJECT_NAME, options)
            logger.error("End of the usage section.")
            logger.info("--------------------")
            logger.info("${Constants.PROJECT_NAME} execution terminated.")

        } catch (exception: FileNotFoundException) {

            logger.error(exception.message)
            logger.info("--------------------")
            logger.info("${Constants.PROJECT_NAME} execution terminated.")

        } catch (exception: FileSystemException) {

            logger.error(exception.message)
            logger.info("--------------------")
            logger.info("${Constants.PROJECT_NAME} execution terminated.")

        } catch (exception: JMetalException) {

            logger.error(exception.message)
            logger.info("--------------------")
            logger.info("${Constants.PROJECT_NAME} execution terminated.")

        } catch (exception: OutOfMemoryError) {

            logger.error("${Constants.PROJECT_NAME} hasn't enough heap space to go further. Please, launch it with option -XX:-UseGCOverheadLimit.")
            logger.info("Example: java -jar <jar name> -UseGCOverheadLimit <parameters>")
            logger.info("--------------------")
            logger.info("${Constants.PROJECT_NAME} execution terminated.")

        }

    }

    private fun loadCommandLineOptions(): Options {

        val options = Options()
        var source = Option.builder("fi").longOpt("fileIn").desc("Relative path to the CSV dataset file (do not use any extension in filename) [REQUIRED].").hasArg().argName("Source File").required().build()
        options.addOption(source)
        source = Option.builder("t").longOpt("targ").desc("Target that must be achieved. Available targets: Best, Worst, Average, All. [REQUIRED]").hasArg().argName("Target").required().build()
        options.addOption(source)
        source = Option.builder("l").longOpt("log").desc("Required level of logging. Available levels: Verbose, Limited, Off. [REQUIRED]").required().hasArg().argName("Logging Level").build()
        options.addOption(source)
        source = Option.builder("i").longOpt("iter").desc("Number of iterations to be done. It is mandatory only if the selected target is: Best, Worst, All. [OPTIONAL]").hasArg().argName("Number of Iterations").build()
        options.addOption(source)
        source = Option.builder("r").longOpt("rep").desc("Number of repetitions to be done to compute a single cardinality during Average experiment. It must be a positive integer value. It is mandatory only if the selected target is: Average, All. [OPTIONAL]").hasArg().argName("Number of Repetitions").build()
        options.addOption(source)
        source = Option.builder("p").longOpt("pop").desc("Size of the initial population to be generated. It must be an integer value. It must be greater or equal than/to of the number of topics of the data set. It must be greater than the value for the option <<mx>> or <<max>> if this one is used.a It is mandatory only if the selected target is: Best, Worst, All. [OPTIONAL]").hasArg().argName("Population Size").build()
        options.addOption(source)
        source = Option.builder("c").longOpt("crossProb").desc("Probability of triggering a crossover operation. It must be a decimal value between 0 and 1. [REQUIRED]").hasArg().argName("Crossover Probability").required().build()
        options.addOption(source)
        source = Option.builder("m").longOpt("mutProb").desc("Probability of triggering a mutation operation. It must be a decimal value between 0 and 1. [REQUIRED]").hasArg().argName("Mutation Probability").required().build()
        options.addOption(source)
        source = Option.builder("k").longOpt("kind").desc("Kind of matrix to consider. It must be a value between WxD and DxW. [REQUIRED]").hasArg().argName("Matrix Kind").required().build()
        options.addOption(source)
        return options

    }

}
