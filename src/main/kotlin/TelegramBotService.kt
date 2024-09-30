import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_DOMAIN = "https://api.telegram.org/bot"

class TelegramBotService(
    private val botToken: String = "7540450751:AAGmgUThMhOmQ2T60f9KXbn4WFxw3oIA2Ts",
    private val httpClient: HttpClient = HttpClient.newBuilder().build(),
) {
    fun getUpdates(updateId: Int?): String {
        val urlGetUpdates = "$TELEGRAM_DOMAIN$botToken/getUpdates?offset=$updateId"
        val requestUpdates = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = httpClient.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage?chat_id=$chatId&text=$text"
        val requestMessage = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = httpClient.send(requestMessage, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}