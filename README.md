# Boast CPvP — Fabric 1.21.11

Mod client gửi tên + tọa độ X/Y/Z + dimension của **chính người đang chạy mod** tới Discord mỗi 60 giây.

## 1. Cấu hình
Sau lần chạy đầu tiên, sửa:
`config/boastcpvp.properties`

Đặt:
`webhook=WEBHOOK_CUA_BAN`
`interval_seconds=60`

Chỉ dùng webhook của kênh Discord bạn quản lý.

## 2. Build
Cần Java 21.

Windows:
```bat
gradlew.bat build
```

File JAR nằm tại:
`build/libs/BoastCPvP-1.0.0.jar`

## 3. Cài
Chép JAR vào thư mục:
`.minecraft/mods`

và cài Fabric API 0.141.6+1.21.11.
