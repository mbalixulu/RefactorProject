# MandatesResolutionService Refactoring Documentation

## Overview

The `MandatesResolutionService` class has been refactored to improve separation of concerns and maintainability. This document outlines the changes made and provides guidance for developers.

## Refactoring Summary

### Original State
- **MandatesResolutionService**: 1,557 lines of code with multiple responsibilities

### Refactored State  
- **MandatesResolutionService**: 794 lines (49% reduction)
- **MandateCaptureService**: New service for mandate capture and submission
- **SearchMandatesService**: New service for search and retrieval operations
- **AuditTrailService**: New service for audit trail and status management

## New Service Classes

### 1. MandateCaptureService
**Purpose**: Handles mandate capture and submission operations.

**Responsibilities**:
- Creating and submitting mandate requests
- Creating resolution requests
- Creating combined mandate-resolution requests
- Sending requests to staging
- Managing signature card submissions

**Key Methods**:
- `createRequest()` - Creates a mandate request with accounts and authorities
- `createRequestReso()` - Creates a resolution request with authorities only
- `createRequestMandates()` - Creates a mandate-only request
- `sendRequestStaging()` - Sends a request to staging as a draft
- `sendRequestSignatureCard()` - Sends a request for signature card to staging

### 2. SearchMandatesService
**Purpose**: Handles search and retrieval of mandate and resolution data.

**Responsibilities**:
- Company lookups
- Draft request retrieval and processing
- Request searches
- Data export operations
- Request details retrieval

**Key Methods**:
- `getCompanyByRegistration(String)` - Retrieves company information by registration number
- `getAllDrafts()` - Retrieves all draft requests from staging
- `getDraftsById(Long)` - Retrieves a specific draft request by ID
- `processViewDraft(RequestStagingDTO)` - Processes a draft request for viewing and editing
- `exportCsv(ExportModel)` - Exports requests to CSV based on filter criteria
- `getRequestById(Long)` - Retrieves detailed request information by ID
- `getAllRecords()` - Retrieves all request records

### 3. AuditTrailService
**Purpose**: Handles audit trail and status management.

**Responsibilities**:
- Request status updates
- Status checking and workflow management
- Audit trail tracking

**Key Methods**:
- `statusUpdated(Long, String, String, String, String)` - Updates the status of a request
- `statusCheck(String)` - Checks and sets appropriate flags based on request status

## Migration Guide

### For Developers

#### Using the New Services

Instead of using `MandatesResolutionService` for all operations, inject the appropriate specialized service:

**Before:**
```java
@Autowired
private MandatesResolutionService mandatesResolutionService;

// Usage
String result = mandatesResolutionService.createRequest();
CompanyDTO company = mandatesResolutionService.getCompanyByRegistration(regNumber);
mandatesResolutionService.statusUpdated(id, outcome, subStatus, status, user);
```

**After:**
```java
@Autowired
private MandateCaptureService mandateCaptureService;

@Autowired
private SearchMandatesService searchMandatesService;

@Autowired
private AuditTrailService auditTrailService;

// Usage
String result = mandateCaptureService.createRequest();
CompanyDTO company = searchMandatesService.getCompanyByRegistration(regNumber);
auditTrailService.statusUpdated(id, outcome, subStatus, status, user);
```

### Backward Compatibility

All extracted methods in `MandatesResolutionService` have been marked as `@Deprecated` and now delegate to the new services. This ensures that existing code continues to work without modification.

