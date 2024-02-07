package allin.utils

import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
fun kronJob(duration: Duration, action: () -> Unit) =
    GlobalScope.launch {
        withContext(Dispatchers.IO) {
            while (true) {
                runCatching { action() }
                delay(duration.inWholeMilliseconds)
            }
        }
    }
