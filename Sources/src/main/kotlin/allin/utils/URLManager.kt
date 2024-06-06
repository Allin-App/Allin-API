package allin.utils

import allin.hostIP
import allin.hostPort
import allin.isCodeFirstContainer

class URLManager {
    fun getURL(): String {
        return if (isCodeFirstContainer.isEmpty()) {
            "http://$hostIP:$hostPort/"
        } else "https://codefirst.iut.uca.fr${isCodeFirstContainer}"
    }
}