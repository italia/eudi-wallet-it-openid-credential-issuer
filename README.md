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

  `docker run -p 8443:8443 eudi-it-wallet-pid-provider`
  
- Verify the successful accessibility of the [swagger](http://localhost:8443/swagger-ui/index.html) endpoint

## Docker Hub Repository

[EUDI IT Wallet Pid Provider - Docker Hub Image Repository](https://hub.docker.com/r/ipzssviluppo/eudi-it-wallet-pid-provider)

# Updates

v.1.2.1 - align PID Data Model and Issuance to spec [v.0.5.1](https://github.com/italia/eudi-wallet-it-docs/tree/0.5.1)

v.1.2.0 - federation and Android SDK example - fix `cnf` param in credential

v.1.1.2 - updated Spring Boot to v.3.1.2 - fixed security issues - fixed bug with kid claim in sd-jwt

# License: 
Apache License Version 2.0


