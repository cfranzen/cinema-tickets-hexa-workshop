terraform {
  backend "gcs" {
    bucket = "cinema-tickets-hexa-workshop-tf-state"
  }
}
