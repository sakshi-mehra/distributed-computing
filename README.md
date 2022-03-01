## Distributed  Computing

### Project Description
Basic functionality added 

### Creating a fat jar
Used maven plugin to create a jar with dependencies

`./mvnw clean package`

### Dockerize the application
Build the dockerfile in the project to containerize the application

#### Steps to dockerize the application 
1. Go inside the application root folder where `Dockerfile` exists
2. Build image for the application `docker build -t ds_project:1 .`
   where -t denotes the name:tag for the image
3. Run the application as a container `docker run -p 8080:8080 ds_project:1`
   where -p exposes the container’s internal port. The format is -p hostPort:containerPort

### Docker basic commands
1. Delete a container - `docker rm <container_id>`
2. Delete an image - `docker rmi <image_id>`
3. Show running containers - `docker container ls`
4. Show all the containers - `docker container ls -all`
5. List images - `docker images`
6. Remove all the containers - `docker rm $(docker ps -a -q)`
    
### Testing 
1. Add jar application run configuration 
2. Add springboot application run configuration

### TODO:
1. Add further APIs
2. Simulate a distributed system with different services as differnet docker containers
