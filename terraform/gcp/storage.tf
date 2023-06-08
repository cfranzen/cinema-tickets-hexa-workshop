resource "google_storage_bucket" "cloudbuild-bucket" {
  name          = "${local.project}_cloudbuild"
  project       = local.project
  location      = local.region
  force_destroy = true

  uniform_bucket_level_access = true
}

resource "google_storage_bucket" "posters-bucket" {
  name          = "${local.project}-posters"
  project       = local.project
  location      = local.region
  force_destroy = true

  uniform_bucket_level_access = true
}

resource "google_storage_bucket_object" "poster1-bucket-object" {
  name   = "poster1.jpg"
  source = "../../posters/poster1.jpg"
  bucket = google_storage_bucket.posters-bucket.id
}
resource "google_storage_bucket_object" "poster2-bucket-object" {
  name   = "poster2.jpg"
  source = "../../posters/poster2.jpg"
  bucket = google_storage_bucket.posters-bucket.id
}
resource "google_storage_bucket_object" "poster3-bucket-object" {
  name   = "poster3.jpg"
  source = "../../posters/poster3.jpg"
  bucket = google_storage_bucket.posters-bucket.id
}

resource "google_storage_bucket_iam_member" "posters-bucket-iam" {
  bucket = google_storage_bucket.posters-bucket.name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.service_sa.email}"
}