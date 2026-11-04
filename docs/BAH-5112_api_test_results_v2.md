# BAH-5110 / BAH-5112 — FHIR R4 API Test Results (v2, after B1/B3/B4/B5 fixes)

**Server:** `https://bahmni.standard`
**Auth:** `superman:Admin123` (HTTP Basic; `curl -k`)
**Run date:** 2026-11-04 (IST)
**Deployed modules (rebuilt and redeployed for this run):**
- FHIR2 4.3.0
- FHIR2 module additional extensions 2.0.0-SNAPSHOT (`fhir2addlextension`)
- Medication Administration 3.0.0-SNAPSHOT — rebuilt with JDK 17, deployed 2026-11-04
- Bahmni IPD 2.0.0-SNAPSHOT — rebuilt with JDK 17, deployed 2026-11-04

**Config:** `standard-config/openmrs/apps/ipdDashboard/app.json` → `enableIpdFhirMigration: false`, `amendmentReasonConceptSetUuid: 5c3d2d00-0ad6-434a-b27b-d820796781a4`

**CapabilityStatement (`GET /openmrs/ws/fhir2/R4/metadata`) for `MedicationAdministration`:**

```json
{
  "interactions": ["create", "read", "patch", "search-type", "update"],
  "searchParams": ["category", "context", "patient", "status", "visit"],
  "operations": ["acknowledge"]
}
```

> This confirms the new `visit` search parameter (B5) and the `$acknowledge` operation (B4) are live, and that `update` (B3) is advertised.

---

## Fixes verified in this run

| Blocker | Fix | Verified by |
|---|---|---|
| **B1** — `medication[x]` missing on scheduled administrations | Translator derives `medicationReference` from the order's drug (fallback to order concept); the derived drug is **not** persisted on the administration | §5 create + `GET`; DB shows `drug_id = NULL` |
| **B3** — FHIR `PUT` appended performers instead of replacing | FHIR-only replace path: scalar fields + performers replaced, notes appended (audit trail); legacy REST keeps append-only behaviour | §7 (performers 2 → 1, notes 1 → 2); DB shows the orphan performer row physically deleted |
| **B4** — acknowledgement overloaded `POST /Task` | Proper `POST /MedicationAdministration/{uuid}/$acknowledge` operation returning the stored acknowledgement as a valid FHIR `Task` | §9 (HTTP 200, `focus = MedicationAdministration/<uuid>`); repeat call → 400 |
| **B5** — `context` never populated; no visit search | Scheduled administrations derive `context` from the order's encounter; custom `visit` search parameter added; emergency search uses `?visit=` | §5 (`context = Encounter/…`), §5c/§5d (`?visit=` and `?context=` both return it), §2 (`?visit=&category=emergency`) |

**B2 is intentionally out of scope** (the non-standard `category=emergency` requirement is unchanged).

---

## Real IDs used

| Value | UUID |
|---|---|
| Patient (Dev D D) | `c35ae72e-da69-4aae-bbb2-8003e7d94c74` |
| Patient active visit (IPD) | `9c5e2b8c-d193-42e4-b7b1-7698888e629f` |
| Patient (Test Care View) | `6e570de6-61e2-4943-b5cf-730eb65cd273` |
| Visit (Test Care View) | `f51e03ca-860b-43c7-92c2-e50fcd4ad34f` |
| Patient (Astra Jollaadhfah) | `2aaea835-8154-421b-974a-fd50bfbbc3cf` |
| Visit (Astra Jollaadhfah) | `7bc6633c-d703-41da-ba6e-61113a8ce4a4` |
| Patient (Paul P) — used for §5/§10 | `fc4fe066-47e8-41be-8805-e2b825502cda` |
| Visit (Paul P) — used for §5c | `e75650b8-096b-4e27-bd8a-dc693ed1d3a1` |
| Encounter (Paul P, order encounter) | `9641d536-08fc-40a8-886d-5481d23806b1` |
| MedicationRequest (Paracetamol 80 mg) | `04a59c60-1950-498b-b959-64c037bd842e` |
| MedicationRequest (Omeprazole 10 mg) — used for §5/§10 | `74eab561-d658-47c5-b6e5-7a5b8284704f` |
| Medication / Drug (Paracetamol 650 mg) | `012be465-f26a-4b4c-8c8b-356e7b81154c` |
| Medication / Drug (Omeprazole 10 mg) | `258cd168-577f-4e56-9c05-2b939e22d6c3` |
| Practitioner (superman) | `d7a67c17-5e07-11ef-8f7c-0242ac120002` |
| Practitioner (Neha Anand, Witness) | `05d6752c-5e08-11ef-8f7c-0242ac120002` |
| Slot (scheduled, order 04a59c60) | `3fa6c590-a1a4-4a0d-8056-fdf9a5e03af5` |
| Slot (scheduled, order 04a59c60) | `52b5b1bf-8d25-40cd-8e85-80e2aa0cb39b` |
| Slot (scheduled, order 74eab561) — §5 | `cccfad35-c185-49f0-a158-265fbafc6731` |
| Slot (scheduled, order 74eab561) — §10 | `ea95e937-4466-46b1-a4b1-21b49bd83d86` |
| Amendment reason concept (Incorrect Time) | `459fd8cd-568e-4d36-b5f5-88e861db62b9` |

> The original order `04a59c60…` had no remaining `SCHEDULED` slots (all five were `COMPLETED` by earlier runs), so §5 and §10 use a different real order (`74eab561…`, patient Paul P) with fresh scheduled slots. Everything else uses the same IDs as the original run.

---

### 1. Drug chart read — MedicationRequest search by visit (BAH-5110)

- **Method / URL:** `GET /openmrs/ws/fhir2/R4/MedicationRequest?visit=f51e03ca-860b-43c7-92c2-e50fcd4ad34f`
- **Old REST equivalent:** `GET /openmrs/ws/rest/v1/ipdVisit/{visitUuid}/medication?includes=emergencyMedications` (the `ipdDrugOrders` array; the FHIR `MedicationRequest?visit=` search replaces the drug-order read)
- **Request payload:** none
- **Response status:** `200 OK`
- **Response summary:**

