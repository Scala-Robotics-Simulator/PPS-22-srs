## Cross-Validation Usage Example

### How to use the validation feature

The `BacklogTable` component now supports cross-validation with sprint data. Here's how to use it:

```jsx
import { BacklogTable } from '@site/src/components/tables/BacklogTable';

// Your backlog data
const backlogData = [
  {
    id: 1,
    item: "Setup repository",
    stima: 6,
    effettivo: 6,
    sprints: [6, 0, 0, 0, 0]
  },
  // ... more items
];

// Your sprint data (from all sprints)
const allSprintData = [
  // Sprint 0 data
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
    task: "Integrazione scalafmt, scalafix e wartremover",
    volontario: "Ceredi",
    stima: 1,
    effettivo: 0.5,
    days: [0.5, 0, 0, 0, 0, 0, 0, 0]
  },
  // ... more sprint tasks from all sprints
];

// Use the component with validation
<BacklogTable 
  data={backlogData} 
  sprintData={allSprintData} 
  showValidation={true} 
/>
```

### What the validation checks:

1. **Consistency**: Compares the `effettivo` hours in backlog items with the sum of `effettivo` hours from all related sprint tasks
2. **Missing data**: Warns if backlog items have no corresponding sprint tasks
3. **Orphaned tasks**: Warns if sprint tasks reference non-existent backlog items
4. **Visual indicators**: Highlights problematic rows in red (errors) or yellow (warnings)

### Validation results display:

- ✅ **Green banner**: All data is consistent
- ❌ **Red banner**: Errors found (mismatched hours)
- ⚠️ **Yellow warnings**: Missing or orphaned data
- **Row highlighting**: Problem rows are highlighted with tooltips showing details
- **Icons**: ❌ for errors, ⚠️ for warnings in the effettivo column

### Props:

- `data`: Array of backlog items (required)
- `sprintData`: Array of all sprint tasks from all sprints (optional)
- `showValidation`: Boolean to enable/disable validation display (default: false)
