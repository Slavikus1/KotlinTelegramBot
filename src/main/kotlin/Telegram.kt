import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates"

    val client = HttpClient.newBuilder().build()

    val request = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()

    val requestUpdates = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

    val response = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())

    println(response.body())
}