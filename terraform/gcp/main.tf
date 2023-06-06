terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 4.67.0"
    }
  }
}

provider "google" {
  alias   = "default"
  project = local.project
  region  = local.region
}

data "google_project" "project" {
  project_id = local.project
}