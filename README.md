# EEW-Plugin for Bukkit
Bukkit/Spigot用の緊急地震速報受信プラグイン
- PocketMine-MP用は[こちら](https://github.com/HADAlove0202/EEW-Plugin-for-PocketMineMP)
## 仕様
- 強震モニタの緊急地震速報を使って緊急地震速報を受信します。
## config.yml
設定方法: [config.yml](src/main/resources/config.yml)
## lang.yml
設定方法: [lang.yml](src/main/resources/lang.yml)
## バグ
- 1.8で使用するとconfig.ymlとlang.ymlでは読めない文字に変換されてしまいます。変更する時はUTF-8で保存すれば問題ありませんがプラグインが読み込まれると読めなくなってしまいます。
