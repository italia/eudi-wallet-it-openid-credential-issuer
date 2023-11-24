# (Q)EEA Issuer

Qualified Electronic Attestation of Attributes (QEAA) is a secure method 
for issuing and verifying digital identities, ensuring that individuals 
can maintain their privacy and identity over the internet.

## Release versions

- 1.4.0 - mDL credentials, in MDOC-CBOR and SD-JWT formats.
- 1.3.0 - fix naming in project
- 1.2.0 - European Disability Card credentials
- 1.1.0 - RP entity configuration
- 1.0.0 - first release

## Install with local Docker image

1. build project with Maven mvn clean package

   `mvn clean package`
  
2. create Docker image with test.Dockerfile, or with docker/Dockerfile but with required environment variables resolved in application.yml

   `docker build -t qeaa-issuer:1.0 -f docker/test.Dockerfile .`
   
3. otherwise, configure the following environment variables for the docker image

```
	ENV SB_SERVER_PORT=8080
	
	#base url or the domain that expose this container
	ENV BASE_URL=http://localhost:${SB_SERVER_PORT}
	
	#similar to base url
	ENV CLIENT_URL=${BASE_URL}
	
	#host of the trust anchor, i.e. demo.federation.eudi.wallet.developers.italia.it
	ENV HOST_TRUST_ANCHOR=demo.federation.eudi.wallet.developers.italia.it
	
	#host of the CIE provider
	ENV HOST_CIE_PROVIDER=127.0.0.1:8001
	
	#host of the relying party
	ENV HOST_RELYING_PARTY=${BASE_URL}
	
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
	
	#URL to call for relying party trust chain retrieval
	ENV FEDERATION_TC_URL=${DEFAULT_TRUST_ANCHOR}/fetch?sub=${RP_CLIENT_ID}/
	
	#optional
	ENV SPID_PROVIDER_SUB=https://$HOST_TRUST_ANCOR/oidc/op/
	
	#optional
	ENV CIE_PROVIDER_SUB=https://$HOST_CIE_PROVIDER/oidc/op/
	
	#OpenId Credential Issuer
	ENV OID_CI_CRED_ISS=${BASE_URL}
	
	#Credential issuer JWK path
	ENV OID_JWK_FILE_PATH=${KEY_ROOT_PATH}/eudi-pp-key-jwk.json
	
	#external yml for this service
	ENV CONF_FILE=${CONF_FILE}/application-docker.yml
```

4. adjust start.sh to load external data i.e. logback

5. run the image created, i.e. 

	`docker run -p 8080:8080 qeaa-issuer:1.0`

6. if the first run, call the jwk creation endpoint:

	`http://localhost:8080/admin/federation/home`
 
7. verify that the configuration is completed:

	`http://localhost:8080/rp/.well-known/openid-federation?format=json `

