# EUDI IT Wallet Pid Provider

## Technical requirements

EUDI-it-wallet-pid-provider is a backend service developed in Java with Spring Boot Framework that include the functionality to issue the Italian PID (Person Identification Data) credential according to [Italian EUDI Wallet Technical Specifications](https://italia.github.io/eudi-wallet-it-docs/en/pid-issuance.html).

## Configuration

In this release there is a mock authentication: the credentials are `user`/`password`.

## Creating Docker Image and Launching Container
- Compile and build the project's JAR file.

  `mvn clean package`

- Build an image using the Dockerfile provided in the code (use the `-t` option to specify a name:tag for the image).

  `docker build -t eudi-it-wallet-pid-provider .`

- Launch a container using the newly created image (where -p EXT_PORT:INT_PORT maps the internal port - to be consistent with the one specified in the YAML file with the Docker profile - and exposes it to a port of our choice).

  `docker run -p 8080:80 eudi-it-wallet-pid-provider`
  
- Verify the successful accessibility of the [swagger](http://localhost:8080/swagger-ui/index.html) endpoint


# License: 
Apache License Version 2.0