**Deprecated Methods** (will be removed in future versions):
- `getCompanyByRegistration(String)` → Use `SearchMandatesService.getCompanyByRegistration(String)`
- `sendRequestStaging()` → Use `MandateCaptureService.sendRequestStaging()`
- `sendRequestSignatureCard()` → Use `MandateCaptureService.sendRequestSignatureCard()`
- `createRequest()` → Use `MandateCaptureService.createRequest()`
- `createRequestReso()` → Use `MandateCaptureService.createRequestReso()`
- `createRequestMandates()` → Use `MandateCaptureService.createRequestMandates()`
- `getAllDrafts()` → Use `SearchMandatesService.getAllDrafts()`
- `getDraftsById(Long)` → Use `SearchMandatesService.getDraftsById(Long)`
- `processViewDraft(RequestStagingDTO)` → Use `SearchMandatesService.processViewDraft(RequestStagingDTO)`
- `exportCsv(ExportModel)` → Use `SearchMandatesService.exportCsv(ExportModel)`
- `getRequestById(Long)` → Use `SearchMandatesService.getRequestById(Long)`
- `statusUpdated(...)` → Use `AuditTrailService.statusUpdated(...)`
- `statusCheck(String)` → Use `AuditTrailService.statusCheck(String)`
- `getAllRecords()` → Use `SearchMandatesService.getAllRecords()`

### Retained Functionality in MandatesResolutionService

The following methods remain in `MandatesResolutionService` as they are UI-specific helper methods and session management logic:

**UI Helper Methods:**
- `setAllDirctors(Map, String)` - Sets director information from UI form
- `removeSpecificAdmin(int)` - Removes a specific admin from directors list
- `removeSpecificAdminReso(int)` - Removes a specific admin from resolution directors
- `removeSpecificSignatory(int)` - Removes a specific signatory
- `getDirectorDetails(...)` - Retrieves director details by user index
- `copyDirectorModel(DirectorModel)` - Copies director model fields
- `getUpdatedDirector(...)` - Updates director information from form
- `getSignatory(...)` - Retrieves signatory from list
- `copySignatoryModel(SignatoryModel)` - Copies signatory model fields
- `getUpdatedSignatory(...)` - Updates signatory information
- `setSearchResult(Map)` - Sets search result data into request wrapper
- `setSearchResultDraft(...)` - Sets search result data for draft requests
- `setSignatory(Map)` - Creates signatory model from UI form
- `getAccount(...)` - Retrieves account from list
- `updateAccount(...)` - Updates account information
- `updateAccountSingle(...)` - Updates a single account
- `getSignatoryData(...)` - Retrieves signatory data from accounts
- `getAddAccountList(...)` - Updates signatory details in account list

**Complex Session Management:**
- `updateDraftByStagingIdStepOne(...)` - Updates draft request with complex form/session handling
- `setUpdatedDirector(...)` - Updates director with check flags
- `updateViewRequest(RequestDetails)` - Updates view request with account/director changes

## Testing

### Unit Testing Recommendations

1. **MandateCaptureService**: Test each submission method with mocked RestTemplate
2. **SearchMandatesService**: Test search and retrieval methods with mocked HTTP responses
3. **AuditTrailService**: Test status update logic and session management
4. **MandatesResolutionService**: Test UI helper methods and delegation to new services

### Integration Testing

Ensure that:
1. The application starts successfully with all new services properly wired
2. Existing controller endpoints continue to function
3. Deprecated methods correctly delegate to new services
4. No regressions in functionality

## Benefits of Refactoring

1. **Improved Maintainability**: Each service has a single, well-defined responsibility
2. **Better Testability**: Smaller, focused services are easier to unit test
3. **Code Reusability**: Specialized services can be reused across different contexts
4. **Reduced Complexity**: MandatesResolutionService is now 49% smaller and easier to understand
5. **Clearer Architecture**: Separation of concerns makes the codebase more intuitive

## Future Improvements

1. **Update Controller**: Migrate `MandatesResolutionUIController` to use new services directly
2. **Remove Deprecated Methods**: After migration is complete, remove deprecated delegation methods
3. **Add Comprehensive Tests**: Create unit and integration tests for all new services
4. **Extract UI Helpers**: Consider creating a separate UI helper service for form/session management
5. **Document API**: Add OpenAPI/Swagger documentation for new service methods

## Questions or Issues?

If you encounter any issues with the refactored code or have questions about using the new services, please contact the development team or refer to the JavaDoc documentation in each service class.

---

**Refactoring Completed**: January 2026  
**Original Service Size**: 1,557 lines  
**Refactored Service Size**: 794 lines  
**Lines Extracted**: 763 (49% reduction)
