import java.lang.Exception

fun main() {
    var lastUpdateId: Int? = 0
    var chatId: Long?
    val telegramBotService = TelegramBotService()

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    val statistic: Statistics = trainer.getStatistics()

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val textRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":\\s*(\\d+)".toRegex()
    val commandsToReact = listOf("menu", "/start")
    val helloRequest = "Hello"

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1
        chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toLong()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value
        val text = textRegex.find(updates)?.groups?.get(1)?.value


        if (text?.toLowerCase()?.capitalize() == helloRequest && chatId != null) {
            telegramBotService.sendMessage(chatId, helloRequest)
        }

        if (text?.toLowerCase() in commandsToReact && chatId != null) {
            telegramBotService.sendMenu(chatId, helloRequest)
        }

        if (data?.toLowerCase() == STATISTICS_CLICKED && chatId != null) {
            telegramBotService.sendMessage(
                chatId,
                "Выучено ${statistic.learned} из ${statistic.total} | ${statistic.percent}%"
            )
        }
    }
}

