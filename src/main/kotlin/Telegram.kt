import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main() {
    var updateId: Int? = 0
    var chatId: Long?
    val telegramBotService = TelegramBotService()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
        val matchResultId: MatchResult? = updateIdRegex.find(updates)
        val groupsId = matchResultId?.groups
        val updateIdInt = groupsId?.get(1)?.value?.toInt()?.plus(1)
        updateId = updateIdInt

        val chatIdRegex: Regex = "\"chat\":\\{\"id\":\\s*(\\d+)".toRegex()
        val matchResultChat: MatchResult? = chatIdRegex.find(updates)
        val groupsChat = matchResultChat?.groups
        chatId = groupsChat?.get(1)?.value?.toLong()

        val textRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResultText: MatchResult? = textRegex.find(updates)
        val groupsText = matchResultText?.groups
        val userText = groupsText?.get(1)?.value

        val helloRequest = "Hello"
        if (userText?.toLowerCase()?.capitalize() == helloRequest) {
            if (chatId != null) {
                telegramBotService.sendMessage(chatId, helloRequest)
            }
        }
    }
}

