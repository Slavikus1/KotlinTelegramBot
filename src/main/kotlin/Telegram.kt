import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
        val matchResult = updateIdRegex.find(updates)
        val groups = matchResult?.groups
        val updateIdInt = groups?.get(1)?.value?.toInt()
        println(updateIdInt)

        if (updateIdInt != null) {
            updateId = updateIdInt.toInt() + 1
        }
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client = HttpClient.newBuilder().build()
    val requestUpdates = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
    return response.body()
}