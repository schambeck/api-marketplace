APP = api-marketplace
VERSION = 1.0.0
JAR = ${APP}-${VERSION}.jar
TARGET_JAR = target/${JAR}

DOCKER_IMAGE = ${APP}:latest
DOCKER_FOLDER = .
DOCKER_CONF = ${DOCKER_FOLDER}/Dockerfile
COMPOSE_CONF = ${DOCKER_FOLDER}/compose.yaml
#REPO = schambeck
REPO = southamerica-east1-docker.pkg.dev/prj-marketplace/repo/schambeck

# maven

clean:
	./mvnw clean

dist-run: dist run

dist:
	./mvnw clean package -DskipTests=true -Dmaven.plugin.validation=BRIEF

run:
	java -jar target/api-*.jar

run-tailscale:
	java -jar target/api-*.jar --spring.profiles.active=tailscale

check:
	./mvnw clean test -Dmaven.plugin.validation=BRIEF

# compose

compose-down-up: compose-down compose-up

compose-up:
	docker compose up -d

compose-up-postgres:
	docker compose up -d postgres

compose-up-postgres-keycloak:
	docker compose up -d postgres keycloak

compose-down:
	docker compose down

# archetype

create-archetype-from-project:
	./mvnw clean archetype:create-from-project \
		-DkeepParent=true \
		-DpropertyFile=archetype.properties \
		-DpackageName=com.schambeck \
		-Darchetype.filteredExtensions=java,xml,md \
		-Dmaven.plugin.validation=BRIEF

install-archetype:
	cd target/generated-sources/archetype && ../../../mvnw clean install

create-project-from-archetype:
	./mvnw archetype:generate \
		-DgroupId=com.schambeck \
		-DartifactId=api-marketplace \
		-Dversion=1.0.0 \
		-DarchetypeGroupId=com.schambeck \
		-DarchetypeArtifactId=api-template-archetype \
		-DinteractiveMode=false \
		-Dpackage=br.com.castgroup.marketplace

# docker

dist-docker-build: dist docker-build

dist-docker-build-push: dist docker-build docker-push

docker-build-push: docker-build docker-push

docker-build:
	docker build -f ${DOCKER_CONF} -t ${DOCKER_IMAGE} .

docker-run:
	docker run -d \
		--name ${APP} \
	  	--env SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/marketplace \
		--publish 8080:8080 \
		${DOCKER_IMAGE}

docker-tag:
	docker tag ${APP} ${REPO}/${DOCKER_IMAGE}

docker-push:
	docker push ${REPO}/${DOCKER_IMAGE}

docker-pull:
	docker pull ${REPO}/${DOCKER_IMAGE}

# keycloak

KC_APP = srv-keycloak
KC_DOCKER_IMAGE = ${KC_APP}:latest
KC_DOCKERFILE = Dockerfile.kc

kc-docker-build-push: kc-docker-build kc-docker-push

kc-docker-build:
	docker build -f ${KC_DOCKERFILE} -t ${KC_DOCKER_IMAGE} .

kc-compose-up:
	docker compose -f compose-keycloak.yaml up -d

kc-compose-down:
	docker compose -f compose-keycloak.yaml down

kc-docker-push:
	docker push ${KC_DOCKER_IMAGE}

kc-docker-run:
	docker run -p 9000:9000 \
		--name ${KC_APP} \
		-e KC_HTTP_PORT=9000 \
		-e KEYCLOAK_ADMIN=admin \
		-e KEYCLOAK_ADMIN_PASSWORD=admin \
		${KC_DOCKER_IMAGE} start-dev --import-realm
