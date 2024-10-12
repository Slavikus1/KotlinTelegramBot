import java.lang.Exception
import java.util.*

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

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val textRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":\\s*(\\d+)".toRegex()
    val commandsToReact = listOf("menu", "/start")
    val helloRequest = "Hello"

    while (true) {
        val statistic: Statistics = trainer.getStatistics()

        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1
        chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toLong()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value
        val text = textRegex.find(updates)?.groups?.get(1)?.value

        if (chatId == null) continue


        if (text?.lowercase(Locale.getDefault())?.capitalize() == helloRequest) {
            telegramBotService.sendMessage(chatId, helloRequest)
        }

        if (text?.lowercase(Locale.getDefault()) in commandsToReact) {
            telegramBotService.sendMenu(chatId, helloRequest)
        }

        if (data?.lowercase(Locale.getDefault()) == STATISTICS_CLICKED) {
            telegramBotService.sendMessage(
                chatId,
                "Выучено ${statistic.learned} из ${statistic.total} | ${statistic.percent}%"
            )
        }

        if (data?.lowercase(Locale.getDefault()) == LEARN_WORDS_CLICKED) {
            telegramBotService.checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }

        if (data?.lowercase(Locale.getDefault())?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBotService.sendMessage(chatId, "Правильно!")
            } else {
                telegramBotService.sendMessage(
                    chatId,
                    "Неправильно! ${trainer.question?.correctAnswer?.questionWord} это ${trainer.question?.correctAnswer?.translate}"
                )
            }
            telegramBotService.checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }
    }
}

