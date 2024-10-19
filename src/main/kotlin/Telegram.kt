import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.HashMap

fun main() {
    var lastUpdateId = 0L
    val telegramBotService = TelegramBotService()
    val json = Json { ignoreUnknownKeys = true }
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        val response: Response = json.decodeFromString(responseString)
        println(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, trainers, telegramBotService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, json: Json, trainers: HashMap<Long, LearnWordsTrainer>, service: TelegramBotService) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data
    val commandsToReact = listOf("menu", "/start")
    val helloRequest = "Hello"
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer(fileName = "$chatId.txt") }

    if (message?.lowercase(Locale.getDefault())?.capitalize() == helloRequest) {
        service.sendMessage(json, chatId, helloRequest)
    }

    if (message?.lowercase(Locale.getDefault()) in commandsToReact) {
        service.sendMenu(json, chatId, helloRequest)
    }

    if (data?.lowercase(Locale.getDefault()) == STATISTICS_CLICKED) {
        val statistic: Statistics = trainer.getStatistics()
        service.sendMessage(
            json,
            chatId,
            "Выучено ${statistic.learned} из ${statistic.total} | ${statistic.percent}%"
        )
    }

    if (data?.lowercase(Locale.getDefault()) == LEARN_WORDS_CLICKED) {
        service.checkNextQuestionAndSend(json, trainer, service, chatId)
    }

    if (data?.lowercase(Locale.getDefault())?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(userAnswerIndex)) {
            service.sendMessage(json, chatId, "Правильно!")
        } else {
            service.sendMessage(
                json,
                chatId,
                "Неправильно! ${trainer.question?.correctAnswer?.questionWord} это ${trainer.question?.correctAnswer?.translate}"
            )
        }
        service.checkNextQuestionAndSend(json, trainer, service, chatId)
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        service.sendMessage(json, chatId, "Прогресс сброшен")
    }
}

