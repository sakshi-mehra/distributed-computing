rm -rf Node/target
cp -r target Node
docker kill $(docker ps -a -q --filter="name=Node")
docker kill $(docker ps -a -q --filter="name=db")
docker kill $(docker ps -a -q --filter="name=Controller")
docker rm $(docker ps -a -q --filter="name=db")
docker rm $(docker ps -a -q --filter="name=Node")
docker rm $(docker ps -a -q --filter="name=Controller")
docker rmi -f $(docker images | grep 'distributed-computing' | awk -F' ' '{print $3}' | uniq)
docker volume rm $(docker volume ls -q --filter="name=distributed-computing")
docker-compose up
