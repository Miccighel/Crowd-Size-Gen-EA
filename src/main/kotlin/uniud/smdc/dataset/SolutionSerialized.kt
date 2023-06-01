package uniud.smdc.dataset

data class SolutionSerialized(
    var objectiveValue: Any? = null,
    var columnsActual: Any? = null,
    var columnsTotal: Any? = null,
    var columnNames: Any? = null,
    var rowsActual: Any? = null,
    var rowsTotal: Any? = null,
    var rowsNames: Any? = null,
    var columnsConfiguration: Any? = null,
    var rowsConfiguration: Any? = null,
)