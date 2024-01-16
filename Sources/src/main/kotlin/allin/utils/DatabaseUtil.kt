package allin.utils

import allin.database
import org.ktorm.database.Database

fun Database.Execute(request: String){
    database.useTransaction {
        val connection = it.connection
        connection.prepareStatement(request).execute()
        connection.commit()
    }
}