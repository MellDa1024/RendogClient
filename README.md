<p align="center">
    <img src="https://raw.githubusercontent.com/MellDa1024/RendogClientAsset/main/assets/RendogClient.png" style="width: 90%">
</p>

[Official RendogServer Discord](https://discord.gg/aKhYsfm)  
RendogClient는 기존에 있던 무정부 서버 전용 클라이언트이면서도 오픈 소스 클라이언트인 "Lambda" 클라이언트의 포크 버전입니다.  
RendogClient의 경우 기존 Lambda 클라이언트에서 흔히 "핵"이라고 여겨지는 모듈들을 전부 제거하였고, 그 대신 렌독서버의 플레이 경험을 더욱 향상시켜줄 모듈들을 개발하여 적용했습니다.

혹여나 다른 서버에서의 악용 방지를 위해 렌독서버가 아닌 서버에 접속 시 클라이언트가 자동으로 서버를 나가게 설계되어있습니다.  
물론 오픈 소스이기에 코드 수정 후 접속하면 그만이지만, 굳이 그렇게까지 할 필요가 있나 싶네요.

<p align="center">
    <a href="https://github.com/lambda-client/lambda/releases/download/3.1/lambda-3.1.jar"><img alt="lambda-3.1.jar" src="https://raw.githubusercontent.com/lambda-client/assets/main/download_button_3.1.png" width="70%" height="70%"></a>
</p>

## Installation
1. Install Minecraft 1.12.2
2. Install Forge
3. Download the mod file [here](https://github.com/lambda-client/lambda/releases/download/3.1/lambda-3.1.jar)
4. Put the file in your `.minecraft/mods` folder

## FAQ

How do I...

<details>
  <summary>... open the ClickGUI?</summary>

> Press `Y`

</details>

<details>
  <summary>... execute a command?</summary>

> Use the ingame chat with the prefix `;`

</details>

<details>
  <summary>... fix most crashes on startup?</summary>

> Possibly you have multiple mods loaded. Forge loads mods in alphabetical order, so you can change the name of the Mod jar to make it load earlier or later. Add for example an exclamation mark to lambda jar to make it load first.
> If you got `Error: java.lang.IllegalAccessError: tried to access field net.minecraft.util.math.Vec3i.field_177962_a from class baritone.k` remove the noverify tag from your arguments.

</details>

<details>
  <summary>... fix problems with Gradle?</summary>

> Make sure you have a Java 8 JDK installed and in your PATH.
We recommend using the [Temurin](https://adoptium.net/?variant=openjdk8&jvmVariant=hotspot/) variant of OpenJDK 

</details>

<details>
  <summary>... reset the ClickGUI scale?</summary>

> Run the command `;set clickgui scale 100`

</details>