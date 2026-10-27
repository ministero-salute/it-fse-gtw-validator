# it-fse-gtw-validator

The validation microservice of the Fascicolo Sanitario Elettronico (FSE) 2.0 Gateway.
It checks CDA documents before publication and returns validation outcomes,
diagnostics, and the mapping identifiers needed for subsequent transformation.

## Document validation

The service performs three main checks:

- **Syntactic validation:** checks the CDA against its XML schema (XSD).
- **Semantic validation:** applies the relevant Schematron rules for the document type.
- **Terminology validation:** checks the codes and code systems used in the document.
  This step is skipped for the TS system.

When these checks allow processing to continue, the service can also request
transformation and validation of a temporary FHIR Bundle through
`it-fse-gtw-fhir-mapping-engine`.

### Optional FHIR checks and diagnostics

The validation API accepts two optional query parameters:

| Parameter | Purpose |
| --- | --- |
| `validations` | Requests FHIR checks such as `NORMATIVE_R4` (FHIR R4 conformance) and `TRAVERSABILITY` (reachability of Bundle resources from the first DocumentReference). |
| `includeRemovalReport` | Requests the mapping engine's removal report, including when no FHIR validation is requested. |

If neither option is requested, the validator does not call the mapping engine.
Otherwise, it collects normalization diagnostics and any requested FHIR validation
results. FHIR validation errors block validation; normalization findings are
reported as warnings unless a FHIR validation error takes precedence.

### Alignment with publication

Publication supplies real metadata to enrich the mapped DocumentReference before
FHIR validation. Because the validation API does not receive that publication
metadata, the validator sends a synthetic `documentReferenceDTO` with every mapping
request, including removal-report-only requests. This exercises the same
DocumentReference enrichment path rather than validating an unenriched resource.

The sample values are defined in
[`SampleDocumentReferenceFactory`](src/main/java/it/finanze/sanita/fse2/ms/gtw/validator/utility/SampleDocumentReferenceFactory.java)
and cover:

- Facility type, practice setting, and service period.
- Events, document obscuration/security label, and administrative regime.
- Document category and identifiers.
- Attachment location, hash, and size.

These values are placeholders, not actual publication metadata or the hash and
size of the submitted document. The generated Bundle is discarded: neither it nor
the sample metadata is published or returned to the caller.

This makes validation more representative of publication, but **does not guarantee
the same outcome with real metadata**. Publication still validates the Bundle
enriched with the actual metadata, whose values can affect the final result.

## Related repositories

- [it-fse-support](https://github.com/ministero-salute/it-fse-support) — FSE 2.0 support and documentation.
- [it-fse-landing](https://github.com/ministero-salute/it-fse-landing) — Overview of the FSE 2.0 repositories.

## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
