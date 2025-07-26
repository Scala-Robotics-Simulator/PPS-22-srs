// Test file for validation functionality
import { validateBacklogConsistency, formatValidationResults } from './validation.js';

// Test data
const testBacklogData = [
  {
    id: 1,
    item: "Setup repository",
    stima: 6,
    effettivo: 6,
    sprints: [6, 0, 0, 0, 0]
  },
  {
    id: 2,
    item: "Domain modeling",
    stima: 12,
    effettivo: 14,
    sprints: [14, 0, 0, 0, 0]
  }
];

const testSprintData = [
  {
    backlogItem: "Setup repository",
    id: "1.1",
    task: "Inizializzazione progetto",
    volontario: "Ceredi",
    stima: 0.5,
    effettivo: 0.5,
    days: [0.5, 0, 0, 0, 0, 0, 0, 0]
  },
  {
    backlogItem: "Setup repository",
    id: "1.2",
    task: "Integrazione tools",
    volontario: "Ceredi",
    stima: 1,
    effettivo: 5.5, // This should make total = 6, matching backlog
    days: [0.5, 0, 0, 0, 0, 0, 0, 0]
  },
  {
    backlogItem: "Domain modeling",
    id: "2.1",
    task: "Analysis",
    volontario: "Team",
    stima: 6,
    effettivo: 10, // This will cause mismatch (10 vs 14 in backlog)
    days: [2, 2, 2, 2, 2, 0, 0, 0]
  }
];

// Run validation
console.log('Testing validation...');
const results = validateBacklogConsistency(testBacklogData, testSprintData);
const formatted = formatValidationResults(results);

console.log('Raw Results:', results);
console.log('Formatted Results:', formatted);

export { testBacklogData, testSprintData };
