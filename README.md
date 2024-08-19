# 测试环境部署
修改redis连接地址
1. 使用 Maven 打包生成 jar 文件
2. 删除旧的镜像
2. 构建 Docker 镜像 `docker build -t peidi-user .`
3. 导出镜像到本地 `docker save -o peidi-user.tar peidi-user`
4. 上传镜像到远程服务器
5. 远程服务器上载入镜像 `docker load -i peidi-user.tar`
6. 停止并移除旧的容器 `docker stop peidi-user` `docker rm peidi-user`
7. 运行新的容器 `docker run --name peidi-user -p 8080:8080 -d peidi-user`
