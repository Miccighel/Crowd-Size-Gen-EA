package uniud.smdc.genetic

import krangl.DataCol
import org.uma.jmetal.solution.Solution
import org.uma.jmetal.solution.integersolution.IntegerSolution
import org.apache.logging.log4j.LogManager

class Solution(

    var configuration: MutableList<Int>,

    ) : IntegerSolution {

    private var logger = LogManager.getLogger(this::class.simpleName)

    private var objectives: DoubleArray
    private var constraints: DoubleArray
    private var attributes: MutableMap<Any, Any>

    init {

        this.objectives = DoubleArray(1)
        for (index in 0 until (objectives.size)) setObjective(index, 0.0)
        this.constraints = DoubleArray(0)
        this.attributes = mutableMapOf()
        this.setAttribute("configuration", this.configuration)
    }

    fun updateData(columns: List<DataCol>, columnNumber: Int, rowNumber: Int, rowIdentifiers: MutableList<String>) {
        val columnsSelected = mutableListOf<DataCol>()
        val columnNames = mutableListOf<String>()
        val rowsNames = mutableListOf<String>()
        var columnsActual = 0
        this.configuration.forEachIndexed { index, selected ->
            if (selected == 1) {
                columnsSelected.plusAssign(columns[index])
                columnNames.plusAssign(columns[index].name)
                columnsActual += 1
            }
        }
        this.setAttribute("configuration", this.configuration)
        this.setAttribute("columns", columnsSelected)
        this.setAttribute("columnsTotal", columnNumber)
        this.setAttribute("columnsActual", columnsActual)
        this.setAttribute("columnsNames", columnNames)
        this.setAttribute("rowsTotal", rowNumber)
        @Suppress("UNCHECKED_CAST")
        val columns = this.getAttribute("columns") as MutableList<DataCol>
        var rowsConfiguration = Array(this.getAttribute("rowsTotal") as Int) { 0 }
        for (rowIndex in 0 until this.getAttribute("rowsTotal") as Int) {
            val row = mutableListOf<Double>()
            for (colIndex in 0 until columns.size) {
                val valueCurrent = (columns[colIndex][rowIndex] as String).toDouble()
                row.plusAssign(valueCurrent)
            }
            var rowValid = false
            for (value in row) {
                if (value != -1.0) rowValid = true
            }
            if (rowValid) {
                rowsConfiguration[rowIndex] = 1
                rowsNames.plusAssign(rowIdentifiers[rowIndex])
            }
        }
        this.setAttribute("rowsActual", rowsConfiguration.sum())
        this.setAttribute("rowsNames", rowsNames)
        this.setAttribute("rowsConfiguration", rowsConfiguration.toMutableList())
    }

    fun getCardinality(): Double {
        return this.getAttribute("columnsActual").toString().toDouble()
    }

    fun getRowConfiguration(): String {
        return (this.getAttribute("rowsConfiguration")as List<*>).joinToString("")
    }

    override fun copy(): Solution<Int> {
        return Solution(this.configuration)
    }

    override fun getVariable(index: Int): Int {
        return configuration[index]
    }

    override fun getVariables(): MutableList<Int> {
        return configuration
    }

    override fun setVariable(index: Int, variable: Int?) {
        if (variable != null) {
            configuration[index] = variable
        }
    }

    override fun getNumberOfVariables(): Int {
        return configuration.size
    }

    override fun getLowerBound(index: Int): Int {
        return 0
    }

    override fun getUpperBound(index: Int): Int {
        return 1
    }

    override fun getNumberOfObjectives(): Int {
        return objectives.size
    }

    override fun setObjective(index: Int, value: Double) {
        objectives[index] = value
    }

    override fun getObjective(index: Int): Double {
        return objectives[index]
    }

    override fun getObjectives(): DoubleArray {
        return objectives
    }

    override fun hasAttribute(id: Any?): Boolean {
        return id as String in attributes
    }

    override fun setAttribute(id: Any?, value: Any?) {
        attributes[id as Any] = value as Any
    }

    override fun getAttribute(id: Any?): Any? {
        return attributes[id]
    }

    override fun getAttributes(): MutableMap<Any, Any> {
        return attributes.toMutableMap()
    }

    override fun setConstraint(index: Int, value: Double) {
        this.constraints[index] = value
    }

    override fun getConstraint(index: Int): Double {
        return constraints[index]
    }

    override fun getNumberOfConstraints(): Int {
        return this.constraints.size
    }

    override fun getConstraints(): DoubleArray {
        return this.constraints
    }

}