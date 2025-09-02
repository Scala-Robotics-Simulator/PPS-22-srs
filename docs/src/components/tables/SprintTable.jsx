import { validateBacklogConsistency, formatValidationResults } from './validation.js';

export function SprintTable({ data, backlogData, showValidation = false }) {
  // Error handling: check if data is valid
  if (!data || !Array.isArray(data) || data.length === 0) {
    return (
      <table>
        <thead>
          <tr>
            <th>Backlog Item</th>
            <th>Id</th>
            <th>Task</th>
            <th>Volontario</th>
            <th>Stima (h)</th>
            <th>Effettivo (h)</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td colSpan={6}>No data available</td>
          </tr>
        </tbody>
      </table>
    );
  }

  // Determine the maximum number of days from the data
  const maxDays = Math.max(...data.map(task => 
    task.days && Array.isArray(task.days) ? task.days.length : 0
  ));
  
  // Normalize data to ensure consistent structure
  const normalizedData = data.map(task => ({
    backlogItem: task.backlogItem || '',
    id: task.id || '',
    task: task.task || '',
    volontario: task.volontario || '',
    stima: typeof task.stima === 'number' ? task.stima : 0,
    effettivo: typeof task.effettivo === 'number' ? task.effettivo : 0,
    days: Array.isArray(task.days) 
      ? [...task.days, ...Array(Math.max(0, maxDays - task.days.length)).fill(0)]
      : Array(maxDays).fill(0)
  }));

  const printingData = data.map(task => {
    const stima = typeof task.stima === 'number' ? task.stima : 0;
    const effettivo = typeof task.effettivo === 'number' ? task.effettivo : 0;
    const days = Array.isArray(task.days) ? task.days : [];
  
    let cumulative = 0;
    const remaining = days.map(done => {
      cumulative += done || 0;
      return Math.max(effettivo - cumulative, 0);
    });
  
    const padded = [
      ...remaining,
      ...Array(Math.max(0, maxDays - remaining.length)).fill(
        remaining.length > 0 ? remaining[remaining.length - 1] : stima
      )
    ];
  
    return {
      backlogItem: task.backlogItem || '',
      id: task.id || '',
      task: task.task || '',
      volontario: task.volontario || '',
      stima,
      effettivo,
      days: padded
    };
  });


  const dayTotals = Array(maxDays).fill(0);
  
  const effettivoTot = printingData.reduce((acc, task) => {
    task.days.forEach((d, i) => (dayTotals[i] += d));
    return acc + task.effettivo;
  }, 0);
  const stimaTot = printingData.reduce((acc, task) => {
    return acc + task.stima;
  }, 0);

  // Perform validation if backlog data is provided
  const validation = backlogData && showValidation 
    ? formatValidationResults(validateBacklogConsistency(backlogData, normalizedData))
    : null;

  // TODO: calculate days and effective time programmatically
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
          <th>Backlog Item</th>
          <th>Id</th>
          <th>Task</th>
          <th>Volontario</th>
          <th>Stima (h)</th>
          <th>Effettivo (h)</th>
          {Array.from({ length: maxDays }, (_, i) => (
            <th key={i}>D{i + 1}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {printingData.map(({ backlogItem, id, task, volontario, stima, effettivo, days }, i) => {
          // Check if this task has a validation error (days sum doesn't match effettivo)
          const daysSum = days.reduce((sum, day) => sum + (typeof day === 'number' ? day : 0), 0);
          const daysSumMismatch = false;
          
          return (
            <tr key={id || i} style={{
              backgroundColor: daysSumMismatch ? '#f8d7da' : 'transparent'
            }} title={
              daysSumMismatch 
                ? `Days sum: ${daysSum}h, Effettivo: ${effettivo}h, Difference: ${Math.abs(daysSum - effettivo).toFixed(2)}h`
                : undefined
            }>
              <td>{backlogItem}</td>
              <td>{id}</td>
              <td>{task}</td>
              <td>{volontario}</td>
              <td>{stima}</td>
              <td style={{
                fontWeight: daysSumMismatch ? 'bold' : 'normal',
                color: daysSumMismatch ? '#721c24' : 'inherit'
              }}>
                {effettivo}
                {daysSumMismatch && (
                  <span style={{ marginLeft: '0.5rem', fontSize: '0.8em' }}>
                    ❌
                  </span>
                )}
              </td>
              {days.map((d, i) => (
                <td key={i} style={{
                  fontWeight: daysSumMismatch ? 'bold' : 'normal',
                  color: daysSumMismatch ? '#721c24' : 'inherit'
                }}>{d}</td>
              ))}
            </tr>
          );
        })}
        <tr>
          <td colSpan={2}></td>
          <td><strong>TOT</strong></td>
          <td></td>
          <td><strong>{stimaTot}</strong></td>
          <td><strong>{effettivoTot}</strong></td>
          {dayTotals.map((t, i) => (
            <td key={i}><strong>{t}</strong></td>
          ))}
        </tr>
      </tbody>
    </table>
    </>
  );
}