import java.io.File

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println("Меню: 1 - Учить слова, 2 - Статистика, 0 - Выход")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }

                    if (notLearnedList.isEmpty()) {
                        println("Все слова выучены")
                        break
                    } else {
                        val questionWords = notLearnedList.take(4).shuffled()
                        val correctAnswer = questionWords.random()
                        println(
                            "${correctAnswer.questionWord}\n" +
                                    "1 - ${questionWords[0].translate}, 2 - ${questionWords[1].translate}, " +
                                    "3 - ${questionWords[2].translate}, 4 - ${questionWords[3].translate}, 0 - В меню"
                        )

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break
                        val correctAnswerIndex = questionWords.indexOf(correctAnswer)

                        if (userAnswerInput == correctAnswerIndex + 1) {
                            correctAnswer.correctAnswersCount++
                            saveDictionary(dictionary)
                            println("Правильно!\n")
                        } else {
                            println("Неправильно! ${correctAnswer.questionWord} - это ${correctAnswer.translate}\n")
                        }
                    }
                }
            }

            2 -> {
                val learned = dictionary.filter { it.correctAnswersCount >= 3 }.size
                val total = dictionary.size
                val percent = learned * 100 / total
                println("Выучено $learned из $total | $percent%")
            }

            0 -> break
            else -> println("Введите 1, 2 или 0")
        }
    }
}

fun saveDictionary(words: List<Word>) {
    val wordsFile = File("words.txt")
    wordsFile.writeText("")
    for (word in words) {
        wordsFile.appendText("${word.questionWord}|${word.translate}|${word.correctAnswersCount}\n")
    }
}

fun loadDictionary(): List<Word> {
    val dictionary = mutableListOf<Word>()
    val wordsFile = File("words.txt")
    wordsFile.readLines().forEach {
        val splitLine = it.split("|")
        dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
    }
    return dictionary
}

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)