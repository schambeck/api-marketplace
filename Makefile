BASE_URL = http://localhost:8080
ENTITY_RESOURCE = ${BASE_URL}/entities

# maven

clean:
	./mvnw clean

dist-run: dist run

dist:
	./mvnw clean package -DskipTests=true -Dmaven.plugin.validation=BRIEF

run:
	java -jar target/api-*.jar

check: clean
	./mvnw test

# compose

compose-down-up: compose-down compose-up

compose-up:
	docker-compose up -d

compose-down:
	docker-compose down

# http

http-list-entity:
	http GET ${ENTITY_RESOURCE}?page=0

# archetype

create-archetype-from-project:
	./mvnw clean archetype:create-from-project \
		-DkeepParent=true \
		-DpropertyFile=archetype.properties \
		-DpackageName=com.schambeck \
		-Darchetype.filteredExtensions=java,xml,md

install-archetype:
	cd target/generated-sources/archetype && mvn clean install

create-project-from-archetype:
	mvn archetype:generate \
		-DgroupId=com.schambeck \
		-DartifactId=api-marketplace \
		-Dversion=1.0.0 \
		-DarchetypeGroupId=com.schambeck \
		-DarchetypeArtifactId=api-template-archetype \
		-DinteractiveMode=false \
		-Dpackage=com.schambeck.marketplace
