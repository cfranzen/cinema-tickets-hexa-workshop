locals {
  project      = "cinema-tickets-hexa-workshop"
  region       = "europe-west1"
  docker_image = "${local.region}-docker.pkg.dev/${local.project}/docker-images/service:latest"
  service_name = "cinema-tickets-service"
  github_repo  = "cfranzen/cinema-tickets-hexa-workshop"

  db_instance_name            = "master-postgres-instance"
  db_name                     = "cinema-tickets"
  db_instance_connection_name = "${local.project}:${local.region}:${google_sql_database_instance.postgres-db-instance.name}"
}
