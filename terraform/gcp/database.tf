resource "google_sql_database_instance" "postgres-db-instance" {
  name                = local.db_instance_name
  database_version    = "POSTGRES_15"
  deletion_protection = true

  settings {
    tier                  = "db-f1-micro"
    availability_type     = "ZONAL"
    disk_autoresize       = true
    disk_autoresize_limit = 0
  }
}

resource "google_sql_database" "postgres-database" {
  name       = local.db_name
  instance   = google_sql_database_instance.postgres-db-instance.name
  charset    = "UTF8"
  collation  = "en_US.UTF8"
  depends_on = [google_sql_database_instance.postgres-db-instance]
}

resource "google_project_iam_member" "cloud-sql-client-iam" {
  project = local.project
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.service_sa.email}"
}

resource "google_sql_user" "postgres-user" {
  name     = "postgres"
  password = "postgres"
  instance = google_sql_database_instance.postgres-db-instance.name
}