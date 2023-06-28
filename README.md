# IT Wallet Pid Provider

## Creating Docker Image and Launching Container
- Compile and build the project's JAR file.

  `mvn clean package`

- Build an image using the Dockerfile provided in the code (use the `-t` option to specify a name:tag for the image).

  `docker build -t it-wallet-pid-provider .`

- Launch a container using the newly created image (where -p EXT_PORT:INT_PORT maps the internal port - to be consistent with the one specified in the YAML file with the Docker profile - and exposes it to a port of our choice).

  `docker run -p 8080:80 it-wallet-pid-provider`
  
- Verify the successful accessibility of the [swagger](http://localhost:8080/swagger-ui/index.html) endpoint


# License: 
Apache License Version 2.0

