import { validateBacklogConsistency, formatValidationResults } from './validation.js';

export function BacklogTable({ data, sprintData, showValidation = false }) {
  // Error handling: check if data is valid
  if (!data || !Array.isArray(data) || data.length === 0) {
    return (
      <table>
        <thead>
          <tr>
            <th>Id</th>
            <th>Item</th>
            <th>Stima (h)</th>
            <th>Effettivo (h)</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td colSpan={4}>No data available</td>
          </tr>
        </tbody>
      </table>
    );
  }

  // Determine the maximum number of sprints from the data
  const maxSprints = Math.max(...data.map(item => 
    item.sprints && Array.isArray(item.sprints) ? item.sprints.length : 0
  ));
  
  // Normalize data to ensure consistent structure
  const normalizedData = data.map(item => ({
    id: item.id || '',
    item: item.item || '',
    stima: typeof item.stima === 'number' ? item.stima : 0,
    effettivo: typeof item.effettivo === 'number' ? item.effettivo : 0,
    sprints: Array.isArray(item.sprints) 
      ? [...item.sprints, ...Array(Math.max(0, maxSprints - item.sprints.length)).fill(0)]
      : Array(maxSprints).fill(0)
  }));
  
  const totals = normalizedData.reduce(
    (acc, item) => {
      acc.stima += item.stima;
      acc.effettivo += item.effettivo;
      acc.sprints = acc.sprints.map((s, i) => s + (item.sprints[i] || 0));
      return acc;
    },
    { stima: 0, effettivo: 0, sprints: Array(maxSprints).fill(0) }
  );

  // Perform validation if sprint data is provided
  const validation = sprintData && showValidation 
    ? formatValidationResults(validateBacklogConsistency(normalizedData, sprintData))
    : null;

  return (
    <>
      {/* Validation Results - Only show when there are errors or warnings */}
      {validation && (!validation.summary.isValid || validation.summary.warningCount > 0) && (
        <div style={{ marginBottom: '1rem' }}>
          <div style={{ 
            padding: '0.75rem', 
            borderRadius: '4px', 
            backgroundColor: validation.summary.isValid ? '#fff3cd' : '#f8d7da',
            border: `1px solid ${validation.summary.isValid ? '#ffeaa7' : '#f5c6cb'}`,
            color: validation.summary.isValid ? '#856404' : '#721c24'
          }}>
            <strong>Validation Results:</strong> {
              !validation.summary.isValid 
                ? `❌ ${validation.summary.errorCount} errors found`
                : `⚠️ ${validation.summary.warningCount} warnings found`
            }
          </div>
          
          {/* Error Messages */}
          {validation.messages.errors.length > 0 && (
            <div style={{ marginTop: '0.5rem' }}>
              {validation.messages.errors.map(error => (
                <div key={error.id} style={{ 
                  padding: '0.5rem', 
                  marginBottom: '0.25rem',
                  backgroundColor: '#f8d7da', 
                  border: '1px solid #f5c6cb',
                  borderRadius: '4px',
                  fontSize: '0.9rem',
                  color: '#721c24'
                }}>
                  ❌ {error.message}
                </div>
              ))}
            </div>
          )}
          
          {/* Warning Messages */}
          {validation.messages.warnings.length > 0 && (
            <div style={{ marginTop: '0.5rem' }}>
              {validation.messages.warnings.map(warning => (
                <div key={warning.id} style={{ 
                  padding: '0.5rem', 
                  marginBottom: '0.25rem',
                  backgroundColor: '#fff3cd', 
                  border: '1px solid #ffeaa7',
                  borderRadius: '4px',
                  fontSize: '0.9rem',
                  color: '#856404'
                }}>
                  ⚠️ {warning.message}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
      
      <table>
      <thead>
        <tr>
          <th>Id</th>
          <th>Item</th>
          <th>Stima (h)</th>
          <th>Effettivo (h)</th>
          {Array.from({ length: maxSprints }, (_, i) => (
            <th key={i}>S{i}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {normalizedData.map(({ id, item, stima, effettivo, sprints }) => {
          const validationItem = validation?.itemDetails.find(detail => detail.itemName === item);
          const hasError = validationItem && !validationItem.isValid && validationItem.severity === 'error';
          const hasWarning = validationItem && validationItem.severity === 'warning';
          
          return (
            <tr key={id} style={{
              backgroundColor: hasError ? '#f8d7da' : hasWarning ? '#fff3cd' : 'transparent'
            }} title={
              validationItem && !validationItem.isValid 
                ? `Sprint total: ${validationItem.sprintEffettivo}h, Difference: ${validationItem.difference}h`
                : undefined
            }>
              <td>{id}</td>
              <td>{item}</td>
              <td>{stima}</td>
              <td style={{
                fontWeight: hasError || hasWarning ? 'bold' : 'normal',
                color: hasError ? '#721c24' : hasWarning ? '#856404' : 'inherit'
              }}>
                {effettivo}
                {validationItem && !validationItem.isValid && (
                  <span style={{ marginLeft: '0.5rem', fontSize: '0.8em' }}>
                    {hasError ? '❌' : '⚠️'}
                  </span>
                )}
              </td>
              {sprints.map((val, i) => (
                <td key={i}>{val}</td>
              ))}
            </tr>
          );
        })}
        <tr>
          <td colSpan={2}><strong>TOT</strong></td>
          <td><strong>{totals.stima}</strong></td>
          <td><strong>{totals.effettivo}</strong></td>
          {totals.sprints.map((s, i) => (
            <td key={i}><strong>{s}</strong></td>
          ))}
        </tr>
      </tbody>
    </table>
    </>
  );
}