```json
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 3,
  "entry": [
    { "resource": { "id": "5653d31c-6cca-4692-8fc2-f1f5decd79a6", "status": "completed", "medicationReference": { "display": "Paracetamol 150 mg/ml" }, "subject": { "reference": "Patient/6e570de6-61e2-4943-b5cf-730eb65cd273" } } },
    { "resource": { "id": "f22b3f12-dc1a-445c-ae52-300cee0abeef", "status": "completed", "medicationReference": { "display": "Paracetamol 650 mg" }, "subject": { "reference": "Patient/6e570de6-61e2-4943-b5cf-730eb65cd273" } } },
    { "resource": { "id": "17dc798a-2e64-48a6-a7f9-1b15eccc0d63", "status": "completed", "medicationReference": { "display": "Tenofovir 300 mg" }, "subject": { "reference": "Patient/6e570de6-61e2-4943-b5cf-730eb65cd273" } } }
  ]
}
```

- **Result:** Pass
- **Notes:** unchanged from the original run. `medicationReference` is used (no `medicationCodeableConcept`); the frontend mapper falls back to `medicationReference.display` and derives `drug.uuid` from the reference.

---

### 2. Emergency medication read — MedicationAdministration search by visit + category (BAH-5110 / B5)

- **Method / URL:** `GET /openmrs/ws/fhir2/R4/MedicationAdministration?visit=9c5e2b8c-d193-42e4-b7b1-7698888e629f&category=emergency`
- **Old REST equivalent:** `GET /openmrs/ws/rest/v1/ipdVisit/{visitUuid}/medication?includes=emergencyMedications` → `emergencyMedications` array. v2 uses `?visit=&category=emergency` instead of the original run's `?context=&category=emergency` (B5).
- **Request payload:** none
- **Response status:** `200 OK`
- **Response summary (6 accumulated test administrations for this visit):**

```json
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 6,
  "entry": [
    { "resource": { "id": "00b7dbe3-f669-48af-9c3b-17974972126c", "status": "completed", "category": { "coding": [{ "code": "emergency" }] }, "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" } } },
    { "resource": { "id": "1f819f3d-11e5-432b-8c69-40f90a66b32e", "status": "completed", "category": { "coding": [{ "code": "emergency" }] }, "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" } } },
    { "resource": { "id": "254dfbd9-2781-4d56-a964-5d5b6a3c6874", "status": "completed", "category": { "coding": [{ "code": "emergency" }] }, "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" } } },
    { "resource": { "id": "aa2c4c11-cfa1-4198-9d72-003607bdb088", "status": "completed", "category": { "coding": [{ "code": "emergency" }] }, "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" } } },
    { "resource": { "id": "de6c6ed1-ece7-446e-ab5d-4b6e4504f5a0", "status": "completed", "category": { "coding": [{ "code": "emergency" }] }, "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" } } },
    { "resource": { "id": "dfa9fd46-f575-4aa2-84d5-a37479bab963", "status": "completed", "category": { "coding": [{ "code": "emergency" }] }, "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" } } }
  ]
}
```

