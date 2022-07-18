# 农田守望者-后端

## 路由

`POST /gps` 接收来自小车的gps信息, 格式为`{"latitude": 12.23,"longitude": 126.2213}`

`websocket /ws` 建立与无人机控制app的websocket连接, 向其下发gps位置

## 实现

使用Go原生http库以及mux.

由于只有两个设备, 我们使用无缓存channel在两个路由之间传递消息. 服务器收到小车信息后, 立即通过websocket发送给无人机. 


