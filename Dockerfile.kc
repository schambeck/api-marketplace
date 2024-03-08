FROM quay.io/keycloak/keycloak:latest
WORKDIR /opt/keycloak
COPY realm.json data/import/realm.json
RUN bin/kc.sh build
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