- **Result:** Pass
- **Deviation (intentional — B5):** the emergency search now uses the Bahmni `visit` parameter instead of `context`. `context` remains the standard Encounter reference and is applied as an additional filter.
- **Negative check:** `GET /MedicationAdministration?category=emergency` (no `visit`) → `400 Bad Request`:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [{ "severity": "error", "code": "processing", "diagnostics": "The 'visit' parameter with a visit uuid is required when category=emergency" }]
}
```

- **Note:** adhoc administrations have no order, so they still have no `context`; they are found through their emergency slot by `visit`. Scheduled administrations now do have `context` (see §5).

---

### 3. MedicationAdministration search by patient / status / context / visit (BAH-5110 / B5)

- **Method / URL:** several `GET /openmrs/ws/fhir2/R4/MedicationAdministration?...` requests
- **Old REST equivalent:** `GET /openmrs/ws/rest/v1/ipd/schedule/type/medication?patientUuid={patientUuid}` (the FHIR search adds `status`, `context` and `visit` filters on top of the patient read)
- **Response status:** `200 OK` for all
- **Results:**

| Query | Result |
|---|---|
| `?patient=2aaea835-8154-421b-974a-fd50bfbbc3cf` | `total: 4`, all `subject.reference = Patient/2aaea835…` |
| `?patient=c35ae72e-da69-4aae-bbb2-8003e7d94c74` | `total: 12` (accumulated test data) |
| `?patient=00000000-0000-0000-0000-000000000000` | `total: 0` |
| `?status=completed` | `total: 17` (accumulated test data) |
| `?visit=7bc6633c-d703-41da-ba6e-61113a8ce4a4` (non-category) | `total: 0` — those administrations are adhoc and have no encounter |
| `?context=7bc6633c-d703-41da-ba6e-61113a8ce4a4` | `total: 0` — the standard Encounter filter is applied |

- **Result:** Pass
- **B5 verification:** after §5 creates a scheduled administration, `?visit=e75650b8-096b-4e27-bd8a-dc693ed1d3a1` returns exactly that administration (`total: 1`), and `?context=9641d536-08fc-40a8-886d-5481d23806b1` returns the same one. This proves the new `visit` handler joins through `encounter.visit` and that `context` is populated.
- **Note:** `performer` and `medication` search parameters are handled by the DAO but are not declared on the IPD provider's `@Search` method, so the server rejects them with `not-supported`. Not part of this test case.

---

### 4. MedicationAdministration read by id (BAH-5110)

- **Method / URL:** `GET /openmrs/ws/fhir2/R4/MedicationAdministration/55d5ebc4-f321-4c31-9406-50c6edb8b039`
- **Old REST equivalent:** `GET /openmrs/ws/rest/v1/ipd/schedule/type/medication?patientUuid={patientUuid}` (the slot → `medicationAdministration` object; the FHIR read fetches that object directly by its UUID)
- **Request payload:** none
- **Response status:** `200 OK`
- **Response body:**

```json
{
  "resourceType": "MedicationAdministration",
  "id": "55d5ebc4-f321-4c31-9406-50c6edb8b039",
  "meta": { "lastUpdated": "2026-09-17T17:35:41.000+00:00" },
  "status": "completed",
  "category": { "coding": [{ "code": "emergency" }] },
  "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c", "type": "Medication", "display": "Paracetamol 650 mg" },
  "subject": { "reference": "Patient/2aaea835-8154-421b-974a-fd50bfbbc3cf", "type": "Patient", "display": "Astra Jollaadhfah (Patient Identifier: ABC200007)" },
  "effectiveDateTime": "2026-09-17T17:35:17+00:00",
  "performer": [
    {
      "id": "648e0c5b-dc21-46ec-b7ab-2d79710f91d9",
      "function": { "coding": [{ "code": "dd2423f7-5e07-11ef-8f7c-0242ac120002", "display": "Performer" }], "text": "Performer" },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" }
    },
    {
      "id": "ed5ab632-cce1-4006-a6ff-5e4df871091f",
      "function": { "coding": [{ "code": "dd249a2f-5e07-11ef-8f7c-0242ac120002", "display": "Witness" }], "text": "Witness" },
      "actor": { "reference": "Practitioner/05d6752c-5e08-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "doctor" }, "display": "Neha Anand" }
    }
  ],
  "note": [
    {
      "id": "00823d83-4acd-4c1b-a8bd-879baa376ea1",
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" },
      "time": "2026-09-17T17:35:41+00:00",
      "text": "dasfs"
    }
  ],
  "dosage": {
    "route": { "coding": [{ "code": "d4634f75-5e07-11ef-8f7c-0242ac120002", "display": "Oral" }], "text": "Oral" },
    "dose": { "value": 1.0, "unit": "Tablet", "system": "http://snomed.info/sct", "code": "385055001" }
  }
}
```

- **Result:** Pass
- **Not-found check:** `GET /MedicationAdministration/00000000-0000-0000-0000-000000000000` → `404`, `OperationOutcome` "Resource of type MedicationAdministration with ID 00000000-0000-0000-0000-000000000000 is not known".

---

### 5. Scheduled administration create (BAH-5112 / B1 / B5)

- **Method / URL:** `POST /openmrs/ws/fhir2/R4/MedicationAdministration`
- **Old REST equivalent:** `POST /openmrs/ws/rest/v1/ipd/scheduledMedicationAdministrations` with `[{ "patientUuid", "orderUuid", "providers":[{ "providerUuid", "function" }], "notes":[{ "authorUuid", "text" }], "status", "slotUuid", "administeredDateTime" }]` (epoch seconds)
- **Request payload:**

```json
{
  "resourceType": "MedicationAdministration",
  "status": "completed",
  "subject": { "reference": "Patient/fc4fe066-47e8-41be-8805-e2b825502cda" },
  "request": { "reference": "MedicationRequest/74eab561-d658-47c5-b6e5-7a5b8284704f" },
  "performer": [
    {
      "function": { "text": "Performer", "coding": [{ "code": "Performer" }] },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" }
    }
  ],
  "note": [
    {
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" },
      "text": "BAH-5112 v2 scheduled administration test"
    }
  ],
  "effectiveDateTime": "2026-09-17T10:02:00.000Z",
  "extension": [
    {
      "url": "http://fhir.openmrs.org/ext/ipd/medication-administration/slot",
      "valueString": "cccfad35-c185-49f0-a158-265fbafc6731"
    }
  ]
}
```

- **Response status:** `201 Created` — `Location: http://bahmni.standard/openmrs/ws/fhir2/R4/MedicationAdministration/67407f32-b760-4363-81e1-5ba6cdba52c7`
- **Response body:**

```json
{
  "resourceType": "MedicationAdministration",
  "id": "67407f32-b760-4363-81e1-5ba6cdba52c7",
  "meta": { "lastUpdated": "2026-11-04T12:20:08.000+00:00" },
  "status": "completed",
  "medicationReference": {
    "reference": "Medication/258cd168-577f-4e56-9c05-2b939e22d6c3",
    "type": "Medication",
    "display": "Omeprazole 10 mg"
  },
  "subject": {
    "reference": "Patient/fc4fe066-47e8-41be-8805-e2b825502cda",
    "type": "Patient",
    "display": "Paul P (Patient Identifier: ABC200002)"
  },
  "context": {
    "reference": "Encounter/9641d536-08fc-40a8-886d-5481d23806b1",
    "type": "Encounter"
  },
  "effectiveDateTime": "2026-09-17T10:02:00+00:00",
  "performer": [
    {
      "id": "2a29e217-36d8-430c-a63e-fc25546bab5d",
      "function": { "coding": [{ "code": "dd2423f7-5e07-11ef-8f7c-0242ac120002", "display": "Performer" }], "text": "Performer" },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" }
    }
  ],
  "request": { "reference": "MedicationRequest/74eab561-d658-47c5-b6e5-7a5b8284704f", "type": "MedicationRequest" },
  "note": [
    {
      "id": "86c0b681-0208-4e24-8ecf-cded590d278b",
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" },
      "time": "2026-11-04T12:20:08+00:00",
      "text": "BAH-5112 v2 scheduled administration test"
    }
  ]
}
```

