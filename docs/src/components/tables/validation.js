/**
 * Validates consistency between backlog data and sprint data
 * @param {Array} backlogData - Array of backlog items with effettivo and stima hours
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

  // Validate that days array sums match effettivo for each sprint task
  sprintData.forEach(task => {
    if (task.days && Array.isArray(task.days)) {
      const daysSum = task.days.reduce((sum, day) => sum + (typeof day === 'number' ? day : 0), 0);
      const effettivo = typeof task.effettivo === 'number' ? task.effettivo : 0;
      const difference = Math.abs(daysSum - effettivo);
      
      if (difference > 0.01) { // Use small threshold for floating point comparison
        results.isValid = false;
        results.errors.push(
          `Task "${task.task}" (ID: ${task.id}): Days sum is ${daysSum}h but effettivo is ${effettivo}h (difference: ${difference.toFixed(2)}h)`
        );
      }
    }
  });

  // Group sprint tasks by backlogItem and sum their effettivo and stima hours
  const sprintTotals = sprintData.reduce((acc, task) => {
    const backlogItem = task.backlogItem || '';
    const effettivo = typeof task.effettivo === 'number' ? task.effettivo : 0;
    const stima = typeof task.stima === 'number' ? task.stima : 0;
    
    if (!acc[backlogItem]) {
      acc[backlogItem] = {
        totalEffettivo: 0,
        totalStima: 0,
        tasks: []
      };
    }
    
    acc[backlogItem].totalEffettivo += effettivo;
    acc[backlogItem].totalStima += stima;
    acc[backlogItem].tasks.push({
      id: task.id,
      task: task.task,
      effettivo,
      stima
    });
    
    return acc;
  }, {});

  // Check each backlog item against sprint totals
  backlogData.forEach(backlogItem => {
    const itemName = backlogItem.item || `ID ${backlogItem.id}`;
    const backlogEffettivo = typeof backlogItem.effettivo === 'number' ? backlogItem.effettivo : 0;
    const backlogStima = typeof backlogItem.stima === 'number' ? backlogItem.stima : 0;
    
    // Find corresponding sprint data
    const sprintData = sprintTotals[itemName];
    
    if (!sprintData) {
      // No sprint tasks found for this backlog item
      if (backlogEffettivo > 0 || backlogStima > 0) {
        results.warnings.push(`Backlog item "${itemName}" has Effettivo ${backlogEffettivo}h, Stima ${backlogStima}h but no corresponding sprint tasks found`);
        results.details[itemName] = {
          backlogEffettivo,
          sprintEffettivo: 0,
          diffEffettivo: backlogEffettivo,
          backlogStima,
          sprintStima: 0,
          diffStima: backlogStima,
          status: 'missing_sprint_data'
        };
      }
      return;
    }

    const sprintEffettivo = sprintData.totalEffettivo;
    const sprintStima = sprintData.totalStima;
    const diffEffettivo = Math.abs(backlogEffettivo - sprintEffettivo);
    const diffStima = Math.abs(backlogStima - sprintStima);
    
    // Store details for this item
    results.details[itemName] = {
      backlogEffettivo,
      sprintEffettivo,
      diffEffettivo,
      backlogStima,
      sprintStima,
      diffStima,
      tasks: sprintData.tasks
    };

    // Check for mismatches
    if (diffEffettivo > 0.01) {
      results.isValid = false;
      results.errors.push(
        `Mismatch in "${itemName}" effettivo: Backlog shows ${backlogEffettivo}h, Sprint tasks total ${sprintEffettivo}h (difference: ${diffEffettivo.toFixed(2)}h)`
      );
      results.details[itemName].status = 'effettivo_mismatch';
    } else if (diffStima > 0.01) {
      results.isValid = false;
      results.errors.push(
        `Mismatch in "${itemName}" stima: Backlog shows ${backlogStima}h, Sprint tasks total ${sprintStima}h (difference: ${diffStima.toFixed(2)}h)`
      );
      results.details[itemName].status = 'stima_mismatch';
    } else {
      results.details[itemName].status = 'valid';
    }
  });

  // Check for sprint tasks that don't have corresponding backlog items
  Object.keys(sprintTotals).forEach(sprintItemName => {
    const hasBacklogItem = backlogData.some(item => item.item === sprintItemName);
    
    if (!hasBacklogItem && sprintItemName !== '') {
      results.warnings.push(
        `Sprint tasks for "${sprintItemName}" (Effettivo ${sprintTotals[sprintItemName].totalEffettivo}h, Stima ${sprintTotals[sprintItemName].totalStima}h) have no corresponding backlog item`
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
