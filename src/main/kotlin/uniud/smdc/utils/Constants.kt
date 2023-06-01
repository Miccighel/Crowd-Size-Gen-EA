package uniud.smdc.utils

import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    val PATH_SEPARATOR = System.getProperty("file.separator").toString()
    val FILE_NAME_SEPARATOR = "-"
    val BASE_PATH = "${Paths.get("").toAbsolutePath().parent}$PATH_SEPARATOR"
    val PROJECT_NAME = "Crowd-Size-Gen"
    val PROJECT_PATH = "${Paths.get("").toAbsolutePath()}$PATH_SEPARATOR"
    val PROJECT_INPUT_PATH = "${PROJECT_PATH}resources$PATH_SEPARATOR"
    val PROJECT_OUTPUT_PATH = "${PROJECT_PATH}result$PATH_SEPARATOR"
    val LOG_PATH = "${PROJECT_PATH}log$PATH_SEPARATOR"
    val LOG_FILE_NAME = "execution$FILE_NAME_SEPARATOR${SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(Date())}"
    val LOG_FILE_SUFFIX = ".log"
    val LOGGING_FACTOR = 10
    val TARGET_BEST = "Best"
    val TARGET_WORST = "Worst"
    val TARGET_AVERAGE = "Average"
    val TARGET_ALL = "All"
    val SOLUTIONS_FILE_SUFFIX = "${FILE_NAME_SEPARATOR}sol$FILE_NAME_SEPARATOR${SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(Date())}"
    val CSV_FILE_EXTENSION = ".csv"
    val JSON_FILE_EXTENSION = ".json"
}