- **Result:** Pass
- **B1 verified:** `medicationReference` is now present and derived from the order's drug (`Medication/258cd168…`, "Omeprazole 10 mg"). It is **not** persisted on the administration: DB row `67407f32…` has `drug_id = NULL`, `drug_order_id = 1`.
- **B5 verified:** `context` is now present and derived from the order's encounter (`Encounter/9641d536…`). `GET` of the created resource returns the same `medicationReference` and `context`.
- **Search verification:** `?visit=e75650b8-096b-4e27-bd8a-dc693ed1d3a1` → `total: 1` (this resource); `?context=9641d536-08fc-40a8-886d-5481d23806b1` → `total: 1` (this resource).
- **Slot:** `cccfad35-c185-49f0-a158-265fbafc6731` became `COMPLETED` and is linked to this administration.
- **Deviation from the original run:** the original §5 response had no `medicationReference` and no `context`; both are now returned. The slot extension is still not echoed on the resource (the slot is linked server-side).

---

### 6. Emergency (adhoc) administration create (BAH-5112)

- **Method / URL:** `POST /openmrs/ws/fhir2/R4/MedicationAdministration`
- **Old REST equivalent:** `POST /openmrs/ws/rest/v1/ipd/adhocMedicationAdministrations` with `{ "patientUuid", "drugUuid", "dose", "doseUnits", "route", "providers":[{ "providerUuid", "function" }], "notes":[{ "authorUuid", "text" }], "status", "administeredDateTime" }`
- **Request payload:**

```json
{
  "resourceType": "MedicationAdministration",
  "status": "completed",
  "category": { "coding": [{ "code": "emergency" }] },
  "subject": { "reference": "Patient/c35ae72e-da69-4aae-bbb2-8003e7d94c74" },
  "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" },
  "performer": [
    {
      "function": { "text": "Performer", "coding": [{ "code": "Performer" }] },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" }
    },
    {
      "function": { "text": "Witness", "coding": [{ "code": "Witness" }] },
      "actor": { "reference": "Practitioner/05d6752c-5e08-11ef-8f7c-0242ac120002" }
    }
  ],
  "note": [
    {
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" },
      "text": "BAH-5112 v2 emergency administration test"
    }
  ],
  "effectiveDateTime": "2026-11-04T13:00:00.000Z",
  "dosage": {
    "dose": { "value": 1, "unit": "Tablet" },
    "route": { "text": "Oral" }
  }
}
```

- **Response status:** `201 Created` — `Location: http://bahmni.standard/openmrs/ws/fhir2/R4/MedicationAdministration/69e371e7-ea4f-4fe8-8617-317a442c2cd7`
- **Response body:**

```json
{
  "resourceType": "MedicationAdministration",
  "id": "69e371e7-ea4f-4fe8-8617-317a442c2cd7",
  "meta": { "lastUpdated": "2026-11-04T12:20:08.000+00:00" },
  "status": "completed",
  "category": { "coding": [{ "code": "emergency" }] },
  "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c", "type": "Medication", "display": "Paracetamol 650 mg" },
  "subject": { "reference": "Patient/c35ae72e-da69-4aae-bbb2-8003e7d94c74", "type": "Patient", "display": "Dev D D (Patient Identifier: ABC1311367953)" },
  "effectiveDateTime": "2026-11-04T13:00:00+00:00",
  "performer": [
    {
      "id": "252d2ff9-4c44-4319-9f11-3b110cf7cbcd",
      "function": { "coding": [{ "code": "dd2423f7-5e07-11ef-8f7c-0242ac120002", "display": "Performer" }], "text": "Performer" },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" }
    },
    {
      "id": "00e6ee45-0f34-4649-9f40-5b33b9a62813",
      "function": { "coding": [{ "code": "dd249a2f-5e07-11ef-8f7c-0242ac120002", "display": "Witness" }], "text": "Witness" },
      "actor": { "reference": "Practitioner/05d6752c-5e08-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "doctor" }, "display": "Neha Anand" }
    }
  ],
  "note": [
    {
      "id": "087baff0-753f-4bab-a86d-0aabc9e6af8a",
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" },
      "time": "2026-11-04T12:20:08+00:00",
      "text": "BAH-5112 v2 emergency administration test"
    }
  ],
  "dosage": {
    "route": { "coding": [{ "code": "d4634f75-5e07-11ef-8f7c-0242ac120002", "display": "Oral" }], "text": "Oral" },
    "dose": { "value": 1.0, "unit": "Tablet", "system": "http://snomed.info/sct", "code": "385055001" }
  }
}
```

- **Result:** Pass
- **Notes:** `dosage.dose.unit` returns `"Tablet"` with SNOMED `system`/`code` (unchanged from the original run). Performer order is `Performer, Witness` in this run; the collection is a `HashSet`, so performer order is not guaranteed. Adhoc administrations still have no `context` (no order to derive it from); they are found by `?visit=&category=emergency` through their slot.

---

### 7. Adhoc administration update — FHIR replace semantics (BAH-5112 / B3)

- **Method / URL:** `PUT /openmrs/ws/fhir2/R4/MedicationAdministration/69e371e7-ea4f-4fe8-8617-317a442c2cd7`
- **Old REST equivalent:** `PUT /openmrs/ws/rest/v1/ipd/adhocMedicationAdministrations/{uuid}` with the same body as adhoc create (no `id`; append-only — performers/notes are appended and other fields are ignored). The FHIR `PUT` now replaces scalar fields and performers and appends notes (B3).
- **Request payload:**

```json
{
  "resourceType": "MedicationAdministration",
  "id": "69e371e7-ea4f-4fe8-8617-317a442c2cd7",
  "status": "completed",
  "category": { "coding": [{ "code": "emergency" }] },
  "subject": { "reference": "Patient/c35ae72e-da69-4aae-bbb2-8003e7d94c74" },
  "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c" },
  "performer": [
    {
      "function": { "text": "Performer", "coding": [{ "code": "Performer" }] },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" }
    }
  ],
  "note": [
    {
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" },
      "text": "BAH-5112 v2 emergency update test"
    }
  ],
  "effectiveDateTime": "2026-11-04T13:20:00.000Z",
  "dosage": {
    "dose": { "value": 2, "unit": "Tablet" },
    "route": { "text": "Oral" }
  }
}
```

- **Response status:** `200 OK`
- **Response body (trimmed):**

