/**
 * Validates consistency between backlog data and sprint data
 * @param {Array} backlogData - Array of backlog items with effettivo hours
 * @param {Array} sprintData - Array of sprint tasks with backlogItem references
 * @returns {Object} Validation results with errors and warnings
 */
export function validateBacklogConsistency(backlogData, sprintData) {
  const results = {
    isValid: true,
    errors: [],
    warnings: [],
    details: {}
  };

  // Early return if data is invalid
  if (!backlogData || !Array.isArray(backlogData) || !sprintData || !Array.isArray(sprintData)) {
    results.isValid = false;
    results.errors.push("Invalid data: Both backlogData and sprintData must be arrays");
    return results;
  }

  // If either array is empty, no validation needed
  if (backlogData.length === 0 || sprintData.length === 0) {
    results.warnings.push("No validation performed: One or both datasets are empty");
    return results;
  }

  // Group sprint tasks by backlogItem and sum their effettivo hours
  const sprintTotals = sprintData.reduce((acc, task) => {
    const backlogItem = task.backlogItem || '';
    const effettivo = typeof task.effettivo === 'number' ? task.effettivo : 0;
    
    if (!acc[backlogItem]) {
      acc[backlogItem] = {
        totalEffettivo: 0,
        tasks: []
      };
    }
    
    acc[backlogItem].totalEffettivo += effettivo;
    acc[backlogItem].tasks.push({
      id: task.id,
      task: task.task,
      effettivo: effettivo
    });
    
    return acc;
  }, {});

  // Check each backlog item against sprint totals
  backlogData.forEach(backlogItem => {
    const itemName = backlogItem.item || `ID ${backlogItem.id}`;
    const backlogEffettivo = typeof backlogItem.effettivo === 'number' ? backlogItem.effettivo : 0;
    
    // Find corresponding sprint data
    const sprintData = sprintTotals[itemName];
    
    if (!sprintData) {
      // No sprint tasks found for this backlog item
      if (backlogEffettivo > 0) {
        results.warnings.push(`Backlog item "${itemName}" has ${backlogEffettivo}h but no corresponding sprint tasks found`);
        results.details[itemName] = {
          backlogEffettivo,
          sprintEffettivo: 0,
          difference: backlogEffettivo,
          status: 'missing_sprint_data'
        };
      }
      return;
    }

    const sprintEffettivo = sprintData.totalEffettivo;
    const difference = Math.abs(backlogEffettivo - sprintEffettivo);
    
    // Store details for this item
    results.details[itemName] = {
      backlogEffettivo,
      sprintEffettivo,
      difference,
      tasks: sprintData.tasks
    };

    // Check for mismatches
    if (difference > 0.01) { // Use small threshold for floating point comparison
      results.isValid = false;
      const status = backlogEffettivo > sprintEffettivo ? 'backlog_higher' : 'sprint_higher';
      
      results.errors.push(
        `Mismatch in "${itemName}": Backlog shows ${backlogEffettivo}h, Sprint tasks total ${sprintEffettivo}h (difference: ${difference.toFixed(2)}h)`
      );
      
      results.details[itemName].status = status;
    } else {
      results.details[itemName].status = 'valid';
    }
  });

  // Check for sprint tasks that don't have corresponding backlog items
  Object.keys(sprintTotals).forEach(sprintItemName => {
    const hasBacklogItem = backlogData.some(item => item.item === sprintItemName);
    
    if (!hasBacklogItem && sprintItemName !== '') {
      results.warnings.push(
        `Sprint tasks for "${sprintItemName}" (${sprintTotals[sprintItemName].totalEffettivo}h) have no corresponding backlog item`
      );
    }
  });

  return results;
}

/**
 * Formats validation results for display
 * @param {Object} validationResults - Results from validateBacklogConsistency
 * @returns {Object} Formatted results for UI display
 */
export function formatValidationResults(validationResults) {
  if (!validationResults) return null;

  const { isValid, errors, warnings, details } = validationResults;
  
  return {
    summary: {
      isValid,
      errorCount: errors.length,
      warningCount: warnings.length,
      totalItems: Object.keys(details).length
    },
    messages: {
      errors: errors.map((error, index) => ({ id: index, message: error, type: 'error' })),
      warnings: warnings.map((warning, index) => ({ id: index, message: warning, type: 'warning' }))
    },
    itemDetails: Object.entries(details).map(([itemName, detail]) => ({
      itemName,
      ...detail,
      isValid: detail.status === 'valid',
      severity: detail.status === 'valid' ? 'success' : 
                detail.status === 'missing_sprint_data' ? 'warning' : 'error'
    }))
  };
}
