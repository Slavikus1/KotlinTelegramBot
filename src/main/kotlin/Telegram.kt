import java.util.*
import kotlin.collections.HashMap

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L
    val telegramBotService = TelegramBotService(botToken)
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val response: Response = telegramBotService.getUpdates(lastUpdateId) ?: continue
        println(response)
        if (response.result?.isEmpty() == true) continue
        val sortedUpdates = response.result?.sortedBy { it.updateId }
        sortedUpdates?.forEach { handleUpdate(it, trainers, telegramBotService) }
        if (sortedUpdates != null) {
            lastUpdateId = sortedUpdates.last().updateId + 1
        }
    }
}

fun handleUpdate(
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    service: TelegramBotService,
) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data
    val commandsToReact = listOf("menu", "/start")
    val helloRequest = "Hello"
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer(fileName = "$chatId.txt") }

    if (message?.lowercase(Locale.getDefault())
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } == helloRequest) {
        service.sendMessage(chatId, helloRequest)
    }

    if (message?.lowercase(Locale.getDefault()) in commandsToReact) {
        service.sendMenu(chatId)
    }

    if (data?.lowercase(Locale.getDefault()) == STATISTICS_CLICKED) {
        val statistic: Statistics = trainer.getStatistics()
        service.sendMessage(
            chatId,
            "Выучено ${statistic.learned} из ${statistic.total} | ${statistic.percent}%"
        )
    }

    if (data?.lowercase(Locale.getDefault()) == LEARN_WORDS_CLICKED) {
        service.checkNextQuestionAndSend(trainer, service, chatId)
    }

    if (data?.lowercase(Locale.getDefault())?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(userAnswerIndex)) {
            service.sendMessage(chatId, "Правильно!")
        } else {
            service.sendMessage(
                chatId,
                "Неправильно! ${trainer.question?.correctAnswer?.questionWord} это ${trainer.question?.correctAnswer?.translate}"
            )
        }
        service.checkNextQuestionAndSend(trainer, service, chatId)
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        service.sendMessage(chatId, "Прогресс сброшен")
    }
}

