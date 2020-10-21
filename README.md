## KadDHT

#### 运行方式

   1. 配置 `resources/config.json` 文件
   2. ```java -jar KadDHT.jar -config config.json```
   
    运行成功后，会进入一个交互 `shell` 中。开头显示***ip:port***，***kadid***
    以便其他节点可通过其加入网络。***default directory***指出程序存放数据，文件的目录ｉ
    
    
#### 命令使用
   * ```exit```　退出程序
   * ```showroute```　显示路由表信息
   * ```get <key>```　***key*** 为`put`命令生成的,并将文件保存在 ***defalut directory*** 中
   * ```put <file>```***file*** 为 ***default directory*** 中的文件,并输出 ***key***
