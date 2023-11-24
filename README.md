# EUDI IT Wallet Pid Provider

## Technical requirements

EUDI-it-wallet-pid-provider is a backend service developed in Java with Spring Boot Framework that include the functionality to issue the Italian PID (Person Identification Data) credential according to [Italian EUDI Wallet Technical Specifications](https://italia.github.io/eudi-wallet-it-docs/en/pid-issuance.html).

## Configuration

In this release there is a mock authentication: the credentials are `user`/`password`.

## Creating Docker Image and Launching Container
1. Compile and build the project's JAR file.

  `mvn clean package`

2. Build an image using the Dockerfile provided in the code (use the `-t` option to specify a name:tag for the image).

  `docker build -t it-wallet-pid-provider .`

3. otherwise, configure the following environment variables for the docker image

```
    ENV SERVER_PORT=8080

    #base url or the domain that expose this container
    ENV BASE_URL=http://localhost:${SERVER_PORT}

    #similar to base url
    ENV CLIENT_URL=${BASE_URL}

    #host of the trust anchor
    ENV HOST_TRUST_ANCHOR=127.0.0.1:8002

    #host of the CIE provider
    ENV HOST_CIE_PROVIDER=127.0.0.1:8001

    #host of the relying party
    ENV HOST_RELYING_PARTY=127.0.0.1:${SERVER_PORT}

    #path to the keys
    ENV KEY_ROOT_PATH=${HOME}/key

    #path to RP jwk
    ENV RP_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-rp-key-jwk.json

    #path to trust mark, optional
    ENV RP_TRUST_MARK_FILE_PATH=${KEY_ROOT_PATH}/oidc-rp-trust-marks.json

    #path to RP encryption jwk

    ENV RP_ENCR_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-encr-rp-key-jwk.json

    #RP client id
    ENV RP_CLIENT_ID=${BASE_URL}/ci

    #RP redirect callback endpoint
    ENV RP_REDIRECT_CALLBACK_URI=https://${HOST_RELYING_PARTY}/rp/callback

    #RP request uri endpoint
    ENV RP_REQUEST_URI=https://${HOST_RELYING_PARTY}/request_uri


    ENV DEFAULT_TRUST_ANCHOR=https://${HOST_TRUST_ANCHOR}

    #URL to call for relying party trust chain retrieval
    ENV FEDERATION_TC_URL=${DEFAULT_TRUST_ANCHOR}/fetch?sub=${RP_CLIENT_ID}/

    #optional
    ENV SPID_PROVIDER_SUB=https://$HOST_TRUST_ANCOR/oidc/op/

    #optional
    ENV CIE_PROVIDER_SUB=https://$HOST_CIE_PROVIDER/oidc/op/

    #OpenId Credential Issuer
    ENV OID_CI_CRED_ISS=127.0.0.1:${SERVER_PORT}

    #Credential issuer JWK path
    ENV OID_CI_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-pp-key-jwk.json

    ENV OID_CI_ENCR_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-encr-pp-key-jwk.json

    #external yml for this service
    ENV CONF_FILE=${CONF_FILE}/application-docker.yml

```

4. adjust ENTRYPOINT to load external data i.e. logback

	`ENTRYPOINT ["java", "-Dlogback.configurationFile=/path/to/config.xml" ,"-Dspring.profiles.active=docker", "-Dspring.config.location=${CONF_FILE}", "-Dlogging.file.name=/home/spring/log/app.log", "-Dspring.pidfile=/home/spring/pid/application.pid","-jar","/home/spring/app.jar"]`


5. run the image created, i.e. 

	`docker run -p 8080:8080 pid-provider:1.0`

6. if the first run, call the jwk creation endpoint:

	`http://localhost:8080/admin/federation/home`
 

7. verify that the configuration is completed:

	`http://localhost:8080/ci/.well-known/openid-federation?format=json `

## Docker Hub Repository

[EUDI IT Wallet Pid Provider - Docker Hub Image Repository](https://hub.docker.com/r/ipzssviluppo/eudi-it-wallet-pid-provider)

# Updates

v.1.3.3. - fix Docker config

v.1.3.2 - fix CVE, actuator config

v.1.3.1 - fix Docker config

v.1.3.0 - authorize returns code and state in jwt

v.1.2.1 - align PID Data Model and Issuance to spec [v.0.5.1](https://github.com/italia/eudi-wallet-it-docs/tree/0.5.1)

v.1.2.0 - federation and Android SDK example - fix `cnf` param in credential

v.1.1.2 - updated Spring Boot to v.3.1.2 - fixed security issues - fixed bug with kid claim in sd-jwt

# License: 
Apache License Version 2.0


