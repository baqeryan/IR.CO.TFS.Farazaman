# Current Endpoint Inventory

This is a descriptive inventory, not a runtime endpoint registry.

## Host Classes

| Boundary | Resolution | Authentication |
|---|---|---|
| Municipality registry | Central application URL | Registry-specific/unauthenticated flow |
| Municipality business API | Runtime preference `BASE_URL` | Bearer token through shared client |
| OIDC identity | Configured identity issuer | AppAuth authorization/token flow |
| Update metadata/APK | Fixed update metadata and allowed HTTPS download hosts | Dedicated unauthenticated client |
| Map style/tiles/assets | Baran map configuration | Separate map client behavior |
| Legacy custom layouts | User/default repository URL construction | Legacy layout downloader behavior |

## Retrofit-Declared Municipality Endpoints

| Owner | Method/path | Purpose |
|---|---|---|
| `AuthService` | `POST token` | Password login and refresh |
| `UserService` | `GET /api/AccountUser/Info` | Authenticated profile |
| `RolesService` | `GET api/AccountUser/Roles` | Authenticated role set |
| `VehicleZoneWorkService` | `GET /api/ApplicationUser/VehicleZoneWork` | Work areas/zones |
| `FormDataApiService` | `POST api/BillOriginViEvent/FormData` | Violation/mission form data |
| `FormDataApiService` | `POST api/BillOriginCleaningItemDailyItem/Create` | Create mission/daily item |
| `FormDataApiService` | `POST api/BillOriginCleaningItemDailyItem/Index` | Mission/daily-item index |
| `FormDataApiService` | `POST api/BillOriginCleaningItemDailyItem/Details/{encryption}` | Mission details |
| `FormDataApiService` | `POST api/GisLayersDataFenceOrgan/Details` | Employer fence |
| `FormDataApiService` | `POST api/GisGeolocation/upload` | Track upload |
| `UploadService` | `POST api/GisGeolocation/upload` | Alternate/legacy typed upload |
| `ViolationApiService` | `POST api/BillOriginViEvent/Create` | Create violation |
| `ApiService` | `GET api/GisLayers/DataLayerRoadByPoint` | Road lookup |
| `ApiService` | `GET api/GisLayer/Index` | GIS layer index |
| `RoadService` | `GET api/GisLayer/DataLayerRoadByPoint` | Alternate road lookup |

## Non-Registry Endpoint Ownership

- Selected-server upload helpers construct municipality upload URLs for Java/legacy callers.
- Password authentication also attempts the compatibility path `api/token` in repository logic.
- Update and map endpoints are constants/configuration rather than members of a shared registry.
- `LoginApi` contains a commented endpoint and is not an active contract.

Path spelling and leading slash differences are part of current source evidence. This inventory does
not normalize or consolidate them.
