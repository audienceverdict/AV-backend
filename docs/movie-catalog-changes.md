# Movie catalog implementation report

Implemented the agreed core catalog scope: enhanced movie profiles, multiple labeled video/image URLs, reusable actors and technicians with skills/social links, and movie-specific character/crew credits.

## Verified results

- `mvn test`: passed during implementation.
- Final `mvn package`: passed, including 19 passing tests (zero skipped tests, failures, or errors).
- Actual MySQL deployment verification remains required; automated database tests use isolated H2 in MySQL mode.
- H2 MySQL-mode migration tests: new/legacy schemas, baseline adoption, repeat startup, retained legacy values, shared-person FKs, and unique primary-poster constraint passed.
- Integration tests: legacy APIs/aliases and response round-trip, optional-field clearing, URL/enum/pagination validation, administrator permissions, media ordering, concurrent primary selection, reusable actor/crew profiles, filmography, and shared-person deletion safety passed.
- Postman generator produced 99 requests across 12 folders; examples were generated locally, not executed against production.
- No live database was changed, and no deployment was performed.

## Migration and API details

See [movie-catalog.md](movie-catalog.md) for V1/V2/V3 migration behavior, legacy data remediation, all new/updated routes, and frontend URL-only request examples.

Optional soundtrack, distribution, awards, box-office, and related-movie modules remain deferred as agreed in the implementation plan. Slugs were not added because this project has no existing slug contract.

## Created files

- `docs/movie-catalog-changes.md`
- `docs/movie-catalog.md`
- `src/main/java/com/slokam/av/controller/MovieCreditController.java`
- `src/main/java/com/slokam/av/controller/MovieMediaController.java`
- `src/main/java/com/slokam/av/controller/PeopleController.java`
- `src/main/java/com/slokam/av/dto/MediaOrderRequest.java`
- `src/main/java/com/slokam/av/dto/MovieCreditRequest.java`
- `src/main/java/com/slokam/av/dto/MovieCreditResponse.java`
- `src/main/java/com/slokam/av/dto/MovieMediaRequest.java`
- `src/main/java/com/slokam/av/dto/MovieMediaResponse.java`
- `src/main/java/com/slokam/av/dto/MovieRequest.java`
- `src/main/java/com/slokam/av/dto/MovieResponse.java`
- `src/main/java/com/slokam/av/dto/PersonRequest.java`
- `src/main/java/com/slokam/av/dto/PersonResponse.java`
- `src/main/java/com/slokam/av/dto/PersonSocialLinkRequest.java`
- `src/main/java/com/slokam/av/dto/PersonSocialLinkResponse.java`
- `src/main/java/com/slokam/av/entity/CharacterCategory.java`
- `src/main/java/com/slokam/av/entity/CreditType.java`
- `src/main/java/com/slokam/av/entity/MovieCredit.java`
- `src/main/java/com/slokam/av/entity/MovieMedia.java`
- `src/main/java/com/slokam/av/entity/MovieMediaType.java`
- `src/main/java/com/slokam/av/entity/Person.java`
- `src/main/java/com/slokam/av/entity/PersonSocialLink.java`
- `src/main/java/com/slokam/av/entity/ProductionStatus.java`
- `src/main/java/com/slokam/av/entity/ReleaseStatus.java`
- `src/main/java/com/slokam/av/entity/SocialPlatform.java`
- `src/main/java/com/slokam/av/mapper/CatalogMapper.java`
- `src/main/java/com/slokam/av/repository/MovieCreditRepository.java`
- `src/main/java/com/slokam/av/repository/MovieMediaRepository.java`
- `src/main/java/com/slokam/av/repository/PersonRepository.java`
- `src/main/java/com/slokam/av/repository/PersonSocialLinkRepository.java`
- `src/main/java/com/slokam/av/service/CatalogValues.java`
- `src/main/java/com/slokam/av/service/MovieCreditService.java`
- `src/main/java/com/slokam/av/service/MovieMediaService.java`
- `src/main/java/com/slokam/av/service/PeopleService.java`
- `src/main/java/com/slokam/av/service/VideoUrls.java`
- `src/main/java/com/slokam/av/validation/HttpUrl.java`
- `src/main/java/com/slokam/av/validation/HttpUrlValidator.java`
- `src/main/java/db/migration/V3__Backfill_movie_profiles.java`
- `src/main/resources/db/migration/V1__legacy_movie_catalog.sql`
- `src/main/resources/db/migration/V2__enhanced_movie_catalog.sql`
- `src/test/java/com/slokam/av/catalog/CatalogIntegrationTest.java`
- `src/test/java/com/slokam/av/catalog/CatalogMigrationTest.java`

## Modified files

- `pom.xml`
- `README.md`
- `postman/AV-backend.postman_collection.json`
- `postman/AV-local.postman_environment.json`
- `postman/ENDPOINTS.md`
- `postman/README.md`
- `scripts/generate_postman.py`
- `src/main/java/com/slokam/av/config/SecurityConfig.java`
- `src/main/java/com/slokam/av/controller/MovieController.java`
- `src/main/java/com/slokam/av/entity/Movie.java`
- `src/main/java/com/slokam/av/exception/handler/GlobalExceptionHandler.java`
- `src/main/java/com/slokam/av/repository/MovieRepository.java`
- `src/main/java/com/slokam/av/service/MovieService.java`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/application-upen.yml`
