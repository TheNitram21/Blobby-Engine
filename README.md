[![License](https://img.shields.io/github/license/TheNitram21/Blobby-Engine)](https://www.github.com/TheNitram21/Blobby-Engine/blob/development/LICENSE)
[![Version](https://img.shields.io/github/v/tag/TheNitram21/Blobby-Engine?label=version)](https://jitpack.io/#TheNitram21/Blobby-Engine)
<img align="right" src="https://thenitram21.github.io/blobby-engine/logo.png" height="150" width="150">

# Blobby Engine
The blobby engine is a 2D level-based easily moddable game engine.

## Summary
1. [Features](#features)
2. [Download](#download)
3. [Getting started](#getting-started)
4. [License](#license)

## Features
### NPCs
Blobby Engine's scripted NPCs allow for creating an immersive and interactive world for the player to explore.

### Lighting
Blobby Engine supports lighting using precalculated light maps.<br>
<img src="https://user-images.githubusercontent.com/49786755/186373213-d2fb3c38-a154-4d0b-9827-cd28f7b8be75.png" width="720">

### Physics
Blobby Engine has built-in collision checks.

### Level Editor
Blobby Engine has a level editor called BEE. BEE is also used for calculating light maps.<br>
<img src="https://user-images.githubusercontent.com/49786755/186373193-ed6281ff-d89c-4231-9788-bfd63656785f.png" width="720">

## Download
Blobby Engine is available using Maven.
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.TheNitram21</groupId>
    <artifactId>Blobby-Engine</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
Remember to replace `VERSION` with the version you want to get.

## Getting started
To create a simple window, first download the `BlobbyEngineFiles.zip` from the [latest release](https://github.com/TheNitram21/Blobby-Engine/releases/latest)
and unzip it into a directory for the game. The ZIP file does contain the whole JRE (Java Runtime Environment) on version 11,
so the download could take some time (if you have a bad internet connection) and Blobby Engine will only support Java 11.<br>
Open your IDE. If you don't want to compile you game and create an executable file everytime you want to test your game, create your project inside the game directory.
Then download the engine using Maven (see [Download](#download)). For opening a basic window using Blobby Engine, the following code is needed:
```java
public class Game implements EventListener {
  public static void main(String[] args) {
    ListenerManager.registerEventListener(new Game());
    BlobbyEngine.run(new RunConfigurations("Title", 1280, 720, null, false));
  }
}
```
This will create a window with the title "Title", a resolution of 1280x720 (the window has to have an aspect ratio of 16:9) and the default icon.
Also, the window will not be in fullscreen mode.<br>
But when running the now, it will crash! Why? That's because you haven't loaded any level. Open the **B**lobby **E**ngine **E**ditor (located in `bin/BEE.exe` from the game directory) and save an empty level into the `map` directory inside your game directory (you'll have to set some level parameters, BEE will tell you which).<br>
Now back to the code: Add the following method to your main class.
```java
@Override
public void onStart(StartEvent event) {
  BlobbyEngine.setPlayer(new Player(new Vector2d(0, 0), Map.of("Texture", TEXTURE_PATH)));
  LevelLoader.loadLevel(LEVEL_PATH, BlobbyEngine::setLevel);
}
```
Remember replacing `LEVEL_PATH` with the path to the level you created (not including `maps/` or `.json`)
and `TEXTURE_PATH` with the path to your player texture (again, not including `textures/` or `.png`). The window should not crash anymore!

## License
Blobby Engine is licensed under the MIT license. It can be viewed [here](https://github.com/TheNitram21/Blobby-Engine/blob/development/LICENSE).
