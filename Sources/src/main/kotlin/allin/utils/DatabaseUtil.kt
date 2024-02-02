package allin.utils

import allin.database
import org.ktorm.database.Database
import java.sql.ResultSet

fun Database.Execute(request: String): ResultSet? {
    try {
        if (!request.isNullOrEmpty()) {
            return database.useTransaction { transaction ->
                val connection = transaction.connection
                val resultSet = connection.prepareStatement(request).executeQuery()
                resultSet
            }
        }
    } catch (e: Exception){
        println(e.message)
        return null
    }
    return null
}