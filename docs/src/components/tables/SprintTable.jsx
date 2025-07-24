export function SprintTable({ data }) {
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

  const dayTotals = Array(maxDays).fill(0);
  
  const effettivoTot = normalizedData.reduce((acc, task) => {
    task.days.forEach((d, i) => (dayTotals[i] += d));
    return acc + task.effettivo;
  }, 0);
  const stimaTot = normalizedData.reduce((acc, task) => {
    return acc + task.stima;
  }, 0);
  // TODO: calculate days and effective time programmatically
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
          {Array.from({ length: maxDays }, (_, i) => (
            <th key={i}>D{i + 1}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {normalizedData.map(({ backlogItem, id, task, volontario, stima, effettivo, days }, i) => (
          <tr key={id || i}>
            <td>{backlogItem}</td>
            <td>{id}</td>
            <td>{task}</td>
            <td>{volontario}</td>
            <td>{stima}</td>
            <td>{effettivo}</td>
            {days.map((d, i) => (
              <td key={i}>{d}</td>
            ))}
          </tr>
        ))}
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
  );
}