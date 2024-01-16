package allin.utils

import allin.database
import org.ktorm.database.Database

fun Database.Execute(request: String){
    if(!request.isNullOrEmpty())
        database.useTransaction {
            val connection = it.connection
            connection.prepareStatement(request).execute()
            connection.commit()
        }
}