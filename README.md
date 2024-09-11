# Fabric Stashwalker Mod

Mod that can be used alongside a hacked client like Rusherhack, Meteor or Future Client
This mod is open source, you can check the code yourself

## Features

- configure keybindings in options menu
- red entity tracers, draws a tracer to valuable items and entities that may contain items:
    - stacked chest minecarts: if multiple chest minecarts are stacked in the same place
    - chest boat
    - llama's and donkeys that have chests
    - elytra item
    - enchanted gapp item
    - diamond/netherite armor item
    - diamond/netherite tool item
    - xp bottle item
    - totem item
    - smithing template item
- blue block entity tracers, draws a blue tracer to any double chest if the chest is not surrounded by mossy cobblestone (so not a dungeon chest)
- new chunks: renders red rectangles around 1.19+ chunks, enabling you to follow chunk trails
- sign reader: posts text of signs you pass by in chat HUD

<br/>
![screenshot1](/screenshots/2024-09-11_10.56.51.png)
<br/>
![screenshot2](/screenshots/2024-09-11_10.57.14.png)
<br/>
![screenshot3](/screenshots/2024-09-11_10.57.20.png)
<br/>
![screenshot4](/screenshots/2024-09-11_10.57.52.png)
<br/>
![screenshot5](/screenshots/2024-09-11_10.58.17.png)
<br/>
![screenshot6](/screenshots/2024-09-11_10.58.34.png)
<br/>
![screenshot7](/screenshots/2024-09-11_10.59.09.png)
<br/>
![screenshot8](/screenshots/2024-09-11_11.17.46.png)
<br/>

## How to build jar from source

- build: ./gradlew build 
- the jar will be in the build/libs/ directory


## How to use

- place the jar inside your Minecraft mods directory
- this mod has the following dependencies:
    - "fabricloader": ">=0.15.11",
	- "minecraft": "1.20.4",
	- "java": "17",
	- "fabric-api": "*"