```json
{
  "resourceType": "MedicationAdministration",
  "id": "69e371e7-ea4f-4fe8-8617-317a442c2cd7",
  "meta": { "lastUpdated": "2026-11-04T12:20:09.000+00:00" },
  "status": "completed",
  "category": { "coding": [{ "code": "emergency" }] },
  "medicationReference": { "reference": "Medication/012be465-f26a-4b4c-8c8b-356e7b81154c", "type": "Medication", "display": "Paracetamol 650 mg" },
  "subject": { "reference": "Patient/c35ae72e-da69-4aae-bbb2-8003e7d94c74", "type": "Patient", "display": "Dev D D (Patient Identifier: ABC1311367953)" },
  "effectiveDateTime": "2026-11-04T13:20:00+00:00",
  "performer": [
    {
      "id": "9e16a13d-2c3b-4908-9c6b-4225fb9fe20f",
      "function": { "coding": [{ "code": "dd2423f7-5e07-11ef-8f7c-0242ac120002", "display": "Performer" }], "text": "Performer" },
      "actor": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" }
    }
  ],
  "note": [
    { "id": "087baff0-753f-4bab-a86d-0aabc9e6af8a", "text": "BAH-5112 v2 emergency administration test" },
    { "id": "e696c1c6-fa6f-481f-a228-1311e9cb9d2d", "text": "BAH-5112 v2 emergency update test" }
  ],
  "dosage": {
    "route": { "coding": [{ "code": "d4634f75-5e07-11ef-8f7c-0242ac120002", "display": "Oral" }], "text": "Oral" },
    "dose": { "value": 2.0, "unit": "Tablet", "system": "http://snomed.info/sct", "code": "385055001" }
  }
}
```

- **Result:** Pass
- **B3 verified:**
  - Scalar fields replaced: `effectiveDateTime` `13:00 → 13:20`, `dose` `1 → 2`.
  - Performers **replaced**: 2 → 1. DB shows only one `medication_administration_performer` row (`9e16a13d…`); the previous two rows were physically deleted by `cascade="all, delete-orphan"`.
  - Notes **appended**: 1 → 2 (audit trail preserved).
- **Deviation from the original run:** the original §7 appended performers (2 → 4) and notes (1 → 2), matching the legacy REST endpoint. FHIR `PUT` now follows FHIR replace semantics; the legacy REST endpoint keeps append-only behaviour (see §12).
- **Validation checks:** `PUT` without `medicationReference` → 400; `PUT` without `performer` → 400; `PUT` with a `request` (scheduled) → 400; body `id` mismatching the URL → 400.

---

### 8. Amend note — JSON Patch (BAH-5112)

- **Method / URL:** `PATCH /openmrs/ws/fhir2/R4/MedicationAdministration/69e371e7-ea4f-4fe8-8617-317a442c2cd7` (`Content-Type: application/json-patch+json`)
- **Old REST equivalent:** `POST /openmrs/ws/rest/v1/ipd/medicationAdministration/{uuid}/note` with `{ "authorUuid", "recordedTime" (epoch s), "text", "statusReasonUuid" }`
- **Request payload:**

```json
[
  {
    "op": "add",
    "path": "/note/-",
    "value": {
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" },
      "time": "2026-11-04T13:25:00.000Z",
      "text": "BAH-5112 v2 amendment note test",
      "extension": [
        {
          "url": "http://fhir.openmrs.org/ext/ipd/note/amendment-reason",
          "valueCodeableConcept": { "coding": [{ "code": "459fd8cd-568e-4d36-b5f5-88e861db62b9" }] }
        }
      ]
    }
  }
]
```

- **Response status:** `200 OK`
- **Response body (notes array of the returned MedicationAdministration):**

```json
{
  "resourceType": "MedicationAdministration",
  "id": "69e371e7-ea4f-4fe8-8617-317a442c2cd7",
  "note": [
    {
      "id": "087baff0-753f-4bab-a86d-0aabc9e6af8a",
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" },
      "time": "2026-11-04T12:20:08+00:00",
      "text": "BAH-5112 v2 emergency administration test"
    },
    {
      "id": "c7b1fdbb-52b5-4a3b-95cb-db1948b868ba",
      "extension": [
        { "url": "http://fhir.openmrs.org/ext/ipd/note/previous-note-id", "valueString": "e696c1c6-fa6f-481f-a228-1311e9cb9d2d" },
        {
          "url": "http://fhir.openmrs.org/ext/ipd/note/amendment-reason",
          "valueCodeableConcept": { "coding": [{ "code": "459fd8cd-568e-4d36-b5f5-88e861db62b9", "display": "Incorrect Time" }], "text": "Incorrect Time" }
        }
      ],
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" },
      "time": "2026-11-04T13:25:00+00:00",
      "text": "BAH-5112 v2 amendment note test"
    },
    {
      "id": "e696c1c6-fa6f-481f-a228-1311e9cb9d2d",
      "authorReference": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002", "type": "Practitioner", "identifier": { "value": "superman" }, "display": "Super Man" },
      "time": "2026-11-04T12:20:09+00:00",
      "text": "BAH-5112 v2 emergency update test"
    }
  ]
}
```

- **Result:** Pass
- **Notes:** the amendment note `c7b1fdbb…` points at the latest note (`e696c1c6…`) via `previous-note-id`, and carries the amendment reason `Incorrect Time`. Note ordering in the response is not guaranteed (`HashSet`).

---

### 9. Acknowledge amendment — `$acknowledge` operation (BAH-5112 / B4)

- **Method / URL:** `POST /openmrs/ws/fhir2/R4/MedicationAdministration/69e371e7-ea4f-4fe8-8617-317a442c2cd7/$acknowledge`
- **Old REST equivalent:** `POST /openmrs/ws/rest/v1/ipd/medicationAdministration/{uuid}/acknowledgement` with `{ "remarks", "approvedByUuid" }`. The original run modelled this as `POST /openmrs/ws/fhir2/R4/Task`; v2 replaces that with the dedicated `$acknowledge` operation (B4).
- **Request payload:**

