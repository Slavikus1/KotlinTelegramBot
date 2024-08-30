import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val learnedNumber: Int = 0,
)

fun main() {
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

    val dictionary = loadDictionary()

    while (true) {
        val learnedCounter = dictionary.filter { it.learnedNumber >= 3 }
        println("Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход")
        val input = readln()
        when (input) {
            "1" -> println("Вы нажали 'учить слова'")
            "2" -> {
                val learnedPercent = (learnedCounter.size * 100) / dictionary.size
                println("Количество выученных слов: ${learnedCounter.size}")
                println("Выучено ${learnedCounter.size} из ${dictionary.size} | $learnedPercent%")
            }

            "0" -> break
            else -> println("Необходимо выбрать один из трех пунктов: 1, 2 или 0!")
        }
    }
}