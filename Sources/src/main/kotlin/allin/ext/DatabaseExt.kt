package allin.ext

import org.ktorm.database.Database
import org.ktorm.expression.FunctionExpression
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.IntSqlType
import org.ktorm.schema.VarcharSqlType
import java.sql.ResultSet

fun Database.executeWithResult(request: String): ResultSet? {
    try {
        if (request.isNotEmpty()) {
            return this.useTransaction { transaction ->
                val connection = transaction.connection
                val resultSet = connection.prepareStatement(request).executeQuery()
                resultSet
            }
        }
    } catch (e: Exception) {
        println(e.message)
        return null
    }
    return null
}

fun Database.execute(request: String) {
    if (request.isNotEmpty())
        this.useTransaction {
            val connection = it.connection
            connection.prepareStatement(request).execute()
            connection.commit()
        }
}

fun ColumnDeclaring<String>.length(): FunctionExpression<Int> {
    return FunctionExpression(
        functionName = "LENGTH",
        arguments = listOf(this.asExpression()),
        sqlType = IntSqlType
    )
}

fun ColumnDeclaring<String>.toLowerCase(): FunctionExpression<String> {
    return FunctionExpression(
        functionName = "LOWER",
        arguments = listOf(this.asExpression()),
        sqlType = VarcharSqlType
    )
}

fun ColumnDeclaring<String>.toUpperCase(): FunctionExpression<String> {
    return FunctionExpression(
        functionName = "UPPER",
        arguments = listOf(this.asExpression()),
        sqlType = VarcharSqlType
    )
}

fun ColumnDeclaring<String>.levenshteinLessEq(
    target: ColumnDeclaring<String>,
    max: ColumnDeclaring<Int>
): FunctionExpression<Int> {
    return FunctionExpression(
        functionName = "levenshtein_less_equal",
        arguments = listOf(this.asExpression(), target.asExpression(), max.asExpression()),
        sqlType = IntSqlType
    )
}

fun ColumnDeclaring<String>.levenshteinLessEq(target: String, max: ColumnDeclaring<Int>): FunctionExpression<Int> =
    levenshteinLessEq(
        wrapArgument(target),
        max
    )