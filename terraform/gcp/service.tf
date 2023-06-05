resource "google_service_account" "service_sa" {
  project    = local.project
  account_id = "cinema-tickets-service-sa"
}