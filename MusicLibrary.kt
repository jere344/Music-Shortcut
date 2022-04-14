import mslinks.ShellLink
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.lang.Exception
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag

class MusicLibrary(private var userScript:String, private var libraryPath:String) {
    private var supportedFormat = arrayOf("mp3", "ogg", "flac", "wav")

    init{
        userScriptParser()
    }
    private fun userScriptParser(){
        userScript = File(userScript)
            .readText(Charsets.UTF_8)
            .trim()
            .replace("    ", "\t")
            .replace("/\\*(?:[^*]|\\*+[^*/])*\\*+/|//.*".toRegex(), "") // Remove js style comments
            .replace("(?m)^[ \t]*\r?\n".toRegex(), "") // Remove empty lines
            .replace(" *(?=\n)".toRegex(), "") // Remove trailing whitespaces
            .replace("(?<=\t) *".toRegex(), "") // Remove space before instructions
            .replace("\"\"", "")
        //println(userScript)


    }

    private fun getLayer(line:String):Int{
        var i = 0
        for (c in line){
            if (c == '\t')
                i++
            else return i }
        throw Exception("No instruction")
    }

    fun createShortcut(newPath : String){
        File(libraryPath).walk().forEach {
            if (it.extension.lowercase() !in supportedFormat) {
                if (it.isDirectory) return@forEach
                return@forEach}
            val shortcutFile = File(newPath + shortcutPath(it) + ".lnk")
            shortcutFile.parentFile.mkdirs()

            ShellLink.createLink(it.path, shortcutFile.path)
        }

    }

    private fun shortcutPath(musicFile : File):String{
        val path = mutableListOf<String>()
        var layer = 0
        val fileTag = AudioFileIO.read(musicFile).tag

        /*
        Move up a layer when a folder is created or an IF condition is true
        You can freely go down a layer but not go up
        So if the IF condition is false the layer won't change and what's in the IF won't be executed
        */

        userScript.lines().forEach {
            // check the layer, skip if the current layer is higher than the last one
            val temp = getLayer(it)
            if (layer < temp){
                return@forEach
            }

            layer = temp

            val instruction = it.trimStart()


            if (instruction == "ADD FOLDER") {
                layer++
                path.add("\\") }
            else if (instruction.take(3) == "IF ") {
                if (instruction.take(4) == "IF (") {
                    if (getTag(fileTag, instruction.drop(4).dropLast(1), musicFile.extension.lowercase()).trim() != "") {
                        layer++ }
                } else if (instruction.take(8) == "IF NOT (") {
                    if (getTag(fileTag, instruction.drop(4).dropLast(1), musicFile.extension.lowercase()).trim() == "") {
                        layer++ }
                }
            }
            else if (instruction.first() == '"' && instruction.last() == '"') path.add(instruction.drop(1).dropLast(1))
            else path.add(getTag(fileTag, instruction, musicFile.extension.lowercase()).replace("[\\\\<>*?/\":|\t\n\r]".toRegex(), ""))

        }
        return path.joinToString("")
            .replace("[\\. ]*\\\\".toRegex(), "\\\\") // Remove dot and space at the end of each folder


    }

    private fun getTag(fileTag : Tag, tag : String, extension : String):String{
        return if (tag.take(7) == "CUSTOM:") getCustomTag(fileTag, tag.drop(7), extension)
        else fileTag.getFirst(FieldKey.valueOf(tag))
    }

    private fun getCustomTag(fileTag: Tag, tag: String, extension: String):String{
        if (extension == "mp3" || extension == "wav") {
            fileTag.getFields("TXXX").forEach {
                val field = it.toString().split("\"")
                if (field[1] == tag) return field[3]
            }
        } else if (extension == "ogg" || extension == "flac"){
            return fileTag.getFirst(FieldKey.valueOf(tag))
        }

        return "None"
    }
}
