# Future Plans & Roadmap

This document outlines upcoming capabilities and iterative enhancements planned for MongoStudio Mobile.

## Near-Term
- [ ] **Native File Import / Export**:
  - Integrate Android Storage Access Framework (SAF) to export collections directly to `.json` files on device storage.
  - JSON document file picker to upload and import documents into collections.
- [ ] **Document Schema Analyzer**:
  - Automatically sample documents in a collection and render visual schema distribution (field types, presence percentage).
- [ ] **Query History & Bookmarks**:
  - Save frequently used filter queries and aggregation pipelines with quick-access bookmarks.
- [ ] **Biometric Lock for Encrypted Vault**:
  - Use `androidx.biometric` (fingerprint / face unlock) before decrypting and connecting to saved production clusters.

## Mid-Term
- [ ] **Full Index Performance Profiler**:
  - Run `explain("executionStats")` on queries directly from the mobile UI to view index scans vs document scans.
- [ ] **Real-time Change Streams**:
  - Support WebSocket / SSE change stream listeners for live document insertion and modification updates in a collection.
- [ ] **Multi-Cluster Quick Switcher**:
  - Swipe or dropdown quick-bar to toggle between multiple active MongoDB connections without navigating back to connection screen.
