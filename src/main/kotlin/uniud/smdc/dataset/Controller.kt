package uniud.smdc.dataset

import uniud.smdc.utils.Constants
import org.apache.logging.log4j.LogManager
import uniud.smdc.utils.CriterionException

import java.io.*

class Controller(

    private var targetToAchieve: String

) {

    private var logger = LogManager.getLogger(this::class.simpleName)
    private var model: Model
    private var view: View

    init {

        logger.info("Checking if ${Constants.PROJECT_NAME} output dir. exists.")

        val outputDirectory = File(Constants.PROJECT_OUTPUT_PATH)
        if (!outputDirectory.exists()) {
            logger.info("Output dir. not exists.")
            if (outputDirectory.mkdirs()) {
                logger.info("Output dir. created.")
                logger.info("Path: \"${Constants.PROJECT_OUTPUT_PATH}\".")
            }
        } else {
            logger.info("Output dir. already exists.")
            logger.info("Output dir. creation skipped.")
            logger.info("Path: \"${Constants.PROJECT_OUTPUT_PATH}\".")
        }
        model = Model()
        view = View()

    }

    fun solve(parameters: Parameters) {
        model.updateParameters(parameters)
        model.loadData()
        try {
            model.solve()
            if (targetToAchieve == Constants.TARGET_AVERAGE) {
                view.print(model, merge())
            } else {
                view.print(model)
            }
        } catch (exception: CriterionException) {
            logger.info(exception.message)
            view.print(model)
        }

    }

    private fun merge(): Map<Double, Double> {
        val meansAggregated = linkedMapOf<Double, Double>()
        val meansAveraged = linkedMapOf<Double, MutableList<Double>>()
        for ((cardinality, mean) in model.problem.means) {
            if (meansAveraged[cardinality] != null) {
                meansAveraged[cardinality]?.plusAssign(mean)
            } else {
                meansAveraged[cardinality] = mutableListOf()
                meansAveraged[cardinality]?.plusAssign(mean)
            }
        }
        for ((cardinality, meansCurrent) in meansAveraged) {
            meansAggregated[cardinality] = meansCurrent.average()
        }
        return meansAggregated
    }

}