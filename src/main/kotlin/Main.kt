import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val learnedNumber: Int,
)

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val learnedCounter = dictionary.size + 1
        val word: Word = Word(original = line[0], translate = line[1], learnedCounter)
        dictionary.add(word)
    }
    wordsFile.appendText("\n")
    wordsFile.appendText("Кол-во выученных слов: ${dictionary.size}")
    dictionary.forEach { println(it) }
}