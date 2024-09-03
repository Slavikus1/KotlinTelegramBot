import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var learnedNumber: Int = 0,
)

fun main() {
    while (true) {
        var dictionary = loadDictionary()
        println("Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход")
        when (readln()) {
            "1" -> {
                var unlearnedNumber = dictionary.filter { it.learnedNumber < 3 }
                if (unlearnedNumber.isEmpty()) {
                    println("Вы выучили все слова.")
                }
                while (unlearnedNumber.isNotEmpty()) {
                    val shuffledUnlearned = unlearnedNumber.take(4).shuffled()
                    val shuffledTranslates = shuffledUnlearned.map { it.translate }
                    val hiddenWord = shuffledUnlearned.random()
                    val indexOfHiddenWord = shuffledUnlearned.indexOf(hiddenWord)
                    println("Как переводится слово ${hiddenWord.original}?")
                    println(shuffledTranslates)
                    println("Введите ответ, указав число от 1 до 4. Нажмите 0 для выхода в меню.")
                    var userAnswer = readln().toIntOrNull()
                    if (userAnswer == 0) break
                    while (userAnswer !in 1..4) {
                        println("Пожалуйста, ответьте указав число от 1 до 4")
                        userAnswer = readln().toIntOrNull()
                    }
                    if (userAnswer == (indexOfHiddenWord + 1)) {
                        println("Верно!")
                        hiddenWord.learnedNumber += 1
                        dictionary = saveDictionary(hiddenWord, dictionary)
                        unlearnedNumber = dictionary.filter { it.learnedNumber < 3 }
                    } else println("Неверно!")
                }
                continue

            }

            "2" -> {
                val learnedCounter = dictionary.filter { it.learnedNumber >= 3 }
                val learnedPercent = (learnedCounter.size * 100) / dictionary.size
                println("Количество выученных слов: ${learnedCounter.size}")
                println("Выучено ${learnedCounter.size} из ${dictionary.size} | $learnedPercent%")
            }

            "0" -> break
            else -> println("Необходимо выбрать один из трех пунктов: 1, 2 или 0!")
        }
    }
}

fun loadDictionary(): List<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1])
        dictionary.add(word)
    }
    return dictionary
}

fun saveDictionary(word: Word, dictionary: List<Word>): List<Word> {
    val newDictionary = dictionary.map { it -> if (word.original == it.original) word else it }
    val file = File("words.txt")
    return newDictionary
}