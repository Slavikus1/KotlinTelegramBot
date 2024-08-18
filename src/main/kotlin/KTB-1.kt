import java.io.File

fun main() {
    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()
    val stringFile = wordsFile.readLines()
    stringFile.forEach { println(it) }
}