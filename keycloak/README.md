# Keycloak Realm Configuration

The `housekeeping` Keycloak realm is now managed declaratively by Terraform.

See `housekeepingTF/modules/keycloak-config/main.tf` for:
- Realm settings
- Roles (ADMIN, RECORDER)
- OIDC clients (housekeeping-ui, jenkins, sonarqube)
- Seed admin user

`realm-export.json` is kept here as a historical reference only and is no
longer imported by Keycloak at startup. The Keycloak container no longer
uses `--import-realm`.
