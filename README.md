<p align="center">
    <img src="https://raw.githubusercontent.com/MellDa1024/RendogClientAsset/main/assets/RendogClient.png" style="width: 90%">
</p>

[Official RendogServer Discord](https://discord.gg/aKhYsfm)  
RendogClient는 기존에 있던 무정부 서버 전용 클라이언트이면서도 오픈 소스 클라이언트인 "Lambda" 클라이언트의 포크 버전입니다.  
RendogClient의 경우 기존 Lambda 클라이언트에서 흔히 "핵"이라고 여겨지는 모듈들을 전부 제거하였고, 그 대신 렌독서버의 플레이 경험을 더욱 향상시켜줄 모듈들을 개발하여 적용했습니다.

혹여나 다른 서버에서의 악용 방지를 위해 렌독서버가 아닌 서버에 접속 시 클라이언트가 자동으로 서버를 나가게 설계되어있습니다.  
물론 오픈 소스이기에 코드 수정 후 접속하면 그만이지만, 굳이 그렇게까지 할 필요가 있나 싶네요.

## 도움!
무기의 쿨타임 데이터를 수집해야 합니다. 자세한 내용은 [여기](https://github.com/MellDa1024/RendogDataBase) 를 참고해주세요.

## 설치 방법
1. Minecraft 1.12.2 버전을 설치합니다.
2. Forge(버전 : 2860)을 설치합니다.
3. 모드를 [여기서](https://github.com/MellDa1024/RendogClient/releases) 다운받습니다.
4. 모드 파일을 `.minecraft/mods` 으로 옮깁니다.

## FAQ

<details>
  <summary>ClickGUI를 여는 법</summary>

> `Y`를 누르세요.

</details>

<details>
  <summary>명령어를 실행시키는 방법</summary>

> 인게임 쳇에서 `;help commands`를 입력해보세요.

</details>

<details>
  <summary>ClickGui의 크기를 초기화 하는 법</summary>

>  `;set clickgui scale 100`를 입력해보세요.

</details>

## 알려진 버그들
> Creo의 KoreanChat 사용 시 RendogClient의 명령어 UI가 보이지 않고, 실행한 명령어가 과거 채팅 기록에 나오지 않는 현상