```json
{
  "resourceType": "Parameters",
  "parameter": [
    { "name": "remarks", "valueString": "BAH-5112 v2 acknowledgement test" }
  ]
}
```

- **Response status:** `200 OK`
- **Response body:**

```json
{
  "resourceType": "Task",
  "id": "3b79aa67-6b6e-4d60-b4ac-893435e6346e",
  "meta": { "lastUpdated": "2026-11-04T12:20:09.000+00:00" },
  "status": "completed",
  "intent": "order",
  "focus": { "reference": "MedicationAdministration/69e371e7-ea4f-4fe8-8617-317a442c2cd7" },
  "for": { "reference": "Patient/c35ae72e-da69-4aae-bbb2-8003e7d94c74" },
  "authoredOn": "2026-11-04T12:20:09+00:00",
  "owner": { "reference": "Practitioner/d7a67c17-5e07-11ef-8f7c-0242ac120002" },
  "note": [{ "text": "BAH-5112 v2 acknowledgement test" }]
}
```

- **Result:** Pass
- **B4 verified:** the operation is advertised in the CapabilityStatement, returns a valid FHIR `Task`, and the acknowledgement is persisted. The stored task (`fhir_task` row `3b79aa67…`) has `status = COMPLETED`, `comment = "BAH-5112 v2 acknowledgement test"`, and `focus = MedicationAdministrationNote/c7b1fdbb…` (the amendment note). The API response deliberately exposes `focus = MedicationAdministration/<uuid>` because the note is part of the administration, not a standalone FHIR resource; `for` and `owner` are derived from the stored references' target UUIDs.
- **Repeat call:** `POST …/$acknowledge` again → `400 Bad Request`:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [{ "severity": "error", "code": "processing", "diagnostics": "Medication administration is already acknowledged and cannot be acknowledged again." }]
}
```

- **Deviation from the original run:** the original §9 used `POST /Task` with a `Task` body and returned the created Task. The overloaded `POST /Task` interception was removed; the dedicated operation is now used. The frontend posts `Parameters` to `…/$acknowledge`.
- **Bug found and fixed during this verification:** the first attempt returned `500` because the deployed Bahmni `TaskTranslator` override (`BahmniTaskTranslatorImpl`) passes the OpenMRS target type (`org.openmrs.Provider`) as the FHIR reference type and throws `IllegalArgumentException` for every stored Task (this also breaks `GET /Task/{id}`). The provider now builds the acknowledgement Task from the stored `FhirTask` and its references' target UUIDs, producing valid FHIR references and avoiding the broken override.

---

### 10. Legacy — scheduled administration create

- **Method / URL:** `POST /openmrs/ws/rest/v1/ipd/scheduledMedicationAdministrations`
- **Request payload:**

```json
[
  {
    "patientUuid": "fc4fe066-47e8-41be-8805-e2b825502cda",
    "orderUuid": "74eab561-d658-47c5-b6e5-7a5b8284704f",
    "providers": [{ "providerUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "function": "Performer" }],
    "notes": [{ "authorUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "text": "BAH-5112 v2 legacy scheduled test" }],
    "status": "completed",
    "slotUuid": "ea95e937-4466-46b1-a4b1-21b49bd83d86",
    "administeredDateTime": 1789629720
  }
]
```

- **Response status:** `200 OK`
- **Response body (one element, trimmed):**

```json
[
  {
    "uuid": "417cf0ee-e152-42a3-ad25-c68a653708a8",
    "patientUuid": "fc4fe066-47e8-41be-8805-e2b825502cda",
    "encounterUuid": "9641d536-08fc-40a8-886d-5481d23806b1",
    "orderUuid": "74eab561-d658-47c5-b6e5-7a5b8284704f",
    "providers": [
      { "uuid": "50e5c7dd-7393-47c9-8647-c62a0ea9f052", "provider": { "uuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "display": "superman - Super Man" }, "function": "Performer" }
    ],
    "notes": [
      {
        "uuid": "d17b7902-9eae-4cf4-9dae-481399a5d649",
        "author": { "uuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "display": "superman - Super Man" },
        "recordedTime": 1793794809000,
        "text": "BAH-5112 v2 legacy scheduled test",
        "amendmentReason": null,
        "previousNoteUuid": null,
        "acknowledgement": null
      }
    ],
    "status": "completed",
    "statusReason": null,
    "drug": null,
    "dosingInstructions": null,
    "dose": null,
    "doseUnits": null,
    "route": null,
    "site": null,
    "administeredDateTime": 1789629720000
  }
]
```

- **Result:** Pass
- **Deviation (side effect of B5):** `encounterUuid` is now populated (`9641d536…`) because the shared factory derives the encounter from the order. The original run returned `encounterUuid: null`. `drug` remains `null` for scheduled administrations (the drug lives on the order).
- **Slot:** `ea95e937-4466-46b1-a4b1-21b49bd83d86` became `COMPLETED` and is linked to this administration.

---

### 11. Legacy — adhoc administration create

- **Method / URL:** `POST /openmrs/ws/rest/v1/ipd/adhocMedicationAdministrations`
- **Request payload:**

```json
{
  "patientUuid": "c35ae72e-da69-4aae-bbb2-8003e7d94c74",
  "drugUuid": "012be465-f26a-4b4c-8c8b-356e7b81154c",
  "dose": 1,
  "doseUnits": "Tablet",
  "route": "Oral",
  "providers": [
    { "providerUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "function": "Performer" },
    { "providerUuid": "05d6752c-5e08-11ef-8f7c-0242ac120002", "function": "Witness" }
  ],
  "notes": [{ "authorUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "text": "BAH-5112 v2 legacy adhoc test" }],
  "status": "completed",
  "administeredDateTime": 1793797200
}
```

- **Response status:** `200 OK`
- **Response body (trimmed):**

```json
{
  "uuid": "b9e8fbcd-3a0c-4b25-bdf9-0f91e38d2c00",
  "patientUuid": "c35ae72e-da69-4aae-bbb2-8003e7d94c74",
  "encounterUuid": null,
  "orderUuid": null,
  "status": "completed",
  "dose": 1.0,
  "doseUnits": { "uuid": "1513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "display": "Tablet" },
  "route": { "uuid": "d4634f75-5e07-11ef-8f7c-0242ac120002", "display": "Oral" },
  "administeredDateTime": 1793797200000,
  "providers": [
    { "function": "Performer", "provider": { "uuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002" } },
    { "function": "Witness", "provider": { "uuid": "05d6752c-5e08-11ef-8f7c-0242ac120002" } }
  ],
  "notes": [{ "text": "BAH-5112 v2 legacy adhoc test", "recordedTime": 1793794809000 }]
}
```

- **Result:** Pass
- **Notes:** unchanged legacy behaviour; `encounterUuid` stays `null` because there is no order.

---

### 12. Legacy — adhoc administration update (append-only)

- **Method / URL:** `PUT /openmrs/ws/rest/v1/ipd/adhocMedicationAdministrations/b9e8fbcd-3a0c-4b25-bdf9-0f91e38d2c00`
- **Request payload:**

```json
{
  "patientUuid": "c35ae72e-da69-4aae-bbb2-8003e7d94c74",
  "drugUuid": "012be465-f26a-4b4c-8c8b-356e7b81154c",
  "dose": 1,
  "doseUnits": "Tablet",
  "route": "Oral",
  "providers": [{ "providerUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "function": "Performer" }],
  "notes": [{ "authorUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "text": "BAH-5112 v2 legacy adhoc update test" }],
  "status": "completed",
  "administeredDateTime": 1793797200
}
```

- **Response status:** `200 OK`
- **Response body (trimmed):**

```json
{
  "uuid": "b9e8fbcd-3a0c-4b25-bdf9-0f91e38d2c00",
  "status": "completed",
  "dose": 1.0,
  "administeredDateTime": 1793797200000,
  "providers": [
    { "function": "Performer", "provider": { "uuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002" } },
    { "function": "Performer", "provider": { "uuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002" } },
    { "function": "Witness", "provider": { "uuid": "05d6752c-5e08-11ef-8f7c-0242ac120002" } }
  ],
  "notes": [
    { "text": "BAH-5112 v2 legacy adhoc test" },
    { "text": "BAH-5112 v2 legacy adhoc update test" }
  ]
}
```

- **Result:** Pass
- **Notes:** legacy REST keeps append-only semantics (performers 2 → 3, notes 1 → 2). This is the deliberate contrast with the FHIR `PUT` replace semantics in §7.

---

### 13. Legacy — amend note

- **Method / URL:** `POST /openmrs/ws/rest/v1/ipd/medicationAdministration/b9e8fbcd-3a0c-4b25-bdf9-0f91e38d2c00/note`
- **Request payload:**

```json
{
  "authorUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002",
  "recordedTime": 1793797500,
  "text": "BAH-5112 v2 legacy amendment note test",
  "statusReasonUuid": "459fd8cd-568e-4d36-b5f5-88e861db62b9"
}
```

- **Response status:** `200 OK`
- **Response body:**

```json
{
  "uuid": "2e52fb52-f5f7-4f05-b698-09759bdb5fd3",
  "author": { "uuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002", "display": "superman - Super Man" },
  "recordedTime": 1793797500000,
  "text": "BAH-5112 v2 legacy amendment note test",
  "amendmentReason": "Incorrect Time",
  "previousNoteUuid": "7c46314b-9909-4dcc-a206-318548bfcf9a",
  "acknowledgement": null
}
```

- **Result:** Pass

---

### 14. Legacy — acknowledge amendment

- **Method / URL:** `POST /openmrs/ws/rest/v1/ipd/medicationAdministration/b9e8fbcd-3a0c-4b25-bdf9-0f91e38d2c00/acknowledgement`
- **Request payload:**

```json
{
  "remarks": "BAH-5112 v2 legacy acknowledgement test",
  "approvedByUuid": "d7a67c17-5e07-11ef-8f7c-0242ac120002"
}
```

- **Response status:** `200 OK`
- **Response body (trimmed):**

```json
{
  "uuid": "4fc325f8-3d7b-4a3f-9a30-e85e21e0ec3e",
  "name": "ACKNOWLEDGE_MEDICATION_NOTE",
  "status": "COMPLETED",
  "intent": "ORDER",
  "taskType": { "uuid": "56eb8040-2d0d-11f1-9dc8-0242ac120009", "display": "acknowledge_amend_note" },
  "creator": { "uuid": "d7a669e7-5e07-11ef-8f7c-0242ac120002", "display": "superman" },
  "comment": "BAH-5112 v2 legacy acknowledgement test",
  "focus": {
    "reference": "org.openmrs.module.ipd.api.model.MedicationAdministrationNote/2e52fb52-f5f7-4f05-b698-09759bdb5fd3",
    "type": "org.openmrs.module.ipd.api.model.MedicationAdministrationNote"
  },
  "for": {
    "reference": "org.openmrs.Patient/c35ae72e-da69-4aae-bbb2-8003e7d94c74",
    "type": "org.openmrs.Patient"
  }
}
```

- **Result:** Pass
- **Notes:** unchanged legacy behaviour. The legacy response exposes the OpenMRS class names in `focus`/`for`; the FHIR `$acknowledge` response (§9) exposes proper FHIR references.

---

### 15. Negative check — unsupported category on create

- **Method / URL:** `POST /openmrs/ws/fhir2/R4/MedicationAdministration`
- **Request payload:** same as §6 with `"category": { "coding": [{ "code": "inpatient" }] }`
- **Response status:** `400 Bad Request`
- **Response body:**

```json
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "Unsupported category; only 'emergency' is supported (omit category for a scheduled administration)"
    }
  ]
}
```

- **Result:** Pass

---

### 16. Negative check — scheduled create without slot extension

- **Method / URL:** `POST /openmrs/ws/fhir2/R4/MedicationAdministration`
- **Request payload:** same as §5 with the `extension` array removed
- **Response status:** `400 Bad Request`
- **Response body:**

```json
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "Extension http://fhir.openmrs.org/ext/ipd/medication-administration/slot (valueString = slot uuid) is required for a scheduled administration"
    }
  ]
}
```

- **Result:** Pass

---

### 17. Negative check — amend an acknowledged (locked) note

- **Method / URL:** `PATCH /openmrs/ws/fhir2/R4/MedicationAdministration/69e371e7-ea4f-4fe8-8617-317a442c2cd7`
- **Request payload:** same as §8
- **Response status:** `409 Conflict`
- **Response body:**

```json
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "Cannot amend note: Medication administration is acknowledged and locked."
    }
  ]
}
```

- **Result:** Pass

---

## Resources created during testing (v2 run)

| Resource | UUID | Notes |
|---|---|---|
| MedicationAdministration (scheduled, FHIR) | `67407f32-b760-4363-81e1-5ba6cdba52c7` | Slot `cccfad35-c185-49f0-a158-265fbafc6731` now `COMPLETED`; `medicationReference` derived from order; `context` populated |
| MedicationAdministration (emergency, FHIR) | `69e371e7-ea4f-4fe8-8617-317a442c2cd7` | Used for §7 replace, §8 amend, §9 acknowledge, §17 lock check |
| Task (acknowledgement, FHIR `$acknowledge`) | `3b79aa67-6b6e-4d60-b4ac-893435e6346e` | Stored focus `MedicationAdministrationNote/c7b1fdbb…`; API focus `MedicationAdministration/69e371e7…` |
| Note (amendment, FHIR) | `c7b1fdbb-52b5-4a3b-95cb-db1948b868ba` | On `69e371e7…`; previous note `e696c1c6…`; reason `Incorrect Time` |
| MedicationAdministration (scheduled, legacy) | `417cf0ee-e152-42a3-ad25-c68a653708a8` | Slot `ea95e937-4466-46b1-a4b1-21b49bd83d86` now `COMPLETED`; `encounterUuid` populated |
| MedicationAdministration (emergency, legacy) | `b9e8fbcd-3a0c-4b25-bdf9-0f91e38d2c00` | Used for §12 append update, §13 amend, §14 acknowledge |
| Task (acknowledgement, legacy) | `4fc325f8-3d7b-4a3f-9a30-e85e21e0ec3e` | Focus `MedicationAdministrationNote/2e52fb52…` |
| Note (amendment, legacy) | `2e52fb52-f5f7-4f05-b698-09759bdb5fd3` | On `b9e8fbcd…` |

---

## Summary of deviations from the original run

1. **§2 emergency search** now uses `?visit=<visitUuid>&category=emergency`; `category=emergency` without `visit` returns `400`. `context` remains a standard Encounter filter. The result count is higher because test data accumulated across runs.
2. **§3** `?visit=` on the non-emergency path returns only administrations whose encounter belongs to that visit. Adhoc administrations (no encounter) return `0`; scheduled administrations created after B5 return `1`.
3. **§5 scheduled create** now returns `medicationReference` (derived from the order, B1) and `context` (derived from the order's encounter, B5). The derived drug is not persisted (`drug_id = NULL`).
4. **§7 FHIR `PUT`** now replaces performers (2 → 1) and appends notes (1 → 2). The legacy REST `PUT` (§12) still appends performers (2 → 3) and notes (1 → 2).
5. **§9 acknowledgement** now uses `POST /MedicationAdministration/{uuid}/$acknowledge` with a `Parameters` body and returns a valid FHIR `Task` (`focus = MedicationAdministration/<uuid>`). The overloaded `POST /Task` path was removed. Repeat acknowledgement returns `400`.
6. **§10 legacy scheduled create** now returns `encounterUuid` (derived from the order) instead of `null`. This is a side effect of sharing the B5 encounter-resolution logic; `drug` remains `null`.
7. **§9 bug found and fixed during verification:** the deployed Bahmni `TaskTranslator` override throws on every stored Task (it passes `org.openmrs.Provider` as the FHIR reference type). `$acknowledge` now builds the Task from the stored `FhirTask` and its references' target UUIDs, avoiding the broken override and emitting valid FHIR references. This also means `GET /Task/{id}` remains broken in the external `fhir2addlextension` module (pre-existing, out of scope for this PR), but `$acknowledge` no longer depends on it.

---

## Build / deploy commands used for this verification

```bash
# JDK 17
export JAVA_HOME=$HOME/.sdkman/candidates/java/17.0.11-tem

# 1. Build + install the medication-administration module (API is consumed by IPD)
cd openmrs-module-medicationadministration
mvn clean install

# 2. Build the IPD module
cd ../openmrs-module-ipd
mvn clean package

# 3. Replace the deployed omods and restart OpenMRS
docker exec bahmni-standard-openmrs-1 rm -f \
  /openmrs/data/modules/bahmni-ipd-2.0.0-SNAPSHOT.omod \
  /openmrs/data/modules/medication-administration-3.0.0-SNAPSHOT.omod
docker cp openmrs-module-ipd/omod/target/bahmni-ipd-2.0.0-SNAPSHOT.omod \
  bahmni-standard-openmrs-1:/openmrs/data/modules/
docker cp openmrs-module-medicationadministration/omod/target/medication-administration-3.0.0-SNAPSHOT.omod \
  bahmni-standard-openmrs-1:/openmrs/data/modules/
docker restart bahmni-standard-openmrs-1

# 4. Wait for startup, then verify the CapabilityStatement
curl -k -u 'superman:Admin123' \
  https://bahmni.standard/openmrs/ws/fhir2/R4/metadata \
  -H 'Accept: application/fhir+json'
```

**Test suite status at build time:** `openmrs-module-medicationadministration` 19 tests pass; `openmrs-module-ipd` 120 tests pass (1 pre-existing skip).
