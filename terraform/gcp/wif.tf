resource "google_iam_workload_identity_pool" "github_pool" {
  workload_identity_pool_id = "github-wif-pool"
}

resource "google_iam_workload_identity_pool_provider" "github" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-wif-provider"
  attribute_mapping                  = {
    "google.subject"       = "assertion.sub"
    "attribute.actor"      = "assertion.actor"
    "attribute.aud"        = "assertion.aud"
    "attribute.repository" = "assertion.repository"
  }
  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

resource "google_service_account" "github_sa" {
  account_id = "github-sa"
}

resource "google_service_account_iam_member" "github_sa_wif" {
  service_account_id = google_service_account.github_sa.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${local.github_repo}"
}

resource "google_project_iam_member" "cloud-run-admin-iam" {
  project = local.project
  role    = "roles/run.admin"
  member  = "serviceAccount:${google_service_account.github_sa.email}"
}

resource "google_project_iam_member" "cloud-build-editor-iam" {
  project = local.project
  role    = "roles/cloudbuild.builds.editor"
  member  = "serviceAccount:${google_service_account.github_sa.email}"
}

resource "google_project_iam_member" "storage-admin-iam" {
  project = local.project
  role    = "roles/storage.admin"
  member  = "serviceAccount:${google_service_account.github_sa.email}"
}

resource "google_project_iam_member" "service-account-user-iam" {
  project = local.project
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.github_sa.email}"
}