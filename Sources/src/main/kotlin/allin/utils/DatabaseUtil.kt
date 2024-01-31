package allin.utils

import org.ktorm.database.Database

fun Database.Execute(request: String) {
    if (request.isNotEmpty())
        this.useTransaction {
            val connection = it.connection
            connection.prepareStatement(request).execute()
            connection.commit()
        }
}