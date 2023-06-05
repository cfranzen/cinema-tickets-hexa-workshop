resource "google_storage_bucket" "cloudbuild-bucket" {
  name          = "${local.project}_cloudbuild"
  project       = local.project
  location      = local.region
  force_destroy = true

  uniform_bucket_level_access = true
}