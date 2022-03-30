I am a beginner developper and it was made in a few hours as a learning project. 
I do not guarantee the stability of this script.




# Music-Shortcut

A Kotlin script to create a shortcut for each music files in your library and sort them following a custom script .


---

## Features

- support mp3, wav, ogg and flac.
- Support custom tags
- Custom easy script to sort your library the way you want


## Use

```
val a = MusicLibrary("C:\\path_to_your_script.txt", "D:\\path_to_you_library")
a.createShortcut("C:\\path_to_the_your_shortcut_folder\\")

```


### Custom scripts

Exemple :
```
ADD FOLDER //Create a folder with only one custom tag named "GROUP"
    CUSTOM:GROUP

ADD FOLDER
    IF (YEAR)// If the album have a year name the second folder "[YEAR]. [ALBUM]"
        YEAR
        ". "
    ALBUM // Else just name it "[ALBUM]"



IF (DISC_NO) // Create a folder with only "Disc [DISC_NO]"
     ADD FOLDER
    "Disc "
    DISC_NO

ADD FOLDER
    IF (TRACK)
        TRACK
        ". "
    ARTIST
    " - "
    TITLE
```
[Availible tags](./Supported%20tags)

## Requirements

- [jaudiotagger](https://www.jthink.net/jaudiotagger/)
- [mslink](https://github.com/DmitriiShamrikov/mslinks)

