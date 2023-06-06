resource "google_service_account" "service_sa" {
  account_id = "cinema-tickets-service-sa"
}

resource "google_cloud_run_v2_service" "cinema-tickets-service" {
  name     = "cinema-tickets-service"
  project  = local.project
  location = local.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    scaling {
      min_instance_count = 0
      max_instance_count = 1
    }

    volumes {
      name = "cloudsql"
      cloud_sql_instance {
        instances = [google_sql_database_instance.postgres-db-instance.connection_name]
      }
    }

    containers {
      # We use an empty image here since we refresh that immage using Github action
      image = "us-docker.pkg.dev/cloudrun/container/hello"
      resources {
        startup_cpu_boost = true
        cpu_idle          = true
      }
      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "gcp"
      }
      volume_mounts {
        name       = "cloudsql"
        mount_path = "/cloudsql"
      }
    }
    execution_environment = "EXECUTION_ENVIRONMENT_GEN2"
    service_account       = google_service_account.service_sa.email
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }
}

#resource "google_cloud_run_service_iam_binding" "default" {
#  project  = google_cloud_run_v2_service.cinema-tickets-service.project
#  location = google_cloud_run_v2_service.cinema-tickets-service.location
#  service  = google_cloud_run_v2_service.cinema-tickets-service.name
#  role     = "roles/run.invoker"
#  members  = ["allUsers"]
#}