package allin.ext

import org.ktorm.database.Database
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