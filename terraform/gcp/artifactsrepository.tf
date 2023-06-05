resource "google_artifact_registry_repository" "cloud-run-source-deploy" {
  project       = local.project
  location      = local.region
  repository_id = "cloud-run-source-deploy"
  format        = "DOCKER"
}

resource "google_artifact_registry_repository_iam_member" "artifactregistry-admin-iam" {
  project    = google_artifact_registry_repository.cloud-run-source-deploy.project
  location   = google_artifact_registry_repository.cloud-run-source-deploy.location
  repository = google_artifact_registry_repository.cloud-run-source-deploy.name
  role       = "roles/artifactregistry.admin"
  member     = "serviceAccount:${google_service_account.github_sa.email}"

  depends_on = [google_iam_workload_identity_pool_provider.github]
}
