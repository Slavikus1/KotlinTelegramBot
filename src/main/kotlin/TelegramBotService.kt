import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val TELEGRAM_DOMAIN = "https://api.telegram.org/bot"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val ALL_WORDS_LEARNED = "Вы выучили все слова в базе"
const val RESET_CLICKED = "reset_clicked"

class TelegramBotService(
    private val botToken: String,
) {
    private val httpClient: HttpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    fun getUpdates(updateId: Long): Response? {
        val urlGetUpdates = "$TELEGRAM_DOMAIN$botToken/getUpdates?offset=$updateId"
        val requestUpdates = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val result: Result<HttpResponse<String>> =
            runCatching { httpClient.send(requestUpdates, HttpResponse.BodyHandlers.ofString()) }
        return if (result.isSuccess) {
            result.getOrNull()?.body()?.let { json.decodeFromString(it) }
        } else {
            println("Error: ${result.exceptionOrNull()?.message ?: "Some error"}")
            null
        }
    }


    fun sendMessage(chatId: Long, text: String): String? {
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val result: Result<HttpResponse<String>> =
            runCatching { httpClient.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (result.isSuccess) {
            result.getOrNull()?.body()
        } else {
            println("Error: ${result.exceptionOrNull()?.message ?: "Some error"}")
            null
        }
    }

    fun sendMenu(chatId: Long): String? {
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED))
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val result: Result<HttpResponse<String>> =
            runCatching { httpClient.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (result.isSuccess) {
            result.getOrNull()?.body()
        } else {
            println("Error: ${result.exceptionOrNull()?.message ?: "Some error"}")
            null
        }
    }

    private fun sendUserQuestion(chatId: Long, question: Question): String? {
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.questionWord,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                })
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val result: Result<HttpResponse<String>> =
            runCatching { httpClient.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (result.isSuccess) {
            result.getOrNull()?.body()
        } else {
            println("Error: ${result.exceptionOrNull()?.message ?: "Some error"}")
            null
        }
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        telegramBotService: TelegramBotService,
        chatId: Long,
    ) {
        val newQuestion = trainer.getNextQuestion()
        if (newQuestion == null) {
            telegramBotService.sendMessage(chatId, ALL_WORDS_LEARNED)
        } else telegramBotService.sendUserQuestion(chatId, newQuestion)
    }
}