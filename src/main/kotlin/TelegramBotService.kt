import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val TELEGRAM_DOMAIN = "https://api.telegram.org/bot"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val ALL_WORDS_LEARNED = "Вы выучили все слова в базе"

class TelegramBotService(
    private val botToken: String = "7540450751:AAGmgUThMhOmQ2T60f9KXbn4WFxw3oIA2Ts"
) {
    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int?): String {
        val urlGetUpdates = "$TELEGRAM_DOMAIN$botToken/getUpdates?offset=$updateId"
        val requestUpdates = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = httpClient.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }


    fun sendMessage(chatId: Long, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val requestMessage = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = httpClient.send(requestMessage, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long, text: String): String {
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучать слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendUserQuestion(chatId: Long, question: Question): String {
        val text = question.correctAnswer.questionWord
        val optionsJson = question.variants.mapIndexed { index, word ->
            """
            {
                "text": "${word.translate}",
                "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX$index"
            }
            """
        }.joinToString(",")

        val inlineKeyboard = """
            {
                "chat_id": $chatId,
                "text": "$text",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            $optionsJson
                        ]
                    ]
                }
            }
        """.trimIndent()
        val urlSendMessage = "$TELEGRAM_DOMAIN$botToken/sendMessage"
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(inlineKeyboard))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, telegramBotService: TelegramBotService, chatId: Long) {
        val newQuestion = trainer.getNextQuestion()
        if (newQuestion == null) {
            telegramBotService.sendMessage(chatId, ALL_WORDS_LEARNED)
        } else telegramBotService.sendUserQuestion(chatId, newQuestion)
    }
}