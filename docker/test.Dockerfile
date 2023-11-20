FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /home/spring/app.jar

ENV SERVER_PORT=8080

#base url or the domain that expose this container
ENV BASE_URL=http://localhost:${SERVER_PORT}

#similar to base url, i.e. https://api.eudi-wallet-it-issuer.it
ENV CLIENT_URL=${BASE_URL}

#host of the trust anchor, i.e. demo.federation.eudi.wallet.developers.italia.it
ENV HOST_TRUST_ANCHOR=demo.federation.eudi.wallet.developers.italia.it

#host of the CIE provider
ENV HOST_CIE_PROVIDER=127.0.0.1:8001

#host of the relying party, i.e.api.eudi-wallet-it-issuer.it
ENV HOST_RELYING_PARTY=127.0.0.1:${SERVER_PORT}

#path to the keys
ENV KEY_ROOT_PATH=${HOME}/key

#path to RP jwk
ENV RP_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-qeea-key-jwk.json

#path to trust mark, optional
ENV RP_TRUST_MARK_FILE_PATH=${KEY_ROOT_PATH}/oidc-rp-trust-marks.json

#path to RP encryption jwk

ENV RP_ENCR_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-encr-rp-key-jwk.json

#RP client id
ENV RP_CLIENT_ID=${BASE_URL}/rp

#RP redirect callback endpoint
ENV RP_REDIRECT_CALLBACK_URI=https://${HOST_RELYING_PARTY}/rp/callback

#RP request uri endpoint
ENV RP_REQUEST_URI=https://${HOST_RELYING_PARTY}/request_uri


ENV DEFAULT_TRUST_ANCHOR=https://${HOST_TRUST_ANCHOR}

#URL to call for relying party trust chain retrieval, i.e. https://demo.federation.eudi.wallet.developers.italia.it/fetch?sub=https://api.eudi-wallet-it-issuer.it/rp/
ENV FEDERATION_TC_URL=${DEFAULT_TRUST_ANCHOR}/fetch?sub=${RP_CLIENT_ID}/

#optional
ENV SPID_PROVIDER_SUB=https://$HOST_TRUST_ANCOR/oidc/op/

#optional
ENV CIE_PROVIDER_SUB=https://$HOST_CIE_PROVIDER/oidc/op/

#OpenId Credential Issuer, i.e. i.e. https://api.eudi-wallet-it-issuer.it
ENV OID_CI_CRED_ISS=127.0.0.1:${SERVER_PORT}

#Credential issuer JWK path
ENV OID_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-pp-key-jwk.json

#external yml for this service
ENV CONF_FILE=${CONF_FILE}/application-docker.yml

COPY docker/application-docker.yml ${CONF_FILE}

EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=docker", "-Dspring.config.location=${CONF_FILE}", "-Dlogging.file.name=/home/spring/log/app.log", "-Dspring.pidfile=/home/spring/pid/application.pid","-jar","/home/spring/app.jar"]
