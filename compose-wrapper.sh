#!/bin/sh
export EXTERNAL_IP=$(gcloud compute instances list --format="get(EXTERNAL_IP)[no-heading]")
exec docker-compose $@
